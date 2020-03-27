package org.taktik.icure.config

import com.fasterxml.classmate.ResolvedType
import com.fasterxml.classmate.TypeResolver
import com.fasterxml.classmate.types.ResolvedArrayType
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.method.HandlerMethod
import org.springframework.web.server.WebSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.OperationBuilderPlugin
import springfox.documentation.spi.service.ParameterBuilderPlugin
import springfox.documentation.spi.service.contexts.OperationContext
import springfox.documentation.spi.service.contexts.ParameterContext
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.spring.web.readers.operation.HandlerMethodResolver


@Configuration
class SwaggerConfig {
    /*
    @Bean
    @Primary
    fun fluxMethodResolver(resolver: TypeResolver?) = object : HandlerMethodResolver(resolver) {
        override fun methodReturnType(handlerMethod: HandlerMethod): ResolvedType {
            var retType = super.methodReturnType(handlerMethod)
            // we unwrap Mono, Flux, and as a bonus - ResponseEntity
            while (retType.erasedType == Mono::class.java || retType.erasedType == Flux::class.java || retType.erasedType == ResponseEntity::class.java) {
                retType = if (retType.erasedType == Flux::class.java) { // treat it as an array
                    val type = retType.typeBindings.getBoundType(0)
                    ResolvedArrayType(type.erasedType, type.typeBindings, type)
                } else {
                    retType.typeBindings.getBoundType(0)
                }
            }
            return retType
        }
    }

    @Bean
    @Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER)
    fun customParameterBuilderPlugin() = object : ParameterBuilderPlugin {
        override fun apply(parameterContext: ParameterContext) {
            parameterContext.parameterBuilder().order(parameterContext.resolvedMethodParameter().parameterIndex)
        }
        override fun supports(delimiter: DocumentationType) = true
    }

    @Bean
    fun api(): Docket {
        val securityReference = SecurityReference.builder()
                .reference("basicAuth")
                .scopes(arrayOf<AuthorizationScope>())
                .build()

        val securityContexts = listOf(SecurityContext.builder().securityReferences(listOf(securityReference)).build())

        val auth = listOf(BasicAuth("basicAuth"))
        return Docket(DocumentationType.SWAGGER_2)
                .apiInfo(ApiInfo("iCure Cloud API Documentation", "", "1.0", "", Contact("Antoine Duchâteau", "", ""), "", "", listOf()))
                .securitySchemes(auth)
                .securityContexts(securityContexts)
                .consumes(setOf(MediaType.APPLICATION_JSON_VALUE))
                .produces(setOf(MediaType.APPLICATION_JSON_VALUE))
                .ignoredParameterTypes(ServerHttpRequest::class.java, WebSession::class.java)
                .tags(
                        Tag("accesslog", "Access logs base API"),
                        Tag("code", "Codes CRUD and advanced API"),
                        Tag("contact", "Contacts CRUD and advanced API"),
                        Tag("document", "Documents CRUD and advanced API"),
                        Tag("entitytemplate", "Entity templates CRUD and advanced API"),
                        Tag("doctemplate", "Entity templates CRUD and advanced API"),
                        Tag("filter", "Entity templates CRUD and advanced API"),
                        Tag("form", "Forms CRUD and advanced API"),
                        Tag("generic", "iCure generic actions API"),
                        Tag("group", "Practice groups API"),
                        Tag("hcparty", "Healthcare parties CRUD and advanced API"),
                        Tag("helement", "Health elements CRUD and advanced API"),
                        Tag("icure", "iCure application basic API"),
                        Tag("insurance", "Insurances CRUD and advanced API"),
                        Tag("invoice", "Invoices CRUD and advanced API"),
                        Tag("auth", "Authentification API"),
                        Tag("message", "Messages CRUD and advanced API"),
                        Tag("patient", "Patients CRUD and advanced API"),
                        Tag("replication", "Replication API"),
                        Tag("tarification", "Tarifications CRUD and advanced API"),
                        Tag("technicaladmin", "Technical internal API"),
                        Tag("user", "Users CRUD and advanced API"),
                        Tag("be_drugs", "API for belgian Drugs service"),
                        Tag("be_mikrono", "API for belgian Mikrono service"),
                        Tag("be_progenda", "API for belgian Progenda service"),
                        Tag("be_kmehr", "API for belgian Kmehr service"),
                        Tag("be_result_import", "API for belgian Result_import service"),
                        Tag("be_result_export", "API for belgian Result_export service")
                )
                .select().apis(
                        RequestHandlerSelectors.basePackage("org.taktik.icure.services.external.rest.v1.controllers.be")
                                .or(RequestHandlerSelectors.basePackage("org.taktik.icure.services.external.rest.v1.controllers.core"))
                                .or(RequestHandlerSelectors.basePackage("org.taktik.icure.services.external.rest.v1.controllers.support"))
                )
                .paths(PathSelectors.any()).build()
    }
    */

    @Bean
    fun springShopOpenAPI(): OpenAPI? {
        return OpenAPI()
                .components(Components()
                        .addSecuritySchemes("basicScheme", SecurityScheme()
                                .type(SecurityScheme.Type.HTTP).scheme("basic")))
                .info(Info().title("iCure Cloud API Documentation")
                .description("Spring shop sample application")
                .version("v0.0.1"))
                .addSecurityItem(SecurityRequirement().addList("basicScheme"))
    }



}