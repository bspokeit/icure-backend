package org.taktik.icure.asyncdao.replicator

import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.slf4j.LoggerFactory
import org.taktik.couchdb.Client
import org.taktik.couchdb.get
import org.taktik.icure.asyncdao.UserDAO
import org.taktik.icure.entities.Group
import org.taktik.icure.entities.User
import org.taktik.icure.properties.CouchDbProperties
import java.net.URI

@FlowPreview
@ExperimentalCoroutinesApi
class UserReplicator(private val couchDbProperties: CouchDbProperties, sslContextFactory: SslContextFactory, private val userDAO: UserDAO) : EntityReplicator<User>(sslContextFactory) {
    private val log = LoggerFactory.getLogger(EntityReplicator::class.java)

    override val entityType: Class<User>
        get() = User::class.java

    override suspend fun prepareReplication(client: Client, group: Group) = withContext(IO) {
        //TODO userDAO.initStandardDesignDocument(group)
    }

    override suspend fun replicate(client: Client, group: Group, entityIds: Flow<IdAndRev>): Flow<IdAndRev> {
        val dbInstanceUri = URI(couchDbProperties.url)
        return entityIds.onEach { idAndRev ->
            withContext(IO) {
                val userId = idAndRev.id
                val from = checkNotNull(client.get<User>(userId))
                var to: User? = userDAO.getOnFallback(dbInstanceUri, group.id + ":" + userId, true)

                if (to == null) {
                    to = User()
                    to.id = group.id + ":" + from.id
                    log.info("User ${to.id} not found on fallback: Creating !")
                }
                if (
                        to.status != from.status ||
                        to.isUse2fa != from.isUse2fa ||
                        to.passwordHash != from.passwordHash ||
                        to.healthcarePartyId != from.healthcarePartyId ||
                        (!from.isSecretEmpty && to.secret != from.secret) ||
                        to.login != from.login ||
                        to.applicationTokens != from.applicationTokens ||
                        to.groupId != group.id
                ) {
                    to.status = from.status
                    to.isUse2fa = from.isUse2fa
                    to.passwordHash = from.passwordHash
                    to.healthcarePartyId = from.healthcarePartyId
                    to.secret = if (from.isSecretEmpty) null else from.secret
                    to.login = from.login
                    to.applicationTokens = from.applicationTokens
                    to.groupId = group.id

                    userDAO.saveOnFallback(dbInstanceUri, to)
                }
            }
        }
    }
}
