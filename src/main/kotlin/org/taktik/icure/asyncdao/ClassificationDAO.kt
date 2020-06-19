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

package org.taktik.icure.asyncdao

import kotlinx.coroutines.flow.Flow
import org.taktik.icure.entities.Classification
import java.net.URI

interface ClassificationDAO : GenericDAO<Classification> {
    fun findByPatient(dbInstanceUrl: URI, groupId: String, patientId: String): Flow<Classification>

    suspend fun getClassification(dbInstanceUrl: URI, groupId: String, classificationId: String): Classification?

    fun findByHCPartySecretPatientKeys(dbInstanceUrl: URI, groupId: String, hcPartyId: String, secretPatientKeys: List<String>): Flow<Classification>
}