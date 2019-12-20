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
package org.taktik.icure.asynclogic.impl.filter.invoice

import kotlinx.coroutines.flow.Flow
import org.springframework.beans.factory.annotation.Autowired
import org.taktik.icure.asynclogic.impl.filter.Filter
import org.taktik.icure.asynclogic.impl.filter.Filters
import org.taktik.icure.dto.filter.invoice.InvoiceByHcPartyCodeDateFilter
import org.taktik.icure.entities.Invoice
import org.taktik.icure.asynclogic.HealthcarePartyLogic
import org.taktik.icure.asynclogic.InvoiceLogic
import java.util.*
import java.util.stream.Collectors

class InvoiceByHcPartyCodeDateFilter(private val invoiceLogic: InvoiceLogic,
                                     private val healthcarePartyLogic: HealthcarePartyLogic) : Filter<String, Invoice, InvoiceByHcPartyCodeDateFilter> {

    override suspend fun resolve(filter: InvoiceByHcPartyCodeDateFilter, context: Filters): Flow<String> {
        return if (filter.healthcarePartyId != null) invoiceLogic.listInvoiceIdsByTarificationsByCode(filter.healthcarePartyId, filter.code(), filter.startInvoiceDate, filter.endInvoiceDate) as Flow<String>
        else healthcarePartyLogic.allEntityIds.flatMap { hcpId -> invoiceLogic.listInvoiceIdsByTarificationsByCode(hcpId, filter.code(), filter.startInvoiceDate, filter.endInvoiceDate) } as Flow<String>
    }
}