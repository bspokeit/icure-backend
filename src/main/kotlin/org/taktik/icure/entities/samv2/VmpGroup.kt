package org.taktik.icure.entities.samv2

import com.fasterxml.jackson.annotation.JsonProperty
import com.github.pozo.KotlinBuilder
import org.ektorp.Attachment
import org.taktik.icure.entities.EntityReference
import org.taktik.icure.entities.base.StoredDocument
import org.taktik.icure.entities.embed.RevisionInfo
import org.taktik.icure.entities.samv2.embed.NoGenericPrescriptionReason
import org.taktik.icure.entities.samv2.embed.NoSwitchReason
import org.taktik.icure.entities.samv2.embed.SamText

@KotlinBuilder
data class VmpGroup(
        @JsonProperty("_id") override val id: String,
        @JsonProperty("_rev") override val rev: String? = null,
        @JsonProperty("deleted") override val deletionDate: Long? = null,

        val from: Long? = null,
        val to: Long? = null,
        val code: String? = null,
        val name: SamText? = null,
        val noGenericPrescriptionReason: NoGenericPrescriptionReason? = null,
        val noSwitchReason: NoSwitchReason? = null,

        @JsonProperty("_attachments") override val attachments: Map<String, Attachment>? = null,
        @JsonProperty("_revs_info") override val revisionsInfo: List<RevisionInfo>? = null,
        @JsonProperty("_conflicts") override val conflicts: List<String>? = null,
        @JsonProperty("rev_history") override val revHistory: Map<String, String>? = null,
        @JsonProperty("java_type") override val _type: String = EntityReference::javaClass.name
) : StoredDocument {
    override fun withIdRev(id: String?, rev: String) = if (id != null) this.copy(id = id, rev = rev) else this.copy(rev = rev)
    override fun withDeletionDate(deletionDate: Long?) = this.copy(deletionDate = deletionDate)
}
