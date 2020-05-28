package org.taktik.icure.services.external.rest.v1.mapper.samv2.embed

import org.mapstruct.Mapper
import org.taktik.icure.entities.samv2.embed.Copayment
import org.taktik.icure.services.external.rest.v1.dto.samv2.embed.CopaymentDto
@Mapper
interface CopaymentMapper {
	fun map(copaymentDto: CopaymentDto):Copayment
	fun map(copayment: Copayment):CopaymentDto
}
