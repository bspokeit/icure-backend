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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.github.pozo.KotlinBuilder
import org.taktik.icure.constants.TypedValuesType
import org.taktik.icure.utils.InstantDeserializer
import org.taktik.icure.utils.InstantSerializer
import java.io.Serializable
import java.time.Instant
import java.util.Date


@KotlinBuilder
data class TypedValueDto<T>(
        val type: TypedValuesType? = null,
        val booleanValue: Boolean? = null,
        val integerValue: Int? = null,
        val doubleValue: Double? = null,
        val stringValue: String? = null,

        @JsonSerialize(using = InstantSerializer::class, include = JsonSerialize.Inclusion.NON_NULL)
        @JsonDeserialize(using = InstantDeserializer::class)
        val dateValue: Instant? = null
) : Comparable<TypedValueDto<T>>, Serializable {
    companion object {
        fun <T> withValue(value: T): TypedValueDto<T> = withTypeAndValue(when (value) {
            is Boolean -> TypedValuesType.BOOLEAN
            is Int -> TypedValuesType.INTEGER
            is Double -> TypedValuesType.DOUBLE
            is String -> TypedValuesType.STRING
            is Date -> TypedValuesType.DATE
            else -> throw IllegalArgumentException("Unknown value type")
        }, value)

        fun <T> withTypeAndValue(type: TypedValuesType, value: T): TypedValueDto<T> = when (type) {
            TypedValuesType.BOOLEAN -> if (value is Boolean) {
                TypedValueDto(booleanValue = value, type = type)
            } else throw IllegalArgumentException("Illegal boolean value")
            TypedValuesType.INTEGER -> if (value is Int) {
                TypedValueDto(integerValue = value, type = type)
            } else throw IllegalArgumentException("Illegal integer value")
            TypedValuesType.DOUBLE -> if (value is Double) {
                TypedValueDto(doubleValue = value, type = type)
            } else throw IllegalArgumentException("Illegal double value")
            TypedValuesType.STRING, TypedValuesType.JSON, TypedValuesType.CLOB -> if (value is String) {
                TypedValueDto(stringValue = value, type = type)
            } else throw IllegalArgumentException("Illegal string value")
            TypedValuesType.DATE -> if (value is Instant) {
                TypedValueDto(dateValue = value, type = type)
            } else if (value is Date) {
                TypedValueDto(dateValue = (value as Date).toInstant(), type = type)
            } else throw IllegalArgumentException("Illegal date value")
        }
    }

    @JsonIgnore
    fun <T> getValue(): T? {
        if (type == null) {
            return null
        }
        return when (type) {
            TypedValuesType.BOOLEAN -> booleanValue as? T
            TypedValuesType.INTEGER -> integerValue as? T
            TypedValuesType.DOUBLE -> doubleValue as? T
            TypedValuesType.STRING, TypedValuesType.CLOB, TypedValuesType.JSON -> stringValue as? T
            TypedValuesType.DATE -> dateValue as? T
        }
    }

    override fun compareTo(other: TypedValueDto<T>): Int {
        return (other.getValue<T>() as Comparable<T>).compareTo(getValue<T>()!!)
    }

    override fun toString(): String {
        if (type != null) {
            when (type) {
                TypedValuesType.BOOLEAN -> return booleanValue.toString()
                TypedValuesType.INTEGER -> return integerValue.toString()
                TypedValuesType.DOUBLE -> return doubleValue.toString()
                TypedValuesType.STRING, TypedValuesType.CLOB, TypedValuesType.JSON -> return stringValue!!
                TypedValuesType.DATE -> return dateValue.toString()
            }
        }
        return super.toString()
    }
}