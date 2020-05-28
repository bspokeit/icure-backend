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

package org.taktik.icure.services.external.rest.v1.controllers.support

import com.squareup.moshi.Moshi
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.Operation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.reactor.asCoroutineContext
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.withContext
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpMethod
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.WebSession
import org.taktik.icure.asynclogic.AsyncSessionLogic
import org.taktik.icure.security.SecurityToken
import org.taktik.icure.services.external.rest.v1.dto.AuthenticationResponse
import org.taktik.icure.services.external.rest.v1.dto.LoginCredentials
import org.taktik.icure.spring.asynccache.AsyncCacheManager
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.lang.IllegalArgumentException
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.coroutines.CoroutineContext


@ExperimentalCoroutinesApi
@RestController
@RequestMapping("/rest/v1/auth")
@Tag(name = "auth")
class LoginController(private val mapper: MapperFacade, private val sessionLogic: AsyncSessionLogic, asyncCacheManager: AsyncCacheManager) {
    val cache = asyncCacheManager.getCache<String, SecurityToken>("spring.security.tokens")

    @Operation(summary = "login", description = "Login using username and password")
    @PostMapping("/login")
    fun login(request : ServerHttpRequest, session: WebSession) = mono {
        val body: Flux<DataBuffer> = request.body
        val bodyText =
        body.awaitFirstOrNull()?.let { buffer: DataBuffer ->
            val charBuffer: CharBuffer = StandardCharsets.UTF_8.decode(buffer.asByteBuffer())
            DataBufferUtils.release(buffer)
            charBuffer.toString()
        } ?: throw IllegalArgumentException("Missing body")

        withContext(Dispatchers.Default) {
            val loginInfo = Moshi.Builder().build().adapter(LoginCredentials::class.java).fromJson(bodyText)
            return@withContext loginInfo?.let {
                val response = AuthenticationResponse()
                val authentication = sessionLogic.login(loginInfo.username!!, loginInfo.password!!, request, session)
                response.isSuccessful = authentication != null && authentication.isAuthenticated
                if (response.isSuccessful) {
                    val secContext =  SecurityContextImpl(authentication)
                    val securityContext = kotlin.coroutines.coroutineContext[ReactorContext]?.context?.put(SecurityContext::class.java, Mono.just(secContext))
                    withContext(kotlin.coroutines.coroutineContext.plus(securityContext?.asCoroutineContext() as CoroutineContext)){
                        response.healthcarePartyId = sessionLogic.getCurrentHealthcarePartyId()
                        response.username = loginInfo.username

                        session.attributes["SPRING_SECURITY_CONTEXT"] = secContext
                    }
                }
                mapper.map(response, AuthenticationResponse::class.java)
            } ?: throw BadCredentialsException("bad credentials")
        }
    }

    @Operation(summary = "logout", description = "Logout")
    @GetMapping("/logout")
    fun logout() = mono {
        sessionLogic.logout()
        mapper.map(AuthenticationResponse(true), AuthenticationResponse::class.java)
    }

    @Operation(summary = "logout", description = "Logout")
    @PostMapping("/logout")
    fun logoutPost() = mono {
        sessionLogic.logout()
        mapper.map(AuthenticationResponse(true), AuthenticationResponse::class.java)
    }

    @Operation(summary = "token", description = "Get token for subsequent operation")
    @GetMapping("/token/{method}/{path}")
    fun token(@PathVariable method: String, @PathVariable path: String) = mono {
        val token = UUID.randomUUID().toString()
        cache.put(token, SecurityToken(HttpMethod.valueOf(method), path, sessionLogic.getCurrentSessionContext().getAuthentication()))
        token
    }

}
