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
package org.taktik.icure.services.external.rest.v1.dto

import org.taktik.icure.constants.PropertyTypeScope
import org.taktik.icure.constants.TypedValuesType
import org.taktik.icure.services.external.rest.v1.dto.base.StoredDocumentDto


data class PropertyTypeDto(
        override val id: String,
        override val rev: String? = null,
        override val deletionDate: Long? = null,

        val identifier: String? = null,
        val type: TypedValuesType? = null,
        val scope: PropertyTypeScope? = null,
        val unique: Boolean = false,
        val editor: String? = null,
        val localized: Boolean = false,

        override val _type: String = PropertyTypeDto::javaClass.name
) : StoredDocumentDto {
    override fun withIdRev(id: String?, rev: String) = if (id != null) this.copy(id = id, rev = rev) else this.copy(rev = rev)
    override fun withDeletionDate(deletionDate: Long?) = this.copy(deletionDate = deletionDate)
}