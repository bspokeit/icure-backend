package org.taktik.icure.services.external.rest.v1.mapper.samv2.embed

import org.mapstruct.Mapper
import org.taktik.icure.entities.samv2.embed.AmpComponent
import org.taktik.icure.services.external.rest.v1.dto.samv2.embed.AmpComponentDto
@Mapper
interface AmpComponentMapper {
	fun map(ampComponentDto: AmpComponentDto):AmpComponent
	fun map(ampComponent: AmpComponent):AmpComponentDto
}
