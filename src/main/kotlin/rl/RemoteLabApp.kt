package rl

import kotlinx.datetime.Instant
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import rl.domain.user.Email
import rl.domain.user.token.TokenValidationInfo
import rl.repositoryJdbi.mappers.EmailMapper
import rl.repositoryJdbi.mappers.InstantMapper
import rl.repositoryJdbi.mappers.TokenValidationInfoMapper

@SpringBootApplication
class RemoteLabApp {
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

fun Jdbi.configureWithAppRequirements(): Jdbi {
    installPlugin(KotlinPlugin())
    installPlugin(PostgresPlugin())

    registerColumnMapper(Email::class.java, EmailMapper())
    registerColumnMapper(Instant::class.java, InstantMapper())
    registerColumnMapper(TokenValidationInfo::class.java, TokenValidationInfoMapper())

    return this
}

fun main(args: Array<String>) {
    runApplication<RemoteLabApp>(*args)
}