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
import org.taktik.icure.constants.PropertyTypeScope
import org.taktik.icure.constants.TypedValuesType
import org.taktik.icure.entities.base.Identifiable
import org.taktik.icure.entities.base.StoredDocument
import java.io.Serializable

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class PropertyType : StoredDocument, Cloneable, Serializable, Identifiable<String?>, Cloneable {
    var identifier: String? = null
    var type: TypedValuesType? = null
    var scope: PropertyTypeScope? = null
    var unique = false
    var editor: String? = null
    var localized = false

    constructor() {}
    constructor(type: TypedValuesType?, identifier: String?) {
        this.type = type
        this.identifier = identifier
    }

    constructor(type: TypedValuesType?, scope: PropertyTypeScope?, identifier: String?) {
        this.type = type
        this.scope = scope
        this.identifier = identifier
    }

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Any {
        return super.clone()
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as PropertyType
        return id == that.id
    }

    override fun hashCode(): Int {
        return if (id != null) id.hashCode() else 0
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
