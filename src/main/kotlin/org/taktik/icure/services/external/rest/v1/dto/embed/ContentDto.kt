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
package org.taktik.icure.services.external.rest.v1.dto.embed


import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.github.pozo.KotlinBuilder
import com.squareup.moshi.Json
import org.taktik.icure.utils.InstantDeserializer
import org.taktik.icure.utils.InstantSerializer
import java.io.Serializable
import java.time.Instant

@KotlinBuilder
data class ContentDto(
        @Json(name = "s") val stringValue: String? = null,
        @Json(name = "n") val numberValue: Double? = null,
        @Json(name = "b") val booleanValue: Boolean? = null,
        @Json(name = "i")
        @JsonSerialize(using = InstantSerializer::class, include = JsonSerialize.Inclusion.NON_NULL)
        @JsonDeserialize(using = InstantDeserializer::class)
        val instantValue: Instant? = null,
        @Json(name = "dt") val fuzzyDateValue: Long? = null,
        @Json(name = "x") val binaryValue: ByteArray? = null,
        @Json(name = "d") val documentId: String? = null,
        @Json(name = "m") val measureValue: MeasureDto? = null,
        @Json(name = "p") val medicationValue: MedicationDto? = null,
        @Json(name = "c") val compoundValue: Set<ServiceDto>? = null
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContentDto) return false

        if (stringValue != other.stringValue) return false
        if (numberValue != other.numberValue) return false
        if (booleanValue != other.booleanValue) return false
        if (instantValue != other.instantValue) return false
        if (fuzzyDateValue != other.fuzzyDateValue) return false
        if (binaryValue != null) {
            if (other.binaryValue == null) return false
            if (!binaryValue.contentEquals(other.binaryValue)) return false
        } else if (other.binaryValue != null) return false
        if (documentId != other.documentId) return false
        if (measureValue != other.measureValue) return false
        if (medicationValue != other.medicationValue) return false
        if (compoundValue != other.compoundValue) return false

        return true
    }

    override fun hashCode(): Int {
        var result = stringValue?.hashCode() ?: 0
        result = 31 * result + (numberValue?.hashCode() ?: 0)
        result = 31 * result + (booleanValue?.hashCode() ?: 0)
        result = 31 * result + (instantValue?.hashCode() ?: 0)
        result = 31 * result + (fuzzyDateValue?.hashCode() ?: 0)
        result = 31 * result + (binaryValue?.contentHashCode() ?: 0)
        result = 31 * result + (documentId?.hashCode() ?: 0)
        result = 31 * result + (measureValue?.hashCode() ?: 0)
        result = 31 * result + (medicationValue?.hashCode() ?: 0)
        result = 31 * result + (compoundValue?.hashCode() ?: 0)
        return result
    }
}