package com.recipes

import com.zaxxer.hikari.HikariDataSource
import org.springframework.context.annotation.Bean
import javax.sql.DataSource
import org.springframework.context.annotation.Configuration
import org.testcontainers.containers.PostgreSQLContainer

@Configuration
class TestConfig {
    companion object {
        private val postgres = PostgreSQLContainer("postgres:16-alpine")
            .apply {
                start()
            }
    }

    @Bean
    fun databaseInitializer(): DataSource =
        HikariDataSource().apply {
            jdbcUrl = postgres.jdbcUrl
            username = postgres.username
            password = postgres.password
        }
}
