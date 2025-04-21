package isel.rl.core.host

import io.github.cdimascio.dotenv.dotenv
import isel.rl.core.domain.ApiKey
import isel.rl.core.domain.config.LaboratoriesDomainConfig
import isel.rl.core.http.pipeline.ApiKeyInterceptor
import isel.rl.core.repository.jdbi.configureWithAppRequirements
import kotlinx.datetime.Clock
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import kotlin.time.DurationUnit
import kotlin.time.toDuration

const val API_KEY = "API_KEY"

const val MIN_LENGTH_LABNAME = "MIN_LENGTH_LABNAME"
const val MAX_LENGTH_LABNAME = "MAX_LENGTH_LABNAME"
const val MIN_LENGTH_LABDESCRIPTION = "MIN_LENGTH_LABDESCRIPTION"
const val MAX_LENGTH_LABDESCRIPTION = "MAX_LENGTH_LABDESCRIPTION"
const val MIN_LABDURATION = "MIN_LABDURATION"
const val MAX_LABDURATION = "MAX_LABDURATION"
const val MIN_LABQUEUE_LIMIT = "MIN_LABQUEUE_LIMIT"
const val MAX_LABQUEUE_LIMIT = "MAX_LABQUEUE_LIMIT"

@SpringBootApplication(scanBasePackages = ["isel.rl.core"])
class RemoteLabApp {
    /**
     * Loads environment variables from a .env file located in the shared domain directory.
     * This variables are used to configure the application domain restrictions.
     */
    val domainConfigs =
        dotenv {
            directory = "../../internal/shared/domain"
            filename = ".env"
        }

    @Bean
    fun apiKeyInfo() =
        ApiKey(
            dotenv {
                directory = "../../internal/shared/secrets"
                filename = ".env"
            }[API_KEY]!!
        )

    @Bean
    fun laboratoryDomainConfig() =
        LaboratoriesDomainConfig(
            minLengthLabName = domainConfigs[MIN_LENGTH_LABNAME]!!.toInt(),
            maxLengthLabName = domainConfigs[MAX_LENGTH_LABNAME]!!.toInt(),
            minLengthLabDescription = domainConfigs[MIN_LENGTH_LABDESCRIPTION]!!.toInt(),
            maxLengthLabDescription = domainConfigs[MAX_LENGTH_LABDESCRIPTION]!!.toInt(),
            minLabDuration = domainConfigs[MIN_LABDURATION]!!.toLong().toDuration(DurationUnit.MINUTES),
            maxLabDuration = domainConfigs[MAX_LABDURATION]!!.toLong().toDuration(DurationUnit.MINUTES),
            minLabQueueLimit = domainConfigs[MIN_LABQUEUE_LIMIT]!!.toInt(),
            maxLabQueueLimit = domainConfigs[MAX_LABQUEUE_LIMIT]!!.toInt(),
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
    val apiKeyInterceptor: ApiKeyInterceptor
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(apiKeyInterceptor)
    }
}

fun main(args: Array<String>) {
    runApplication<RemoteLabApp>(*args)
}
