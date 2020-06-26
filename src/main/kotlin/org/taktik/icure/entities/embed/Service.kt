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
package org.taktik.icure.entities.embed

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.github.pozo.KotlinBuilder
import org.taktik.icure.entities.base.CodeStub
import org.taktik.icure.entities.base.ICureDocument
import org.taktik.icure.utils.DynamicInitializer
import org.taktik.icure.utils.invoke
import org.taktik.icure.validation.AutoFix
import org.taktik.icure.validation.NotNull
import org.taktik.icure.validation.ValidCode
import java.util.*

/**
 * Services are created in the course a contact. Information like temperature, blood pressure and so on.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@KotlinBuilder
data class Service(
        @JsonProperty("_id") override val id: String = UUID.randomUUID().toString(),//Only used when the Service is emitted outside of its contact
        @JsonIgnore val contactId: String? = null,
        @JsonIgnore val subContactIds: Set<String>? = null, //Only used when the Service is emitted outside of its contact
        @JsonIgnore val plansOfActionIds: Set<String>? = null, //Only used when the Service is emitted outside of its contact
        @JsonIgnore val healthElementsIds: Set<String>? = null, //Only used when the Service is emitted outside of its contact
        @JsonIgnore val formIds: Set<String>? = null, //Only used when the Service is emitted outside of its contact
        @JsonIgnore val secretForeignKeys: Set<String>? = HashSet(), //Only used when the Service is emitted outside of its contact
        @JsonIgnore val cryptedForeignKeys: Map<String, Set<Delegation>> = mapOf(), //Only used when the Service is emitted outside of its contact
        @JsonIgnore val delegations: Map<String, Set<Delegation>> = mapOf(), //Only used when the Service is emitted outside of its contact
        @JsonIgnore val encryptionKeys: Map<String, Set<Delegation>> = mapOf(), //Only used when the Service is emitted outside of its contact
        val label: String = "<invalid>",
        val dataClassName: String? = null,
        val index: Long? = null, //Used for sorting
        val content: Map<String, Content> = mapOf(), //Localized, in the case when the service contains a document, the document id is the SerializableValue
        @Deprecated("use encryptedSelf instead") val encryptedContent: String? = null, //Crypted (AES+base64) version of the above, deprecated, use encryptedSelf instead
        val textIndexes: Map<String, String> = mapOf(), //Same structure as content but used for full text indexation
        @field:NotNull(autoFix = AutoFix.FUZZYNOW) val valueDate: Long? = null, // YYYYMMDDHHMMSS if unknown, 00, ex:20010800000000. Note that to avoid all confusion: 2015/01/02 00:00:00 is encoded as 20140101235960.
        @field:NotNull(autoFix = AutoFix.FUZZYNOW) val openingDate: Long? = null, // YYYYMMDDHHMMSS if unknown, 00, ex:20010800000000. Note that to avoid all confusion: 2015/01/02 00:00:00 is encoded as 20140101235960.
        val closingDate: Long? = null, // YYYYMMDDHHMMSS if unknown, 00, ex:20010800000000. Note that to avoid all confusion: 2015/01/02 00:00:00 is encoded as 20140101235960.
        val formId: String? = null, //Used to group logically related services
        @field:NotNull(autoFix = AutoFix.NOW) override val created: Long? = null,
        @field:NotNull(autoFix = AutoFix.NOW) override val modified: Long? = null,
        override val endOfLife: Long? = null,
        @field:NotNull(autoFix = AutoFix.CURRENTUSERID) override val author: String? = null, //userId
        @field:NotNull(autoFix = AutoFix.CURRENTHCPID) override val responsible: String? = null, //healthcarePartyId
        override val medicalLocationId: String? = null,
        val comment: String? = null,
        val status: Int? = null, //bit 0: active/inactive, bit 1: relevant/irrelevant, bit2 : present/absent, ex: 0 = active,relevant and present
        val invoicingCodes: Set<String> = setOf(),
        @field:ValidCode(autoFix = AutoFix.NORMALIZECODE) override val codes: Set<CodeStub> = setOf(), //stub object of the Code used to qualify the content of the Service
        @field:ValidCode(autoFix = AutoFix.NORMALIZECODE) override val tags: Set<CodeStub> = setOf(), //stub object of the tag used to qualify the type of the Service
        override val encryptedSelf: String? = null
) : Encrypted, ICureDocument<String>, Comparable<Service> {
    companion object : DynamicInitializer<Service>

    fun merge(other: Service) = Service(args = this.solveConflictsWith(other))
    fun solveConflictsWith(other: Service) = super<Encrypted>.solveConflictsWith(other) + super<ICureDocument>.solveConflictsWith(other) + mapOf(
            "label" to if (this.label.isBlank()) other.label else this.label,
            "dataClassName" to (this.dataClassName ?: other.dataClassName),
            "index" to (this.index ?: other.index),
            "content" to (other.content + this.content),
            "encryptedContent" to (this.encryptedContent ?: other.encryptedContent),
            "textIndexes" to (other.textIndexes + this.textIndexes),
            "valueDate" to (valueDate?.coerceAtMost(other.valueDate ?: Long.MAX_VALUE) ?: other.valueDate),
            "openingDate" to (openingDate?.coerceAtMost(other.openingDate ?: Long.MAX_VALUE) ?: other.openingDate),
            "closingDate" to (closingDate?.coerceAtLeast(other.closingDate ?: 0L) ?: other.closingDate),
            "formId" to (this.formId ?: other.formId),
            "author" to (this.author ?: other.author),
            "responsible" to (this.responsible ?: other.responsible),
            "comment" to (this.comment ?: other.comment),
            "status" to (this.status ?: other.status),
            "invoicingCodes" to (other.invoicingCodes + this.invoicingCodes)
    )

    override fun compareTo(@NotNull other: Service): Int {
        if (this == other) {
            return 0
        }
        var idx = if (index != null && other.index != null) index.compareTo(other.index) else 0
        if (idx != 0) return idx
        idx = id.compareTo(other.id)
        return if (idx != 0) idx else 1
    }
    override fun withTimestamps(created: Long?, modified: Long?) =
            when {
                created != null && modified != null -> this.copy(created = created, modified = modified)
                created != null -> this.copy(created = created)
                modified != null -> this.copy(modified = modified)
                else -> this
            }

}
