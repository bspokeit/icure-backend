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

package org.taktik.icure.services.external.rest.v1.controllers.support

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactor.mono
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.taktik.couchdb.DocIdentifier
import org.taktik.icure.asynclogic.AsyncSessionLogic
import org.taktik.icure.asynclogic.DocumentTemplateLogic
import org.taktik.icure.entities.DocumentTemplate
import org.taktik.icure.entities.embed.DocumentType
import org.taktik.icure.services.external.rest.v1.dto.DocumentTemplateDto
import org.taktik.icure.services.external.rest.v1.dto.data.ByteArrayDto
import org.taktik.icure.services.external.rest.v1.mapper.DocumentTemplateMapper
import org.taktik.icure.utils.FormUtils
import org.taktik.icure.utils.injectReactorContext
import reactor.core.publisher.Flux
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamSource

@ExperimentalCoroutinesApi
@RestController
@RequestMapping("/rest/v1/doctemplate")
@Tag(name = "doctemplate")
class DocumentTemplateController(
        private val documentTemplateLogic: DocumentTemplateLogic,
        private val sessionLogic: AsyncSessionLogic,
        private val documentTemplateMapper: DocumentTemplateMapper
) {
    @Operation(summary = "Gets a document template")
    @GetMapping("/{documentTemplateId}")
    fun getDocumentTemplate(@PathVariable documentTemplateId: String) = mono {
        val documentTemplate = documentTemplateLogic.getDocumentTemplateById(documentTemplateId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "DocumentTemplate fetching failed")
        documentTemplateMapper.map(documentTemplate)
    }

    @Operation(summary = "Deletes a document template")
    @DeleteMapping("/{documentTemplateIds}")
    fun deleteDocumentTemplate(@PathVariable documentTemplateIds: String): Flux<DocIdentifier> {
        val documentTemplateIdsList = documentTemplateIds.split(',')
        return try {
            documentTemplateLogic.deleteByIds(documentTemplateIdsList).injectReactorContext()
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Document template deletion failed")
        }
    }

    @Operation(summary = "Gets all document templates")
    @GetMapping("/bySpecialty/{specialityCode}")
    fun findDocumentTemplatesBySpeciality(@PathVariable specialityCode: String): Flux<DocumentTemplateDto> {
        val documentTemplates = documentTemplateLogic.getDocumentTemplatesBySpecialty(specialityCode)
        return documentTemplates.map { ft -> documentTemplateMapper.map(ft) }.injectReactorContext()
    }

    @Operation(summary = "Gets all document templates by Type")
    @GetMapping("/byDocumentType/{documentTypeCode}")
    fun findDocumentTemplatesByDocumentType(@PathVariable documentTypeCode: String): Flux<DocumentTemplateDto> {
        DocumentType.fromName(documentTypeCode)
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot retrieve document templates: provided Document Type Code doesn't exists")
        val documentTemplates = documentTemplateLogic.getDocumentTemplatesByDocumentType(documentTypeCode)
        return documentTemplates.map { ft -> documentTemplateMapper.map(ft) }.injectReactorContext()
    }

    @Operation(summary = "Gets all document templates by Type For currentUser")
    @GetMapping("/byDocumentTypeForCurrentUser/{documentTypeCode}")
    fun findDocumentTemplatesByDocumentTypeForCurrentUser(@PathVariable documentTypeCode: String): Flux<DocumentTemplateDto> = flow {
        DocumentType.fromName(documentTypeCode)
                ?: throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot retrieve document templates: provided Document Type Code doesn't exists")
        emitAll(
                documentTemplateLogic.getDocumentTemplatesByDocumentTypeAndUser(documentTypeCode, sessionLogic.getCurrentUserId())
                        .map { ft -> documentTemplateMapper.map(ft) }
        )
    }.injectReactorContext()

    @Operation(summary = "Gets all document templates for current user")
    @GetMapping
    fun findDocumentTemplates(): Flux<DocumentTemplateDto> = flow {
        emitAll(
                documentTemplateLogic.getDocumentTemplatesByUser(sessionLogic.getCurrentUserId())
                        .map { ft -> documentTemplateMapper.map(ft) }
        )
    }.injectReactorContext()

    @Operation(summary = "Gets all document templates for all users")
    @GetMapping("/find/all")
    fun findAllDocumentTemplates(): Flux<DocumentTemplateDto> {
        val documentTemplates = documentTemplateLogic.getAllEntities()
        return documentTemplates.map { ft -> documentTemplateMapper.map(ft) }.injectReactorContext()
    }

    @Operation(summary = "Create a document template with the current user", description = "Returns an instance of created document template.")
    @PostMapping
    fun createDocumentTemplate(@RequestBody ft: DocumentTemplateDto) = mono {
        val documentTemplate = documentTemplateLogic.createDocumentTemplate(documentTemplateMapper.map(ft))
        documentTemplateMapper.map(documentTemplate)
    }

    @Operation(summary = "Modify a document template with the current user", description = "Returns an instance of created document template.")
    @PutMapping("/{documentTemplateId}")
    fun updateDocumentTemplate(@PathVariable documentTemplateId: String, @RequestBody ft: DocumentTemplateDto) = mono {
        val template = documentTemplateMapper.map(ft).copy(id = documentTemplateId)
        val documentTemplate = documentTemplateLogic.modifyDocumentTemplate(template)
                ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Document Template update failed")

        documentTemplateMapper.map(documentTemplate)
    }

    @Operation(summary = "Download a the document template attachment")
    @GetMapping("/{documentTemplateId}/attachment/{attachmentId}", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun getDocumentTemplateAttachment(@PathVariable documentTemplateId: String,
                                      @PathVariable attachmentId: String,
                                      response: ServerHttpResponse) = mono {
        val document = documentTemplateLogic.getDocumentTemplateById(documentTemplateId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found")
        if (document.attachment != null) {
            if (document.version == null) {
                val xmlSource = StreamSource(ByteArrayInputStream(document.attachment))
                val xsltSource = StreamSource(FormUtils::class.java.getResourceAsStream("DocumentTemplateLegacyToNew.xml"))
                val byteOutputStream = ByteArrayOutputStream()
                val result = javax.xml.transform.stream.StreamResult(byteOutputStream)
                val transFact = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null)
                try {
                    val trans = transFact.newTransformer(xsltSource)
                    trans.transform(xmlSource, result)
                    byteOutputStream.toByteArray()
                } catch (e: TransformerException) {
                    throw IllegalStateException("Could not convert legacy document")
                }
            } else {
                document.attachment
            }
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "AttachmentDto not found")
        }
    }

    @Operation(summary = "Download a the document template attachment")
    @GetMapping("/{documentTemplateId}/attachmentText/{attachmentId}", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun getAttachmentText(@PathVariable documentTemplateId: String, @PathVariable attachmentId: String,
                          response: ServerHttpResponse) = response.writeWith(flow {
        val document = documentTemplateLogic.getDocumentTemplateById(documentTemplateId)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found")
        if (document.attachment != null) {
            emit(DefaultDataBufferFactory().wrap(document.attachment))
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "AttachmentDto not found")
        }
    }.injectReactorContext())

    @Operation(summary = "Creates a document's attachment")
    @PutMapping("/{documentTemplateId}/attachment", consumes = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun setDocumentTemplateAttachment(@PathVariable documentTemplateId: String, @RequestBody payload: ByteArray) = mono {
        val documentTemplate = documentTemplateLogic.getDocumentTemplateById(documentTemplateId)
                ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Document modification failed")
        documentTemplateLogic.modifyDocumentTemplate(documentTemplate.copy(attachment = payload))?.let { documentTemplateMapper.map(it) }
    }

    @Operation(summary = "Creates a document's attachment")
    @PutMapping("/{documentTemplateId}/attachmentJson", consumes = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun setDocumentTemplateAttachmentJson(@PathVariable documentTemplateId: String, @RequestBody payload: ByteArrayDto) = mono {
        val documentTemplate = documentTemplateLogic.getDocumentTemplateById(documentTemplateId)
                ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Document modification failed")
        documentTemplateLogic.modifyDocumentTemplate(documentTemplate.copy(attachment = payload.data))?.let { documentTemplateMapper.map(it) }
    }
}