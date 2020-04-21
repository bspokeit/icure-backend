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
package org.taktik.icure.entities

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import org.ektorp.Attachment
import org.taktik.icure.entities.base.*
import org.taktik.icure.entities.embed.LetterValue
import org.taktik.icure.entities.embed.Periodicity
import org.taktik.icure.entities.embed.RevisionInfo
import org.taktik.icure.entities.embed.Valorisation
import org.taktik.icure.entities.utils.MergeUtil.mergeListsDistinct
import org.taktik.icure.entities.utils.MergeUtil.mergeMapsOfSets
import org.taktik.icure.entities.utils.MergeUtil.mergeSets
import org.taktik.icure.utils.DynamicInitializer
import org.taktik.icure.utils.invoke

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class Tarification(
        @JsonProperty("_id") override val id: String,         // id = type|code|version  => this must be unique
        @JsonProperty("_rev") override val rev: String? = null,
        @JsonProperty("deleted") override val deletionDate: Long? = null,

        override val type : String? = null, //ex: ICD (type + version + code combination must be unique) (or from tags -> CD-ITEM)
        override val code : String? = null, //ex: I06.2 (or from tags -> healthcareelement). Local codes are encoded as LOCAL:SLLOCALFROMMYSOFT
        override val version : String? = null, //ex: 10. Must be lexicographically searchable

        val author: String? = null,
        val regions : Set<String> = setOf(), //ex: be,fr
        val periodicity: List<Periodicity> = listOf(),
        val level : Int? = null, //ex: 0 = System, not to be modified by user, 1 = optional, created or modified by user
        val label : Map<String, String> = mapOf(), //ex: {en: Rheumatic Aortic Stenosis, fr: Sténose rhumatoïde de l'Aorte}
        val links : List<String> = listOf(), //Links towards related codes (corresponds to an approximate link in qualifiedLinks)
        val qualifiedLinks : Map<LinkQualification, List<String>> = mapOf(), //Links towards related codes
        val flags : Set<CodeFlag> = setOf(), //flags (like female only) for the code
        val searchTerms : Map<String, Set<String>> = mapOf(), //Extra search terms/ language
        val data: String? = null,
        val appendices: Map<AppendixType, String> = mapOf(),
        val isDisabled: Boolean = false,
        val valorisations: Set<Valorisation> = setOf(),
        val category: Map<String, String> = mapOf(),
        val consultationCode: Boolean? = null,
        val hasRelatedCode: Boolean? = null,
        val needsPrescriber: Boolean? = null,
        val relatedCodes: Set<String> = setOf(),
        val nGroup: String? = null,
        val letterValues: List<LetterValue> = listOf(),

        @JsonProperty("_attachments") override val attachments: Map<String, Attachment>? = null,
        @JsonProperty("_revs_info") override val revisionsInfo: List<RevisionInfo>? = null,
        @JsonProperty("_conflicts") override val conflicts: List<String>? = null,
        @JsonProperty("rev_history") override val revHistory: Map<String, String>? = null,
        @JsonProperty("java_type") override val _type: String = Tarification::javaClass.name
) : StoredDocument, CodeIdentification {
    companion object : DynamicInitializer<Tarification>
    fun merge(other: Tarification) = Tarification(args = this.solveConflictsWith(other))
    fun solveConflictsWith(other: Tarification) = super<StoredDocument>.solveConflictsWith(other) + super<CodeIdentification>.solveConflictsWith(other) + mapOf(
            "author" to (this.author ?: other.author),
            "regions" to (other.regions + this.regions),
            "periodicity" to (other.periodicity + this.periodicity),
            "level" to (this.level ?: other.level),
            "label" to (other.label + this.label),
            "links" to (other.links + this.links),
            "qualifiedLinks" to (other.qualifiedLinks + this.qualifiedLinks),
            "flags" to (other.flags + this.flags),
            "searchTerms" to mergeMapsOfSets(this.searchTerms, other.searchTerms),
            "data" to (this.data ?: other.data),
            "appendices" to (other.appendices + this.appendices),
            "isDisabled" to (this.isDisabled),
            "valorisations" to mergeSets(this.valorisations, other.valorisations,
                    {a,b -> a.predicate == b.predicate && a.startOfValidity == b.startOfValidity && a.endOfValidity == b.endOfValidity}),
            "category" to (other.category + this.category),
            "consultationCode" to (this.consultationCode ?: other.consultationCode),
            "hasRelatedCode" to (this.hasRelatedCode ?: other.hasRelatedCode),
            "needsPrescriber" to (this.needsPrescriber ?: other.needsPrescriber),
            "relatedCodes" to (other.relatedCodes + this.relatedCodes),
            "nGroup" to (this.nGroup ?: other.nGroup),
            "letterValues" to mergeListsDistinct(this.letterValues, other.letterValues,
                    {a,b -> a.coefficient == b.coefficient && a.index == b.index && a.letter == b.letter})
    )
    override fun withIdRev(id: String?, rev: String): Tarification =
            if (id != null) this.copy(id = id, rev = rev) else this.copy(rev = rev)
}
