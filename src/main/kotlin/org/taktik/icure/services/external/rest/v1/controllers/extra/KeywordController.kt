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

package org.taktik.icure.services.external.rest.v1.controllers.extra

import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.Operation
import kotlinx.coroutines.flow.map
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import org.taktik.couchdb.DocIdentifier
import org.taktik.icure.entities.Keyword
import org.taktik.icure.asynclogic.KeywordLogic
import org.taktik.icure.services.external.rest.v1.dto.KeywordDto
import org.taktik.icure.services.external.rest.v1.mapper.KeywordMapper
import org.taktik.icure.utils.injectReactorContext
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/rest/v1/keyword")
@Tag(name = "keyword")
class KeywordController(private val keywordLogic: KeywordLogic, private val keywordMapper: KeywordMapper) {

    @Operation(summary = "Create a keyword with the current user", description = "Returns an instance of created keyword.")
    @PostMapping
    suspend fun createKeyword(@RequestBody c: KeywordDto) =
            keywordLogic.createKeyword(keywordMapper.map(c))?.let { keywordMapper.map(it) }
                    ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Keyword creation failed.")

    @Operation(summary = "Get a keyword")
    @GetMapping("/{keywordId}")
    suspend fun getKeyword(@PathVariable keywordId: String) =
            keywordLogic.getKeyword(keywordId)?.let { keywordMapper.map(it) }
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Getting keyword failed. Possible reasons: no such keyword exists, or server error. Please try again or read the server log.")


    @Operation(summary = "Get keywords by user")
    @GetMapping("/byUser/{userId}")
    fun getKeywordsByUser(@PathVariable userId: String) =
            keywordLogic.getKeywordsByUser(userId).let { it.map { c -> keywordMapper.map(c) } }.injectReactorContext()

    @Operation(summary = "Gets all keywords")
    @GetMapping
    fun getKeywords(): Flux<KeywordDto> {
        return keywordLogic.getAllEntities().map { c -> keywordMapper.map(c) }.injectReactorContext()
    }

    @Operation(summary = "Delete keywords.", description = "Response is a set containing the ID's of deleted keywords.")
    @DeleteMapping("/{keywordIds}")
    fun deleteKeywords(@PathVariable keywordIds: String): Flux<DocIdentifier> {
        val ids = keywordIds.split(',')
        if (ids.isEmpty()) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "A required query parameter was not specified for this request.")
        return keywordLogic.deleteKeywords(ids.toSet()).injectReactorContext()
    }

    @Operation(summary = "Modify a keyword", description = "Returns the modified keyword.")
    @PutMapping
    suspend fun modifyKeyword(@RequestBody keywordDto: KeywordDto): KeywordDto {
        keywordLogic.modifyKeyword(keywordMapper.map(keywordDto))
        return keywordLogic.getKeyword(keywordDto.id)?.let { keywordMapper.map(it) }
                ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Keyword modification failed.")
    }
}