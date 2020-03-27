package org.taktik.icure.services.external.rest.v1.controllers.be

import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.Operation
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.mono
import ma.glasnost.orika.MapperFacade
import org.springframework.web.bind.annotation.*
import org.taktik.icure.entities.Invoice
import org.taktik.icure.asynclogic.*
import org.taktik.icure.services.external.rest.v1.dto.MapOfIdsDto
import java.util.*


@RestController
@RequestMapping("/rest/v1/be_efact")
@Tag(name = "beefact")
class EfactController(val mapper: MapperFacade,
                      val efactLogic: EfactLogic,
                      val sessionLogic: AsyncSessionLogic,
                      val healthcarePartyLogic: HealthcarePartyLogic,
                      val invoiceLogic: InvoiceLogic,
                      val patientLogic: PatientLogic,
                      val documentLogic: DocumentLogic,
                      val insuranceLogic: InsuranceLogic) {

    @Operation(summary = "create batch and message")
    @PostMapping("/{insuranceId}/{newMessageId}/{numericalRef}")
    fun createBatchAndMessage(@PathVariable insuranceId: String,
                              @PathVariable newMessageId: String,
                              @PathVariable numericalRef: Long,
                              @RequestBody ids: MapOfIdsDto) = mono {
        val hcp = healthcarePartyLogic.getHealthcareParty(sessionLogic.getCurrentHealthcarePartyId())
        val ins = insuranceLogic.getInsurance(insuranceId)

        val invoices = HashMap<String, List<Invoice>>()
        for ((key, value) in ids.mapOfIds) {
            invoices[key] = invoiceLogic.getInvoices(value).toList()
        }

        if (hcp != null && ins != null) {
            efactLogic.prepareBatch(newMessageId, numericalRef, hcp, ins, false, invoices)
        }else null
    }
}
