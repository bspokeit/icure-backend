package org.taktik.icure.asynclogic

import kotlinx.coroutines.flow.Flow
import org.ektorp.ComplexKey
import org.taktik.couchdb.DocIdentifier
import org.taktik.couchdb.ViewQueryResultEvent
import org.taktik.icure.asyncdao.ContactDAO
import org.taktik.icure.db.PaginatedList
import org.taktik.icure.db.PaginationOffset
import org.taktik.icure.dto.data.LabelledOccurence
import org.taktik.icure.dto.filter.chain.FilterChain
import org.taktik.icure.entities.Contact
import org.taktik.icure.entities.embed.Delegation
import org.taktik.icure.entities.embed.Service
import org.taktik.icure.validation.aspect.Check

interface ContactLogic : EntityPersister<Contact, String> {
    suspend fun getContact(id: String): Contact?
    fun getContacts(selectedIds: Collection<String>): Flow<Contact>
    fun getPaginatedContacts(selectedIds: Collection<String>, paginationOffset: PaginationOffset<List<String>>): Flow<ViewQueryResultEvent>
    fun findByHCPartyPatient(hcPartyId: String, secretPatientKeys: List<String>): Flow<Contact>

    suspend fun addDelegation(contactId: String, delegation: Delegation): Contact?

    suspend fun createContact(@Check contact: Contact): Contact?
    fun deleteContacts(ids: Set<String>): Flow<DocIdentifier>

    suspend fun modifyContact(@Check contact: Contact): Contact?
    fun getServices(selectedServiceIds: Collection<String>): Flow<Service>

    fun pimpServiceWithContactInformation(s: Service, c: Contact): Service
    fun listServiceIdsByTag(hcPartyId: String, patientSecretForeignKeys: List<String>?, tagType: String, tagCode: String, startValueDate: Long, endValueDate: Long): Flow<String>
    fun listServiceIdsByCode(hcPartyId: String, patientSecretForeignKeys: List<String>?, codeType: String, codeCode: String, startValueDate: Long, endValueDate: Long): Flow<String>
    fun listContactIds(hcPartyId: String): Flow<String>
    fun findByServices(services: Collection<String>): Flow<String>
    fun findServicesBySecretForeignKeys(hcPartyId: String, patientSecretForeignKeys: Set<String>): Flow<String>
    fun findContactsByHCPartyFormId(hcPartyId: String, formId: String): Flow<Contact>

    suspend fun getServiceCodesOccurences(hcPartyId: String, codeType: String, minOccurences: Long): List<LabelledOccurence>
    fun findContactsByHCPartyFormIds(hcPartyId: String, ids: List<String>): Flow<Contact>
    fun getGenericDAO(): ContactDAO
    suspend fun filterContacts(paginationOffset: PaginationOffset<List<String>>, filter: FilterChain<Contact>): Flow<ViewQueryResultEvent>

    suspend fun solveConflicts()
    fun listContactsByOpeningDate(hcPartyId: String, startOpeningDate: Long, endOpeningDate: Long, offset: PaginationOffset<ComplexKey>): Flow<ViewQueryResultEvent>
}