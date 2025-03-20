package rl

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

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

    // TODO: Dont forget to implement Mappers if needed
    //registerColumnMapper(PasswordValidationInfo::class.java, PasswordValidationInfoMapper())

    return this
}

fun main(args: Array<String>) {
    runApplication<RemoteLabApp>(*args)
}