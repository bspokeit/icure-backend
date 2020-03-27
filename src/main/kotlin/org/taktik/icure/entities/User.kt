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
package org.taktik.icure.entities

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.jboss.aerogear.security.otp.api.Base32
import org.taktik.icure.constants.Roles.VirtualHostDependency
import org.taktik.icure.constants.Users
import org.taktik.icure.entities.base.Principal
import org.taktik.icure.entities.base.StoredDocument
import org.taktik.icure.entities.embed.DelegationTag
import org.taktik.icure.entities.embed.Permission
import org.taktik.icure.utils.InstantDeserializer
import org.taktik.icure.utils.InstantSerializer
import java.io.Serializable
import java.time.Instant
import java.util.HashMap
import java.util.HashSet

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class User : StoredDocument(), Principal, Cloneable, Serializable, Cloneable {
    override var name: String? = null
    override var properties: Set<Property> = HashSet()
    override var permissions: Set<Permission> = HashSet()
    var type: Users.Type? = null
    var status: Users.Status? = null
    var login: String? = null
    var passwordHash: String? = null
    private var secret: String? = null
    var isUse2fa: Boolean? = null
    var groupId: String? = null
    var healthcarePartyId: String? = null
    var patientId: String? = null
    var autoDelegations: Map<DelegationTag, Set<String>> = HashMap() //DelegationTag -> healthcarePartyIds

    @JsonSerialize(using = InstantSerializer::class, include = JsonSerialize.Inclusion.NON_NULL)
    @JsonDeserialize(using = InstantDeserializer::class)
    var createdDate: Instant? = null

    @JsonSerialize(using = InstantSerializer::class, include = JsonSerialize.Inclusion.NON_NULL)
    @JsonDeserialize(using = InstantDeserializer::class)
    var lastLoginDate: Instant? = null

    @JsonSerialize(using = InstantSerializer::class, include = JsonSerialize.Inclusion.NON_NULL)
    @JsonDeserialize(using = InstantDeserializer::class)
    var expirationDate: Instant? = null
    var activationToken: String? = null

    @JsonSerialize(using = InstantSerializer::class, include = JsonSerialize.Inclusion.NON_NULL)
    @JsonDeserialize(using = InstantDeserializer::class)
    var activationTokenExpirationDate: Instant? = null
    var passwordToken: String? = null

    @JsonSerialize(using = InstantSerializer::class, include = JsonSerialize.Inclusion.NON_NULL)
    @JsonDeserialize(using = InstantDeserializer::class)
    var passwordTokenExpirationDate: Instant? = null

    @JsonSerialize(using = InstantSerializer::class, include = JsonSerialize.Inclusion.NON_NULL)
    @JsonDeserialize(using = InstantDeserializer::class)
    var termsOfUseDate: Instant? = null
    override var parents: Set<String> = HashSet()
        @JsonIgnore get() = field
        set
    var email: String? = null
    var applicationTokens: Map<String, String> = HashMap()

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Any {
        return super.clone()
    }

    @get:JsonIgnore
    override val virtualHostDependency: VirtualHostDependency
        get() = VirtualHostDependency.NONE

    @get:JsonIgnore
    override val virtualHosts: Set<String>?
        get() = null

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val user = o as User
        return !if (id != null) id != user.id else user.id != null
    }

    override fun hashCode(): Int {
        return if (id != null) id.hashCode() else 0
    }

    @get:JsonIgnore
    val isSecretEmpty: Boolean
        get() = secret == null

    fun getSecret(): String? {
        if (secret == null) {
            secret = Base32.random()
        }
        return secret
    }

    fun setSecret(secret: String?) {
        this.secret = secret
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}