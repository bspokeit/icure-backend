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
package org.taktik.icure.asynclogic.impl

import com.google.common.base.Strings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.taktik.couchdb.DocIdentifier
import org.taktik.couchdb.ViewQueryResultEvent
import org.taktik.icure.asyncdao.InvoiceDAO
import org.taktik.icure.asynclogic.AsyncSessionLogic
import org.taktik.icure.asynclogic.EntityReferenceLogic
import org.taktik.icure.asynclogic.InvoiceLogic
import org.taktik.icure.asynclogic.UserLogic
import org.taktik.icure.asynclogic.impl.filter.Filters
import org.taktik.icure.dao.Option
import org.taktik.icure.dao.impl.idgenerators.UUIDGenerator
import org.taktik.icure.db.PaginationOffset
import org.taktik.icure.dto.data.LabelledOccurence
import org.taktik.icure.dto.filter.chain.FilterChain
import org.taktik.icure.entities.Insurance
import org.taktik.icure.entities.Invoice
import org.taktik.icure.entities.Patient
import org.taktik.icure.entities.embed.Delegation
import org.taktik.icure.entities.embed.InvoiceType
import org.taktik.icure.entities.embed.InvoicingCode
import org.taktik.icure.entities.embed.MediumType
import org.taktik.icure.exceptions.DeletionException
import org.taktik.icure.utils.FuzzyValues
import org.taktik.icure.utils.firstOrNull
import org.taktik.icure.utils.toComplexKeyPaginationOffset
import java.lang.IllegalArgumentException
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.abs
import kotlin.math.max

