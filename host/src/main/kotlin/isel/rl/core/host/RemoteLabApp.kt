package isel.rl.core.host

import isel.rl.core.domain.Secrets
import isel.rl.core.domain.config.DomainConfig
import isel.rl.core.domain.config.GroupsDomainConfig
import isel.rl.core.domain.config.HardwareDomainConfig
import isel.rl.core.domain.config.LaboratoriesDomainConfig
import isel.rl.core.domain.config.UsersDomainConfig
import isel.rl.core.domain.user.token.Sha256TokenEncoder
import isel.rl.core.http.pipeline.AuthenticatedUserArgumentResolver
import isel.rl.core.http.pipeline.interceptors.ApiKeyInterceptor
import isel.rl.core.http.pipeline.interceptors.AuthenticationInterceptor
import isel.rl.core.http.pipeline.interceptors.CheckRoleInterceptor
import isel.rl.core.repository.jdbi.configureWithAppRequirements
import kotlinx.datetime.Clock
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@SpringBootApplication(
    scanBasePackages = ["isel.rl.core"],
    exclude = [SecurityAutoConfiguration::class],
)
@EnableCaching
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
    fun domainConfig(): DomainConfig = domainConfigs

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
    fun laboratoriesDomainConfig(): LaboratoriesDomainConfig =
        LaboratoriesDomainConfig.from(
            config = domainConfigs.laboratory,
        )

    @Bean
    fun groupsDomainConfig() =
        GroupsDomainConfig.from(
            config = domainConfigs.group,
        )

    @Bean
    fun hardwareDomainConfig() =
        HardwareDomainConfig.from(
            config = domainConfigs.hardware,
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
        ).configureWithAppRequirements(domainConfigs)
}

@Configuration
data class PipelineConfigurer(
    val apiKeyInterceptor: ApiKeyInterceptor,
    val authenticationInterceptor: AuthenticationInterceptor,
    val checkRoleInterceptor: CheckRoleInterceptor,
    val authenticatedUserArgumentResolver: AuthenticatedUserArgumentResolver,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(apiKeyInterceptor)
        registry.addInterceptor(authenticationInterceptor)
        registry.addInterceptor(checkRoleInterceptor)
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authenticatedUserArgumentResolver)
    }
}

@Configuration
class RedisConfig {
    @Bean
    @Primary // Make this the primary connection factory
    fun redisConnectionFactory(): LettuceConnectionFactory {
        logger.info("LettuceConnection Factory initialized")

        val redisHost = System.getenv("SPRING_REDIS_HOST") ?: "localhost"
        val redisPort = System.getenv("SPRING_REDIS_PORT")?.toIntOrNull() ?: 6379

        logger.info("Connecting to Redis at $redisHost:$redisPort")

        val standaloneConfig = RedisStandaloneConfiguration(redisHost, redisPort)
        val connectionFactory = LettuceConnectionFactory(standaloneConfig)
        return connectionFactory
    }

    @Bean
    fun redisTemplate(): RedisTemplate<String, String> {
        val template = RedisTemplate<String, String>()
        template.connectionFactory = redisConnectionFactory()
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = StringRedisSerializer()
        return template
    }

    @Bean
    fun redisMessageListenerContainer(): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(redisConnectionFactory())
        return container
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RedisConfig::class.java)
    }
}

fun main(args: Array<String>) {
    runApplication<RemoteLabApp>(*args)
}
