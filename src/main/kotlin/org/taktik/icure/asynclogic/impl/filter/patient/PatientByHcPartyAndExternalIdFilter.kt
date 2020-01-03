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
package org.taktik.icure.asynclogic.impl.filter.patient

import kotlinx.coroutines.flow.Flow
import org.springframework.beans.factory.annotation.Autowired
import org.taktik.icure.asynclogic.AsyncSessionLogic
import org.taktik.icure.asynclogic.impl.filter.Filter
import org.taktik.icure.asynclogic.impl.filter.Filters
import org.taktik.icure.dto.filter.patient.PatientByHcPartyAndExternalIdFilter
import org.taktik.icure.entities.Patient
import org.taktik.icure.asynclogic.PatientLogic
import org.taktik.icure.utils.getLoggedHealthCarePartyId
import java.util.*
import javax.security.auth.login.LoginException

class PatientByHcPartyAndExternalIdFilter(private val patientLogic: PatientLogic,
                                          private val sessionLogic: AsyncSessionLogic) : Filter<String, Patient, PatientByHcPartyAndExternalIdFilter> {

    override suspend fun resolve(filter: PatientByHcPartyAndExternalIdFilter, context: Filters): Flow<String> {
        return try {
            patientLogic.listByHcPartyAndExternalIdsOnly(filter.externalId, if (filter.healthcarePartyId != null) filter.healthcarePartyId else getLoggedHealthCarePartyId(sessionLogic))
        } catch (e: LoginException) {
            throw IllegalArgumentException(e)
        }
    }
}
