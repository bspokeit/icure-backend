package org.taktik.icure.services.external.rest.v1.dto.samv2.embed

import com.github.pozo.KotlinBuilder
@KotlinBuilder
import com.github.pozo.KotlinBuilder
@KotlinBuilder
data class AmppDto(
        override val from: Long? = null,
        override val to: Long? = null,
        val ctiExtended: String? = null,
        val isOrphan: Boolean = false,
        val leafletLink: SamTextDto? = null,
        val spcLink: SamTextDto? = null,
        val rmaPatientLink: SamTextDto? = null,
        val rmaProfessionalLink: SamTextDto? = null,
        val parallelCircuit: Int? = null,
        val parallelDistributor: String? = null,
        val packMultiplier: Short? = null,
        val packAmount: QuantityDto? = null,
        val packDisplayValue: String? = null,
        val status: AmpStatusDto? = null,
        val atcs: List<AtcDto> = listOf(),
        val crmLink: SamTextDto? = null,
        val deliveryModusCode: String? = null,
        val deliveryModus: SamTextDto? = null,
        val deliveryModusSpecification: SamTextDto? = null,
        val distributorCompany: CompanyDto? = null,
        val isSingleUse: Boolean? = null,
        val speciallyRegulated: Int? = null,
        val abbreviatedName: SamTextDto? = null,
        val prescriptionName: SamTextDto? = null,
        val note: SamTextDto? = null,
        val posologyNote: SamTextDto? = null,
        val noGenericPrescriptionReasons: List<SamTextDto>? = listOf(),
        val exFactoryPrice: Double? = null,
        val reimbursementCode: Int? = null,
        val definedDailyDose: QuantityDto? = null,
        val officialExFactoryPrice: Double? = null,
        val realExFactoryPrice: Double? = null,
        val pricingInformationDecisionDate: Long? = null,
        val components: List<AmppComponentDto?>? = null,
        val commercializations: List<CommercializationDto>? = null,
        val dmpps: List<DmppDto>? = null,
        val vaccineIndicationCodes: List<String>? = null
) : DataPeriodDto
