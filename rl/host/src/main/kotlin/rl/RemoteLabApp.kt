package rl

import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import kotlinx.datetime.Clock
import rl.jdbi.configureWithAppRequirements

@SpringBootApplication
class RemoteLabApp {
    /*
     * Loads environment variables from a .env file located in the shared domain directory.
     * This variables are used to configure the application domain restrictions.
     */
    /*
    val domainConfigs =
        dotenv {
            directory = "../internal/shared/domain"
            filename = ".env"
        }

     */

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

fun main(args: Array<String>) {
    runApplication<RemoteLabApp>(*args)
}