package isel.rl.core.host

import io.github.cdimascio.dotenv.dotenv
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
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import kotlin.time.DurationUnit
import kotlin.time.toDuration

const val API_KEY = "API_KEY"
const val JWT_SECRET = "JWT_SECRET"

@SpringBootApplication(scanBasePackages = ["isel.rl.core"])
class RemoteLabApp {
    val privateDirectory: String
        get() =
            if (System.getenv("TEST_MODE") == "true") {
                "../../private"
            } else {
                "../private"
            }

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
    val domainConfigs = DomainConfig.parseDomainConfigs(
        loadDomainConfigFile()
    )

    /**
     * Creates a Sha256TokenEncoder bean.
     *
     * @return the Sha256TokenEncoder instance
     */
    @Bean
    fun tokenEncoder() = Sha256TokenEncoder()

    @Bean
    fun secrets(): Secrets {
        val secrets =
            dotenv {
                directory = "$privateDirectory/shared/secrets"
                filename = ".env"
            }

        return Secrets(
            apiKey = secrets[API_KEY]!!,
            jwtSecret = secrets[JWT_SECRET]!!,
        )
    }

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
    @Bean
    fun jdbi() =
        Jdbi.create(
            PGSimpleDataSource().apply {
                setURL(Environment.getDbUrl())
            },
        ).configureWithAppRequirements()
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

fun main(args: Array<String>) {
    runApplication<RemoteLabApp>(*args)
}
