package org.taktik.icure.services.external.rest.v1.mapper.samv2.embed

import org.mapstruct.Mapper
import org.taktik.icure.entities.samv2.embed.Wada
import org.taktik.icure.services.external.rest.v1.dto.samv2.embed.WadaDto
@Mapper
interface WadaMapper {
	fun map(wadaDto: WadaDto):Wada
	fun map(wada: Wada):WadaDto
}
