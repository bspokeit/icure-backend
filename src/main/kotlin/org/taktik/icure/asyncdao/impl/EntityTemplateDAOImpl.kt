/*
 * Copyright (C) 2018 Taktik SA
 *
 * This file is part of iCureBackend.
 *
 * iCureBackend is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as published by
 * the Free Software Foundation.
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

import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import org.ektorp.ComplexKey
import org.ektorp.support.View
import org.springframework.beans.factory.annotation.Qualifier

import org.springframework.stereotype.Repository
import org.taktik.couchdb.queryView
import org.taktik.couchdb.queryViewIncludeDocs
import org.taktik.icure.asyncdao.EntityTemplateDAO
import org.taktik.icure.dao.impl.idgenerators.IDGenerator
import org.taktik.icure.db.StringUtils
import org.taktik.icure.entities.EntityTemplate
import org.taktik.icure.spring.asynccache.AsyncCacheManager
import org.taktik.icure.utils.createQuery
import org.taktik.icure.utils.distinctById
import java.net.URI

@Repository("entityTemplateDAO")
@View(name = "all", map = "function(doc) { if (doc.java_type == 'org.taktik.icure.entities.EntityTemplate' && !doc.deleted) emit( null, doc._id )}")
class EntityTemplateDAOImpl(@Qualifier("healthdataCouchDbDispatcher") couchDbDispatcher: CouchDbDispatcher, idGenerator: IDGenerator, @Qualifier("asyncCacheManager") AsyncCacheManager: AsyncCacheManager) : CachedDAOImpl<EntityTemplate>(EntityTemplate::class.java, couchDbDispatcher, idGenerator, AsyncCacheManager, mapper), EntityTemplateDAO {

    @View(name = "by_user_type_descr", map = "classpath:js/entitytemplate/By_user_type_descr.js")
    override suspend fun getByUserIdTypeDescr(dbInstanceUrl: URI, groupId: String, userId: String, type: String, searchString: String?, includeEntities: Boolean?): List<EntityTemplate> {
        val client = couchDbDispatcher.getClient(dbInstanceUrl, groupId)

        val descr = if (searchString != null) StringUtils.sanitizeString(searchString) else null
        val viewQuery = createQuery<EntityTemplate>("by_user_type_descr").startKey(ComplexKey.of(userId, type, descr)).endKey(ComplexKey.of(userId, type, (descr
                ?: "") + "\ufff0")).includeDocs(includeEntities ?: false)

        val result = if (viewQuery.isIncludeDocs) client.queryViewIncludeDocs<ComplexKey, EntityTemplate, EntityTemplate>(viewQuery) else client.queryView<ComplexKey, EntityTemplate>(viewQuery)
        return result.mapNotNull { it.value }.distinctById().toList().sortedWith(compareBy({ it.userId }, { it.entityType }, { it.descr }, { it.id }))
    }

    @View(name = "by_type_descr", map = "classpath:js/entitytemplate/By_type_descr.js")
    override suspend fun getByTypeDescr(dbInstanceUrl: URI, groupId: String, type: String, searchString: String?, includeEntities: Boolean?): List<EntityTemplate> {
        val client = couchDbDispatcher.getClient(dbInstanceUrl, groupId)

        val descr = if (searchString != null) StringUtils.sanitizeString(searchString) else null
        val viewQuery = createQuery<EntityTemplate>("by_type_descr").startKey(ComplexKey.of(type, descr)).endKey(ComplexKey.of(type, (descr
                ?: "") + "\ufff0")).includeDocs(includeEntities ?: false)

        val result = if (viewQuery.isIncludeDocs) client.queryViewIncludeDocs<ComplexKey, EntityTemplate, EntityTemplate>(viewQuery) else client.queryView<ComplexKey, EntityTemplate>(viewQuery)
        return result.mapNotNull { it.value }.distinctById().toList().sortedWith(compareBy({ it.userId }, { it.entityType }, { it.descr }, { it.id }))
    }

}
