package isel.rl.core.host

import isel.rl.core.domain.Secrets
import isel.rl.core.domain.config.DomainConfig
import isel.rl.core.domain.config.GroupsDomainConfig
import isel.rl.core.domain.config.LaboratoriesDomainConfig
import isel.rl.core.domain.config.UsersDomainConfig
import isel.rl.core.domain.user.token.Sha256TokenEncoder
import isel.rl.core.http.pipeline.AuthenticatedUserArgumentResolver
import isel.rl.core.http.pipeline.interceptors.ApiKeyInterceptor
import isel.rl.core.http.pipeline.interceptors.AuthenticationInterceptor
import isel.rl.core.repository.jdbi.configureWithAppRequirements
import kotlinx.datetime.Clock
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import kotlin.time.DurationUnit
import kotlin.time.toDuration


@SpringBootApplication(scanBasePackages = ["isel.rl.core"])
class RemoteLabApp {
    /**
     * Helper function to load the domain-config.json file from the resources directory.
     */
    private final fun loadDomainConfigFile(): String {
        val inputStream = object {}.javaClass.getResourceAsStream("/domain-config.json")
        return inputStream?.bufferedReader()?.use { it.readText() }
            ?: throw Exception("Unable to load domain-config.json")
    }

    /**
     * Loads environment variables from a .env file located in the shared domain directory.
     * This variables are used to configure the application domain restrictions.
     */
    val domainConfigs =
        DomainConfig.parseDomainConfigs(
            loadDomainConfigFile(),
        )

    /**
     * Creates a Sha256TokenEncoder bean.
     *
     * @return the Sha256TokenEncoder instance
     */
    @Bean
    fun tokenEncoder() = Sha256TokenEncoder()

    @Value("\${api.key}")
    private lateinit var apiKey: String

    @Bean
    fun secrets() =
        Secrets(
            apiKey = apiKey,
        )

    @Bean
    fun usersDomainConfig(): UsersDomainConfig {
        val usersConfig = domainConfigs.user
        val tokenTtlDurationUnit = DurationUnit.valueOf(usersConfig.tokenTtlDurationUnit)

        return UsersDomainConfig(
            tokenSizeInBytes = usersConfig.tokenSizeInBytes,
            tokenTtl = usersConfig.tokenTtl.toDuration(tokenTtlDurationUnit),
            tokenRollingTtl = usersConfig.tokenRollingTtl.toDuration(tokenTtlDurationUnit),
            maxTokensPerUser = usersConfig.maxTokensPerUser,
        )
    }

    @Bean
    fun laboratoriesDomainConfig(): LaboratoriesDomainConfig {
        val labsConfig = domainConfigs.laboratory
        val labDurationUnit = DurationUnit.valueOf(labsConfig.labDurationUnit)

        return LaboratoriesDomainConfig(
            minLengthLabName = labsConfig.minLengthLabName,
            maxLengthLabName = labsConfig.maxLengthLabName,
            minLengthLabDescription = labsConfig.minLengthLabDescription,
            maxLengthLabDescription = labsConfig.maxLengthLabDescription,
            minLabDuration = labsConfig.minLabDuration.toDuration(labDurationUnit),
            maxLabDuration = labsConfig.maxLabDuration.toDuration(labDurationUnit),
            minLabQueueLimit = labsConfig.minLabQueueLimit,
            maxLabQueueLimit = labsConfig.maxLabQueueLimit,
            labDurationUnit = labsConfig.labDurationUnit,
        )
    }

    @Bean
    fun groupsDomainConfig() =
        GroupsDomainConfig(
            minLengthGroupName = domainConfigs.group.minLengthGroupName,
            maxLengthGroupName = domainConfigs.group.maxLengthGroupName,
            minLengthGroupDescription = domainConfigs.group.minLengthGroupDescription,
            maxLengthGroupDescription = domainConfigs.group.maxLengthGroupDescription,
        )

    /**
     * Creates a Clock bean.
     *
     * @return the Clock instance
     */
    @Bean
    fun clock() = Clock.System

    /**
     * Creates and configures a Jdbi instance.
     *
     * @return the configured Jdbi instance
     */
    @Value("\${db.url}")
    private lateinit var dbURL: String

    @Bean
    fun jdbi() =
        Jdbi.create(
            PGSimpleDataSource().apply {
                setURL(dbURL)
            },
        ).configureWithAppRequirements(
            laboratoriesDomainConfig(),
        )
}

@Configuration
class PipelineConfigurer(
    val apiKeyInterceptor: ApiKeyInterceptor,
    val authenticationInterceptor: AuthenticationInterceptor,
    val authenticatedUserArgumentResolver: AuthenticatedUserArgumentResolver,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(apiKeyInterceptor)
        registry.addInterceptor(authenticationInterceptor)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authenticatedUserArgumentResolver)
    }
}

@Configuration
class CorsConfig {
    @Bean
    fun corsFilter(): CorsFilter {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()


        // Allow all origins for development
        config.addAllowedOrigin("http://localhost:3000")


        // Allow all HTTP methods (GET, POST, PUT, DELETE, etc.)
        config.addAllowedMethod("*")


        // Allow all headers
        config.addAllowedHeader("*")


        // Allow credentials (cookies, authorization headers, etc.)
        config.allowCredentials = true

        source.registerCorsConfiguration("/**", config)
        return CorsFilter(source)
    }
}

fun main(args: Array<String>) {
    runApplication<RemoteLabApp>(*args)
}