@ExperimentalCoroutinesApi
@Service
class InvoiceLogicImpl(private val filters: Filters,
                       private val userLogic: UserLogic,
                       private val uuidGenerator: UUIDGenerator,
                       private val entityReferenceLogic: EntityReferenceLogic,
                       private val invoiceDAO: InvoiceDAO,
                       private val sessionLogic: AsyncSessionLogic) : GenericLogicImpl<Invoice, InvoiceDAO>(sessionLogic), InvoiceLogic {

    override suspend fun createInvoice(invoice: Invoice) = fix(invoice) { invoice ->
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        invoiceDAO.create(dbInstanceUri, groupId, invoice)
    }

    override suspend fun deleteInvoice(invoiceId: String): DocIdentifier? {
        return try {
            deleteByIds(listOf(invoiceId)).firstOrNull()
        } catch (e: Exception) {
            throw DeletionException(e.message, e)
        }
    }

    override suspend fun getInvoice(invoiceId: String): Invoice? {
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        return invoiceDAO.get(dbInstanceUri, groupId, invoiceId)
    }

    override fun getInvoices(ids: List<String>): Flow<Invoice> = flow {
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        emitAll(invoiceDAO.getList(dbInstanceUri, groupId, ids))
    }

    override suspend fun modifyInvoice(invoice: Invoice) = fix(invoice) { invoice ->
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        invoiceDAO.save(dbInstanceUri, groupId, invoice)
    }

    override fun updateInvoices(invoices: List<Invoice>): Flow<Invoice> = flow {
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        emitAll(invoiceDAO.save(dbInstanceUri, groupId, invoices))
    }

    override suspend fun addDelegation(invoiceId: String, delegation: Delegation): Invoice? {
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        val invoice = getInvoice(invoiceId)
        return delegation.delegatedTo?.let { healthcarePartyId ->
            invoice?.let { c ->
                invoiceDAO.save(dbInstanceUri, groupId, c.copy(delegations = c.delegations + mapOf(
                        healthcarePartyId to setOf(delegation)
                )))
            }
        } ?: invoice
    }

    override suspend fun addDelegations(invoiceId: String, delegations: List<Delegation>): Invoice? {
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        val invoice = getInvoice(invoiceId)
        return invoice?.let {
            return invoiceDAO.save(dbInstanceUri, groupId, it.copy(
                    delegations = it.delegations +
                            delegations.mapNotNull { d -> d.delegatedTo?.let { delegateTo -> delegateTo to setOf(d) } }
            ))
        }
    }

    override fun findByAuthor(hcPartyId: String, fromDate: Long?, toDate: Long?, paginationOffset: PaginationOffset<List<String>>): Flow<ViewQueryResultEvent> = flow {
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        emitAll(invoiceDAO.findByHcParty(dbInstanceUri, groupId, hcPartyId, fromDate, toDate, paginationOffset.toComplexKeyPaginationOffset()))
    }

    override fun listByHcPartyContacts(hcParty: String, contactIds: Set<String>): Flow<Invoice> = flow {
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        emitAll(invoiceDAO.listByHcPartyContacts(dbInstanceUri, groupId, hcParty, contactIds))
    }

    override fun listByHcPartyRecipientIds(hcParty: String, recipientIds: Set<String?>): Flow<Invoice> = flow {
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        emitAll(invoiceDAO.listByHcPartyRecipientIds(dbInstanceUri, groupId, hcParty, recipientIds))
    }

    override fun listByHcPartyPatientSks(hcParty: String, secretPatientKeys: Set<String>): Flow<Invoice> = flow {
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        emitAll(invoiceDAO.listByHcPartyPatientFk(dbInstanceUri, groupId, hcParty, secretPatientKeys))
    }

    override fun listByHcPartySentMediumTypeInvoiceTypeSentDate(hcParty: String, sentMediumType: MediumType, invoiceType: InvoiceType, sent: Boolean, fromDate: Long?, toDate: Long?): Flow<Invoice> = flow {
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        emitAll(invoiceDAO.listByHcPartySentMediumTypeInvoiceTypeSentDate(dbInstanceUri, groupId, hcParty, sentMediumType, invoiceType, sent, fromDate, toDate))
    }

    override fun listByHcPartySendingModeStatus(hcParty: String, sendingMode: String?, status: String?, fromDate: Long?, toDate: Long?): Flow<Invoice> = flow {
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        emitAll(invoiceDAO.listByHcPartySendingModeStatus(dbInstanceUri, groupId, hcParty, sendingMode, status, fromDate, toDate))
    }

    override fun listByHcPartyGroupId(hcParty: String, inputGroupId: String): Flow<Invoice> = flow {
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        emitAll(invoiceDAO.listByHcPartyGroupId(dbInstanceUri, groupId, inputGroupId, hcParty))
    }

    override fun listByHcPartyRecipientIdsUnsent(hcParty: String, recipientIds: Set<String?>): Flow<Invoice> = flow {
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        emitAll(invoiceDAO.listByHcPartyRecipientIdsUnsent(dbInstanceUri, groupId, hcParty, recipientIds))
    }

    override fun listByHcPartyPatientSksUnsent(hcParty: String, secretPatientKeys: Set<String>): Flow<Invoice> = flow {
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        emitAll(invoiceDAO.listByHcPartyPatientFkUnsent(dbInstanceUri, groupId, hcParty, secretPatientKeys))
    }

    override fun listByServiceIds(serviceIds: Set<String>): Flow<Invoice> = flow {
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        emitAll(invoiceDAO.listByServiceIds(dbInstanceUri, groupId, serviceIds))
    }

    override suspend fun mergeInvoices(hcParty: String, invoices: List<Invoice>, destination: Invoice?): Invoice? {
        if (destination == null) return null
        for (i in invoices) {
            deleteInvoice(i.id)
        }
        return modifyInvoice(destination.copy(invoicingCodes = destination.invoicingCodes + invoices.flatMap { it.invoicingCodes }))
    }

    override suspend fun validateInvoice(hcParty: String, invoice: Invoice?, refScheme: String, forcedValue: String?): Invoice? {
        if (invoice == null) return null
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        var refScheme: String = refScheme

        return modifyInvoice(invoice.copy(
                sentDate = System.currentTimeMillis(),
                invoiceReference = if (forcedValue != null || !Strings.isNullOrEmpty(invoice.invoiceReference)) {
                    forcedValue
                } else {
                    if (refScheme == null) {
                        refScheme = "yyyy00000"
                    }
                    val ldt = invoice.invoiceDate?.let { FuzzyValues.getDateTime(it) }
                            ?: LocalDateTime.now(ZoneId.systemDefault())
                    val f: NumberFormat = DecimalFormat("00")
                    val startScheme = refScheme.replace("yyyy".toRegex(), "" + ldt.year).replace("MM".toRegex(), f.format(ldt.monthValue.toLong())).replace("dd".toRegex(), "" + f.format(ldt.dayOfMonth.toLong()))
                    val endScheme = refScheme.replace("0".toRegex(), "9").replace("yyyy".toRegex(), "" + ldt.year).replace("MM".toRegex(), "" + f.format(ldt.monthValue.toLong())).replace("dd".toRegex(), "" + f.format(ldt.dayOfMonth.toLong()))
                    val prefix = "invoice:" + invoice.author + ":xxx:"
                    val fix = startScheme.replace("0+$".toRegex(), "")
                    val reference = entityReferenceLogic.getLatest(prefix + fix)
                    if (reference == null || !reference.id.startsWith(prefix)) {
                        val prevInvoices = invoiceDAO.listByHcPartyReferences(dbInstanceUri, groupId, hcParty, endScheme, null, true, 1)
                        val first = prevInvoices.firstOrNull()
                        "" + if (first?.invoiceReference != null) max(java.lang.Long.valueOf(first.invoiceReference) + 1L, java.lang.Long.valueOf(startScheme) + 1L) else java.lang.Long.valueOf(startScheme) + 1L
                    } else {
                        fix + (reference.id.substring(prefix.length + fix.length).toInt() + 1)
                    }
                }
        ))
    }

    override fun appendCodes(hcPartyId: String, userId: String, insuranceId: String?, secretPatientKeys: Set<String>, type: InvoiceType, sentMediumType: MediumType, invoicingCodes: List<InvoicingCode>, invoiceId: String?, invoiceGraceTime: Int?): Flow<Invoice> = flow {
        val fixedCodes = if (sentMediumType == MediumType.efact) {
            invoicingCodes.map { c -> c.copy(pending = true) }
        } else invoicingCodes
        val invoiceGraceTimeInDays = invoiceGraceTime ?: 0
        val selectedInvoice = if (invoiceId != null) getInvoice(invoiceId) else null
        var invoices = if (selectedInvoice != null) mutableListOf<Invoice>() else listByHcPartyPatientSksUnsent(hcPartyId, secretPatientKeys)
                .filter { i -> i.invoiceType == type && i.sentMediumType == sentMediumType && if (insuranceId == null) i.recipientId == null else insuranceId == i.recipientId }.toList().toMutableList()
        if (selectedInvoice == null && invoices.isEmpty()) {
            invoices = listByHcPartyRecipientIdsUnsent(hcPartyId, insuranceId?.let { setOf(it) }
                    ?: setOf()).filter { i -> i.invoiceType == type && i.sentMediumType == sentMediumType && i.secretForeignKeys == secretPatientKeys }.toList().toMutableList()
        }

        val modifiedInvoices: MutableList<Invoice> = LinkedList()
        val createdInvoices: MutableList<Invoice> = LinkedList()

        for (invoicingCode in fixedCodes) {
            val icDateTime = invoicingCode.dateCode?.let { FuzzyValues.getDateTime(it) }
            val unsentInvoice =
                    selectedInvoice
                            ?: if (icDateTime != null) invoices.filter { i ->
                                val invoiceDate = i.invoiceDate?.let { FuzzyValues.getDateTime(it) }
                                invoiceDate != null && abs(invoiceDate.withHour(0).withMinute(0).withSecond(0).withNano(0).until(icDateTime, ChronoUnit.DAYS)) <= invoiceGraceTimeInDays
                            }.firstOrNull()
                            else null

            if (unsentInvoice != null) {
                if (!createdInvoices.contains(unsentInvoice)) {
                    modifyInvoice(unsentInvoice.copy(
                            invoicingCodes = unsentInvoice.invoicingCodes + listOf(invoicingCode)
                    ))?.let {
                        modifiedInvoices.add(it)
                        emit(it)
                    }
                }
            } else {
                val now = System.currentTimeMillis()
                val newInvoice = Invoice(
                        id = uuidGenerator.newGUID().toString(),
                        invoiceDate = invoicingCode.dateCode ?: now,
                        invoiceType = type,
                        sentMediumType = sentMediumType,
                        recipientId = insuranceId,
                        recipientType = if (type == InvoiceType.mutualfund || type == InvoiceType.payingagency) Insurance::class.java.name else Patient::class.java.name,
                        invoicingCodes = listOf(invoicingCode),
                        author = userId,
                        responsible = hcPartyId,
                        created = now,
                        modified = now,
                        careProviderType = "persphysician",
                        invoicePeriod = 0,
                        thirdPartyPaymentJustification = "0"
                )

                //The invoice must be completed with ids and delegations and created on the server
                createdInvoices.add(newInvoice)
                emit(newInvoice)
                invoices.add(newInvoice)
            }
        }
    }

    override fun removeCodes(userId: String, secretPatientKeys: Set<String>, serviceId: String, inputTarificationIds: List<String>): Flow<Invoice> = flow {
        val tarificationIds = inputTarificationIds.toMutableList()
        val user = userLogic.getUser(userId)
        if (user != null) {
            val invoices = listByHcPartyPatientSksUnsent(user.healthcarePartyId ?: throw IllegalArgumentException("The provided user must be linked to an hcp"), secretPatientKeys)
                    .filter { i -> i.invoicingCodes.any { ic -> serviceId == ic.serviceId && tarificationIds.contains(ic.tarificationId) } }
                    .toList().sortedWith(Comparator { a: Invoice, b: Invoice -> ((b.invoiceDate ?: 99999999999999L) as Long).compareTo(a.invoiceDate ?: 0L) })
            for (i in invoices) {
                var hasChanged = false
                val l: MutableList<InvoicingCode> = LinkedList(i.invoicingCodes)
                for (ic in i.invoicingCodes) {
                    if (tarificationIds.contains(ic.tarificationId)) {
                        l.remove(ic)
                        tarificationIds.remove(ic.tarificationId)
                        hasChanged = true
                    }
                }
                if (hasChanged) {
                    modifyInvoice(i.copy(invoicingCodes = l))?.let { emit(it) }
                }
            }
        }
    }

    override fun listAllHcpsByStatus(status: String, from: Long?, to: Long?, hcpIds: List<String>): Flow<Invoice> = flow {
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        emitAll(invoiceDAO.listAllHcpsByStatus(dbInstanceUri, groupId, status, from, to, hcpIds))
    }

    override suspend fun solveConflicts() {
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        val invoicesInConflict = invoiceDAO.listConflicts(dbInstanceUri, groupId).mapNotNull { invoiceDAO.get(dbInstanceUri, groupId, it.id, Option.CONFLICTS) }
        invoicesInConflict.collect { iv ->
            var modifiedInvoice = iv
            iv.conflicts?.mapNotNull { c: String -> invoiceDAO.get(dbInstanceUri, groupId, iv.id, c) }?.forEach { cp ->
                modifiedInvoice = modifiedInvoice.merge(cp)
                invoiceDAO.purge(dbInstanceUri, groupId, cp)
            }
            invoiceDAO.save(dbInstanceUri, groupId, modifiedInvoice)
        }
    }

    override suspend fun getTarificationsCodesOccurences(hcPartyId: String, minOccurences: Long): List<LabelledOccurence> {
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        return invoiceDAO.listTarificationsFrequencies(dbInstanceUri, groupId, hcPartyId)
                .filter { v -> v.value != null && v.value >= minOccurences }
                .map { v -> LabelledOccurence(v.key!!.components[1] as String, v.value) }
                .toList().sortedByDescending { it.occurence }
    }

    override fun listIdsByTarificationsByCode(hcPartyId: String, codeCode: String, startValueDate: Long, endValueDate: Long): Flow<String> = flow {
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        emitAll(invoiceDAO.listIdsByTarificationsByCode(dbInstanceUri, groupId, hcPartyId, codeCode, startValueDate, endValueDate))
    }

    override fun listInvoiceIdsByTarificationsByCode(hcPartyId: String, codeCode: String, startValueDate: Long, endValueDate: Long): Flow<String> = flow {
        val (dbInstanceUri, groupId) = sessionLogic.getInstanceAndGroupInformationFromSecurityContext()
        emitAll(invoiceDAO.listInvoiceIdsByTarificationsByCode(dbInstanceUri, groupId, hcPartyId, codeCode, startValueDate, endValueDate))
    }

    override fun filter(filter: FilterChain<Invoice>) = flow<Invoice> {
        val ids = filters.resolve(filter.getFilter()).toList()
        val invoices = getInvoices(ids)
        val predicate = filter.predicate
        emitAll(if (predicate != null) invoices.filter { predicate.apply(it) } else invoices)
    }

    override fun getGenericDAO(): InvoiceDAO {
        return invoiceDAO
    }
}
