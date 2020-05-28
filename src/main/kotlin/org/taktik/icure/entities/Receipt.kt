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
import com.github.pozo.KotlinBuilder
import org.ektorp.Attachment
import org.taktik.icure.entities.base.CodeStub
import org.taktik.icure.entities.base.Encryptable
import org.taktik.icure.entities.base.StoredICureDocument
import org.taktik.icure.entities.embed.Delegation
import org.taktik.icure.entities.embed.ReceiptBlobType
import org.taktik.icure.entities.embed.RevisionInfo
import org.taktik.icure.entities.utils.MergeUtil.mergeListsDistinct
import org.taktik.icure.utils.DynamicInitializer
import org.taktik.icure.utils.invoke
import org.taktik.icure.validation.AutoFix
import org.taktik.icure.validation.NotNull
import org.taktik.icure.validation.ValidCode

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@KotlinBuilder
data class Receipt(
        @JsonProperty("_id") override val id: String,
        @JsonProperty("_rev") override val rev: String? = null,
        @NotNull(autoFix = AutoFix.NOW) override val created: Long? = null,
        @NotNull(autoFix = AutoFix.NOW) override val modified: Long? = null,
        @NotNull(autoFix = AutoFix.CURRENTUSERID) override val author: String? = null,
        @NotNull(autoFix = AutoFix.CURRENTHCPID) override val responsible: String? = null,
        override val medicalLocationId: String? = null,
        @ValidCode(autoFix = AutoFix.NORMALIZECODE) override val tags: Set<CodeStub> = setOf(),
        @ValidCode(autoFix = AutoFix.NORMALIZECODE) override val codes: Set<CodeStub> = setOf(),
        override val endOfLife: Long? = null,
        @JsonProperty("deleted") override val deletionDate: Long? = null,

        val attachmentIds: Map<ReceiptBlobType, String> = mapOf(),
        val references: List<String> = listOf(), //nipReference:027263GFF152, errorCode:186, errorPath:/request/transaction, org.taktik.icure.entities;tarification:id, org.taktik.entities.Invoice:UUID

        //The ICureDocument (Invoice, Contact, ...) this document is linked to
        val documentId: String? = null,
        val category: String? = null,
        val subCategory: String? = null,

        override val secretForeignKeys: Set<String> = setOf(),
        override val cryptedForeignKeys: Map<String, Set<Delegation>> = mapOf(),
        override val delegations: Map<String, Set<Delegation>> = mapOf(),
        override val encryptionKeys: Map<String, Set<Delegation>> = mapOf(),
        override val encryptedSelf: String? = null,
        @JsonProperty("_attachments") override val attachments: Map<String, Attachment>? = null,
        @JsonProperty("_revs_info") override val revisionsInfo: List<RevisionInfo>? = null,
        @JsonProperty("_conflicts") override val conflicts: List<String>? = null,
        @JsonProperty("rev_history") override val revHistory: Map<String, String>? = null,
        @JsonProperty("java_type") override val _type: String = Receipt::javaClass.name
) : StoredICureDocument, Encryptable {
    companion object : DynamicInitializer<Receipt>

    fun merge(other: Receipt) = Receipt(args = this.solveConflictsWith(other))
    fun solveConflictsWith(other: Receipt) =
            super<StoredICureDocument>.solveConflictsWith(other) +
                    super<Encryptable>.solveConflictsWith(other) + mapOf(
                    "attachmentIds" to (other.attachmentIds + this.attachmentIds),
                    "references" to mergeListsDistinct(this.references, other.references),
                    "documentId" to (this.documentId ?: other.documentId),
                    "category" to (this.category ?: other.category),
                    "subCategory" to (this.subCategory ?: other.subCategory)
            )

    override fun withIdRev(id: String?, rev: String) = if (id != null) this.copy(id = id, rev = rev) else this.copy(rev = rev)
    override fun withDeletionDate(deletionDate: Long?) = this.copy(deletionDate = deletionDate)
    override fun withTimestamps(created: Long?, modified: Long?) =
            when {
                created != null && modified != null -> this.copy(created = created, modified = modified)
                created != null -> this.copy(created = created)
                modified != null -> this.copy(modified = modified)
                else -> this
            }

}
