package isel.rl.core.host

import io.github.cdimascio.dotenv.dotenv
import isel.rl.core.domain.Secrets
import isel.rl.core.domain.config.GroupsDomainConfig
import isel.rl.core.domain.config.LaboratoriesDomainConfig
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

// Laboratory domain restrictions
const val MIN_LENGTH_LAB_NAME = "MIN_LENGTH_LAB_NAME"
const val MAX_LENGTH_LAB_NAME = "MAX_LENGTH_LAB_NAME"
const val MIN_LENGTH_LAB_DESCRIPTION = "MIN_LENGTH_LAB_DESCRIPTION"
const val MAX_LENGTH_LAB_DESCRIPTION = "MAX_LENGTH_LAB_DESCRIPTION"
const val MIN_LAB_DURATION = "MIN_LAB_DURATION"
const val MAX_LAB_DURATION = "MAX_LAB_DURATION"
const val MIN_LAB_QUEUE_LIMIT = "MIN_LAB_QUEUE_LIMIT"
const val MAX_LAB_QUEUE_LIMIT = "MAX_LAB_QUEUE_LIMIT"

// Group domain restrictions
const val MIN_LENGTH_GROUP_NAME = "MIN_LENGTH_GROUP_NAME"
const val MAX_LENGTH_GROUP_NAME = "MAX_LENGTH_GROUP_NAME"
const val MIN_LENGTH_GROUP_DESCRIPTION = "MIN_LENGTH_GROUP_DESCRIPTION"
const val MAX_LENGTH_GROUP_DESCRIPTION = "MAX_LENGTH_GROUP_DESCRIPTION"

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
     * Loads environment variables from a .env file located in the shared domain directory.
     * This variables are used to configure the application domain restrictions.
     */
    val domainConfigs =
        dotenv {
            directory = "$privateDirectory/shared/domain"
            filename = ".env"
        }

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
    fun laboratoriesDomainConfig() =
        LaboratoriesDomainConfig(
            minLengthLabName = domainConfigs[MIN_LENGTH_LAB_NAME]!!.toInt(),
            maxLengthLabName = domainConfigs[MAX_LENGTH_LAB_NAME]!!.toInt(),
            minLengthLabDescription = domainConfigs[MIN_LENGTH_LAB_DESCRIPTION]!!.toInt(),
            maxLengthLabDescription = domainConfigs[MAX_LENGTH_LAB_DESCRIPTION]!!.toInt(),
            minLabDuration = domainConfigs[MIN_LAB_DURATION]!!.toLong().toDuration(DurationUnit.MINUTES),
            maxLabDuration = domainConfigs[MAX_LAB_DURATION]!!.toLong().toDuration(DurationUnit.MINUTES),
            minLabQueueLimit = domainConfigs[MIN_LAB_QUEUE_LIMIT]!!.toInt(),
            maxLabQueueLimit = domainConfigs[MAX_LAB_QUEUE_LIMIT]!!.toInt(),
        )

    @Bean
    fun groupsDomainConfig() =
        GroupsDomainConfig(
            minLengthGroupName = domainConfigs[MIN_LENGTH_GROUP_NAME]!!.toInt(),
            maxLengthGroupName = domainConfigs[MAX_LENGTH_GROUP_NAME]!!.toInt(),
            minLengthGroupDescription = domainConfigs[MIN_LENGTH_GROUP_DESCRIPTION]!!.toInt(),
            maxLengthGroupDescription = domainConfigs[MAX_LENGTH_GROUP_DESCRIPTION]!!.toInt(),
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
