package org.taktik.icure.services.external.rest.v1.mapper.embed

import org.mapstruct.Mapper
import org.taktik.icure.entities.embed.Employer
import org.taktik.icure.services.external.rest.v1.dto.embed.EmployerDto
@Mapper
interface EmployerMapper {
	fun map(employerDto: EmployerDto):Employer
	fun map(employer: Employer):EmployerDto
}
