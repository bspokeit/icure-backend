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
package org.taktik.icure.services.external.rest.v1.dto.base


import com.github.pozo.KotlinBuilder
import java.io.Serializable

@KotlinBuilder
data class SecurityDto(
        val admins: RightDto = RightDto(),
        val members: RightDto = RightDto()
) : Serializable {
    @KotlinBuilder
    data class RightDto(val names: Set<String> = setOf(), val roles: Set<String> = setOf())
}