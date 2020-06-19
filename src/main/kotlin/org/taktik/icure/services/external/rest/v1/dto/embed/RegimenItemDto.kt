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

import com.github.pozo.KotlinBuilder
import org.taktik.icure.services.external.rest.v1.dto.base.CodeStubDto
import java.io.Serializable

@KotlinBuilder
data class RegimenItemDto(
        //Day definition (One and only one of the three following should be not null)
        //The three are null if it applies to every day
        val date: Long? = null, //yyyymmdd at this date
        val dayNumber: Int? = null, //day 1 of treatment. 1 based numeration
        val weekday: Weekday? = null, //on monday

        //Time of day definition (One and only one of the three following should be not null)
        //Both are null if not specified
        val dayPeriod: CodeStubDto? = null, //CD-DAYPERIOD
        val timeOfDay: Long? = null, //hhmmss 103010
        val administratedQuantity: AdministrationQuantity? = null
) : Serializable