/*
 * Copyright (C) 2018 Taktik SA
 *
 * This file is part of iCureBackend.
 *
 * iCureBackend is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * iCureBackend is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with iCureBackend.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.taktik.icure.asyncdao.impl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.*
import ma.glasnost.orika.Mapper
import org.ektorp.ComplexKey
import org.ektorp.support.View
import org.springframework.beans.factory.annotation.Qualifier

import org.springframework.stereotype.Repository
import org.taktik.couchdb.ViewQueryResultEvent
import org.taktik.couchdb.queryView
import org.taktik.couchdb.queryViewIncludeDocsNoValue
import org.taktik.icure.asyncdao.CodeDAO
import org.taktik.icure.dao.impl.idgenerators.IDGenerator
import org.taktik.icure.db.PaginationOffset
import org.taktik.icure.db.StringUtils
import org.taktik.icure.entities.base.Code
import org.taktik.icure.spring.asynccache.AsyncCacheManager
import org.taktik.icure.utils.createQuery
import org.taktik.icure.utils.firstOrNull
import org.taktik.icure.utils.pagedViewQuery
import java.net.URI

@Repository("codeDAO")
@View(name = "all", map = "function(doc) { if (doc.java_type == 'org.taktik.icure.entities.base.Code' && !doc.deleted) emit( null, doc._id )}")
class CodeDAOImpl(@Qualifier("baseCouchDbDispatcher") couchDbDispatcher: CouchDbDispatcher, idGenerator: IDGenerator, @Qualifier("asyncCacheManager") AsyncCacheManager: AsyncCacheManager) : CachedDAOImpl<Code>(Code::class.java, couchDbDispatcher, idGenerator, AsyncCacheManager, mapper), CodeDAO {
    @View(name = "by_type_code_version", map = "classpath:js/code/By_type_code_version.js", reduce = "function(keys, values, rereduce) {if (rereduce) {return sum(values);} else {return values.length;}}")
    override fun findCodes(dbInstanceUrl: URI, groupId: String, type: String?, code: String?, version: String?): Flow<Code> {
        val client = couchDbDispatcher.getClient(dbInstanceUrl, groupId)
        return client.queryViewIncludeDocsNoValue<Array<String>, Code>(
                createQuery<Code>("by_type_code_version")
                        .includeDocs(true)
                        .reduce(false)
                        .startKey(ComplexKey.of(
                                type,
                                code,
                                version
                        ))
                        .endKey(ComplexKey.of(
                                type ?: ComplexKey.emptyObject(),
                                code ?: ComplexKey.emptyObject(),
                                version ?: ComplexKey.emptyObject()
                        ))).map { it.doc }
    }

    override fun findCodeTypes(dbInstanceUrl: URI, groupId: String, type: String?): Flow<String> {
        val client = couchDbDispatcher.getClient(dbInstanceUrl, groupId)
        return client.queryView<String,String>(
                createQuery<Code>("by_type_code_version")
                        .includeDocs(false)
                        .group(true)
                        .groupLevel(2)
                        .startKey(ComplexKey.of(type, null, null))
                        .endKey(ComplexKey.of(if (type == null) null else type + "\ufff0", null, null))).mapNotNull { it.key }
    }

    @View(name = "by_region_type_code_version", map = "classpath:js/code/By_region_type_code_version.js", reduce = "function(keys, values, rereduce) {if (rereduce) {return sum(values);} else {return values.length;}}")
    override fun findCodes(dbInstanceUrl: URI, groupId: String, region: String?, type: String?, code: String?, version: String?): Flow<Code> {
        val client = couchDbDispatcher.getClient(dbInstanceUrl, groupId)
        return client.queryViewIncludeDocsNoValue<Array<String>, Code>(
                createQuery<Code>("by_region_type_code_version")
                        .includeDocs(true)
                        .reduce(false)
                        .startKey(ComplexKey.of(
                                region ?: "\u0000",
                                type ?: "\u0000",
                                code ?: "\u0000",
                                version ?: "\u0000"
                        ))
                        .endKey(ComplexKey.of(
                                region ?: ComplexKey.emptyObject(),
                                type ?: ComplexKey.emptyObject(),
                                code ?: ComplexKey.emptyObject(),
                                version ?: ComplexKey.emptyObject()
                        ))).map { it.doc }
    }

    override fun findCodeTypes(dbInstanceUrl: URI, groupId: String, region: String?, type: String?): Flow<String> {
        val client = couchDbDispatcher.getClient(dbInstanceUrl, groupId)
        return client.queryView<List<String>,String>(
                createQuery<Code>("by_region_type_code_version")
                        .includeDocs(false)
                        .group(true)
                        .groupLevel(2)
                        .startKey(ComplexKey.of(region, type ?: "", null, null))
                        .endKey(ComplexKey.of(region, if (type == null) ComplexKey.emptyObject() else type + "\ufff0", null, null))
        ).mapNotNull { it.key?.get(1) }
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    override fun findCodes(dbInstanceUrl: URI, groupId: String, region: String?, type: String?, code: String?, version: String?, paginationOffset: PaginationOffset<List<String?>>): Flow<ViewQueryResultEvent> {
        val client = couchDbDispatcher.getClient(dbInstanceUrl, groupId)

        val from = ComplexKey.of(region, type, code, version)
        val to = ComplexKey.of(
                region ?: ComplexKey.emptyObject(),
                type ?: ComplexKey.emptyObject(),
                if (code == null) ComplexKey.emptyObject() else code + "\ufff0",
                if (version == null) ComplexKey.emptyObject() else version + "\ufff0"
        )

        val viewQuery = pagedViewQuery<Code, ComplexKey>(
                "by_region_type_code_version",
                from,
                to,
                paginationOffset.toPaginationOffset { ComplexKey.of(*it.toTypedArray()) },
                false
        )
        return client.queryView(viewQuery, Array<String>::class.java, String::class.java, Code::class.java)
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    @View(name = "by_language_label", map = "classpath:js/code/By_language_label.js")
    override fun findCodesByLabel(dbInstanceUrl: URI, groupId: String, region: String?, language: String?, label: String?, paginationOffset: PaginationOffset<List<String?>>): Flow<ViewQueryResultEvent> {
        val client = couchDbDispatcher.getClient(dbInstanceUrl, groupId)
        val sanitizedLabel= label?.let { StringUtils.sanitizeString(it) }
        val startKey = paginationOffset.startKey
        val from =
            ComplexKey.of(
                    region ?: "\u0000",
                    language ?: "\u0000",
                    sanitizedLabel ?: "\u0000"
            )

        val to = ComplexKey.of(
                if (region == null) ComplexKey.emptyObject() else if (language == null) region + "\ufff0" else region,
                if (language == null) ComplexKey.emptyObject() else if (sanitizedLabel == null) language + "\ufff0" else language,
                if (sanitizedLabel == null) ComplexKey.emptyObject() else sanitizedLabel + "\ufff0"
        )

        val viewQuery = pagedViewQuery<Code, ComplexKey>(
                "by_language_label",
                from,
                to,
                paginationOffset.toPaginationOffset { sk -> ComplexKey.of(*sk.mapIndexed { i, s -> if (i==2) s?.let { StringUtils.sanitizeString(it)} else s }.toTypedArray()) },
                false
        )
        return client.queryView(viewQuery, Array<String>::class.java, String::class.java, Code::class.java)
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    @View(name = "by_language_type_label", map = "classpath:js/code/By_language_type_label.js")
    override fun findCodesByLabel(dbInstanceUrl: URI, groupId: String, region: String?, language: String?, type: String?, label: String?, paginationOffset: PaginationOffset<List<String?>>): Flow<ViewQueryResultEvent> {
        val client = couchDbDispatcher.getClient(dbInstanceUrl, groupId)
        val sanitizedLabel= label?.let { StringUtils.sanitizeString(it) }
        val from = ComplexKey.of(
                    region ?: "\u0000",
                    language ?: "\u0000",
                    type ?: "\u0000",
                    sanitizedLabel ?: "\u0000"
            )

        val to = ComplexKey.of(
			if (region == null) ComplexKey.emptyObject() else if (language == null) region + "\ufff0" else region,
			language ?: ComplexKey.emptyObject(),
			type ?: ComplexKey.emptyObject(),
			if (sanitizedLabel == null) ComplexKey.emptyObject() else sanitizedLabel + "\ufff0"
		)

        val viewQuery = pagedViewQuery<Code, ComplexKey>(
                "by_language_type_label",
                from,
                to,
                paginationOffset.toPaginationOffset { sk -> ComplexKey.of(*sk.mapIndexed { i, s -> if (i==3) s?.let { StringUtils.sanitizeString(it)} else s }.toTypedArray()) },
                false
        )
        return client.queryView(viewQuery, Array<String>::class.java, String::class.java, Code::class.java)
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    @View(name = "by_qualifiedlink_id", map = "classpath:js/code/By_qualifiedlink_id.js")
    override fun findCodesByQualifiedLinkId(dbInstanceUrl: URI, groupId: String, region: String?, linkType: String, linkedId: String?, paginationOffset: PaginationOffset<List<String>>): Flow<ViewQueryResultEvent> {
        val client = couchDbDispatcher.getClient(dbInstanceUrl, groupId)
        val from =
            ComplexKey.of(
                    linkType,
                    linkedId
            )
        val to = ComplexKey.of(
                        linkType,
                        linkedId ?: ComplexKey.emptyObject()
            )

        val viewQuery = pagedViewQuery<Code, ComplexKey>(
                "by_qualifiedlink_id",
                from,
                to,
                paginationOffset.toPaginationOffset { ComplexKey.of(*it.toTypedArray()) },
                false
        )
        return client.queryView(viewQuery, Array<String>::class.java, String::class.java, Code::class.java)
    }

    override fun listCodeIdsByLabel(dbInstanceUrl: URI, groupId: String, region: String?, language: String?, label: String?): Flow<String> {
        val client = couchDbDispatcher.getClient(dbInstanceUrl, groupId)
        val sanitizedLabel= label?.let { StringUtils.sanitizeString(it) }
        val from =
            ComplexKey.of(
                region ?: "\u0000",
                language ?: "\u0000",
                    sanitizedLabel ?: "\u0000"
                         )

        val to = ComplexKey.of(
            if (region == null) ComplexKey.emptyObject() else if (language == null) region + "\ufff0" else region,
            if (language == null) ComplexKey.emptyObject() else if (sanitizedLabel == null) language + "\ufff0" else language,
            if (sanitizedLabel == null) ComplexKey.emptyObject() else sanitizedLabel + "\ufff0"
                              )

        return client.queryView<String,String>(
                createQuery<Code>("by_language_label")
                        .includeDocs(false)
                        .startKey(from)
                        .endKey(to)).mapNotNull { it.key }
    }

    override fun listCodeIdsByLabel(dbInstanceUrl: URI, groupId: String, region: String?, language: String?, type: String?, label: String?): Flow<String> {
        val client = couchDbDispatcher.getClient(dbInstanceUrl, groupId)
        val sanitizedLabel= label?.let { StringUtils.sanitizeString(it) }
        val from =
            ComplexKey.of(
                region ?: "\u0000",
                language ?: "\u0000",
                type ?: "\u0000",
                    sanitizedLabel ?: "\u0000"
                         )
        val to = ComplexKey.of(
            if (region == null) ComplexKey.emptyObject() else if (language == null) region + "\ufff0" else region,
            language ?: ComplexKey.emptyObject(),
            type ?: ComplexKey.emptyObject(),
            if (sanitizedLabel == null) ComplexKey.emptyObject() else sanitizedLabel + "\ufff0"
                              )

        return client.queryView<String,String>(
                createQuery<Code>("by_language_type_label")
                        .includeDocs(false)
                        .startKey(from)
                        .endKey(to)).mapNotNull { it.id }

    }

    override fun listCodeIdsByQualifiedLinkId(dbInstanceUrl: URI, groupId: String, linkType: String, linkedId: String?): Flow<String> {
        val client = couchDbDispatcher.getClient(dbInstanceUrl, groupId)
        val from = ComplexKey.of(
                linkType,
                linkedId
        )
        val to = ComplexKey.of(
                        linkType,
                        linkedId ?: ComplexKey.emptyObject()
        )

        return client.queryView<String,String>(
                createQuery<Code>("by_qualifiedlink_id")
                        .includeDocs(false)
                        .startKey(from)
                        .endKey(to)).mapNotNull { it.id }
    }

    override fun getForPagination(dbInstanceUri: URI, groupId: String, ids: List<String>): Flow<ViewQueryResultEvent> {
        val client = couchDbDispatcher.getClient(dbInstanceUri, groupId)
        return client.getForPagination(ids, Code::class.java)
    }

	override suspend fun isValid(dbInstanceUrl: URI, groupId: String, codeType: String, codeCode: String, codeVersion: String?) = findCodes(dbInstanceUrl, groupId, codeType, codeCode, codeVersion).firstOrNull() != null

	@InternalCoroutinesApi
    override suspend fun getCodeByLabel(dbInstanceUrl: URI, groupId: String, region: String, label: String, ofType: String, labelLang : List<String>) : Code? {
        val client = couchDbDispatcher.getClient(dbInstanceUrl, groupId)
        val sanitizedLabel= label.let { StringUtils.sanitizeString(it) }
        for (lang in labelLang) {
            val codeFlow = client.queryViewIncludeDocsNoValue<Array<String>, Code>(
                    createQuery<Code>("by_region_type_code_version")
                            .includeDocs(true)
                            .reduce(false)
                            .key(ComplexKey.of(
                                    region,
                                    lang,
                                    ofType,
                                    sanitizedLabel
                            ))).map { it.doc }.filter { c -> c.label?.get(lang)?.let { StringUtils.sanitizeString(it) } == sanitizedLabel }
            val code = codeFlow.firstOrNull()
            if (code != null) {
                return code
            }
        }

		//throw IllegalArgumentException("code of type $ofType not found for label $label in languages $labelLang")
        return null
	}
}
