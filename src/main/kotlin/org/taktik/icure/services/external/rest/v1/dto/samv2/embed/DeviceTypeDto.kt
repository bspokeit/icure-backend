package org.taktik.icure.services.external.rest.v1.dto.samv2.embed

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.github.pozo.KotlinBuilder
import java.io.Serializable

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@KotlinBuilder
data class DeviceTypeDto(
        val code: String? = null,
        val name: SamTextDto? = null,
        val edqmCode: String? = null,
        val edqmDefinition: String? = null
) : Serializable
