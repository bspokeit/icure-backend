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
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.mono
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.taktik.icure.asynclogic.ReceiptLogic
import org.taktik.icure.entities.Receipt
import org.taktik.icure.entities.embed.ReceiptBlobType
import org.taktik.icure.security.CryptoUtils
import org.taktik.icure.services.external.rest.v1.dto.ReceiptDto
import org.taktik.icure.services.external.rest.v1.mapper.ReceiptMapper
import org.taktik.icure.utils.firstOrNull
import org.taktik.icure.utils.injectReactorContext
import reactor.core.publisher.Flux

@ExperimentalCoroutinesApi
@RestController
@RequestMapping("/rest/v1/receipt")
@Tag(name = "receipt")
class ReceiptController(
        private val receiptLogic: ReceiptLogic,
        private val receiptMapper: ReceiptMapper
) {

    @Operation(summary = "Creates a receipt")
    @PostMapping
    fun createReceipt(@RequestBody receiptDto: ReceiptDto) = mono {
        try {
            val created = receiptLogic.createEntities(listOf(receiptMapper.map(receiptDto)))
            created.firstOrNull()?.let { receiptMapper.map(it) }
                    ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Receipt creation failed.")
        } catch (e: Exception) {
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Receipt creation failed.")
        }
    }

    @Operation(summary = "Deletes a receipt")
    @DeleteMapping("/{receiptIds}")
    fun deleteReceipt(@PathVariable receiptIds: String) =
            try {
                receiptLogic.deleteByIds(receiptIds.split(',')).injectReactorContext()
            } catch (e: Exception) {
                throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Receipt deletion failed")
            }


    @Operation(summary = "Get an attachment")
    @GetMapping("/{receiptId}/attachment/{attachmentId}", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun getReceiptAttachment(
            @PathVariable receiptId: String,
            @PathVariable attachmentId: String,
            @RequestParam enckeys: String) = mono {
        val attachment = receiptLogic.getAttachment(receiptId, attachmentId).flatMapConcat { byteBuffer ->
            byteBuffer.array().map { it }.asFlow()
        }.toList().toByteArray()
        if (attachment.isEmpty()) throw ResponseStatusException(HttpStatus.NOT_FOUND, "Attachment not found")
                .also { logger.error(it.message) }
        CryptoUtils.decryptAESWithAnyKey(attachment, enckeys.split('.'))
    }

    @Operation(summary = "Creates a receipt's attachment")
    @PutMapping("/{receiptId}/attachment/{blobType}", consumes = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun setReceiptAttachment(
            @PathVariable receiptId: String,
            @PathVariable blobType: String,
            @RequestParam(required = false) enckeys: String?,
            @RequestBody payload: ByteArray) = mono {

        var encryptedPayload = payload
        if (enckeys?.isNotEmpty() == true) {
            encryptedPayload = CryptoUtils.encryptAESWithAnyKey(encryptedPayload, enckeys.split(',')[0])
        }

        val receipt = receiptLogic.getEntity(receiptId)
        if (receipt != null) {
            receiptLogic.addReceiptAttachment(receipt, ReceiptBlobType.valueOf(blobType), encryptedPayload)
            receiptMapper.map(receipt)
        } else throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Receipt modification failed")
    }

    @Operation(summary = "Gets a receipt")
    @GetMapping("/{receiptId}")
    fun getReceipt(@PathVariable receiptId: String) = mono {
        receiptLogic.getEntity(receiptId)?.let { receiptMapper.map(it) }
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Receipt not found")
    }

    @Operation(summary = "Gets a receipt")
    @GetMapping("/byref/{ref}")
    fun listByReference(@PathVariable ref: String): Flux<ReceiptDto> =
            receiptLogic.listByReference(ref).map { receiptMapper.map(it) }.injectReactorContext()

    @Operation(summary = "Updates a receipt")
    @PutMapping
    fun modifyReceipt(@RequestBody receiptDto: ReceiptDto) = mono {
        val receipt = receiptMapper.map(receiptDto)
        try {
            val updated = receiptLogic.updateEntities(listOf(receipt))
            updated.map { receiptMapper.map(it) }.firstOrNull()
                    ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update receipt")

        } catch (e: Exception) {
            logger.error("Cannot update receipt", e)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Receipt modification failed")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(javaClass)
    }

}