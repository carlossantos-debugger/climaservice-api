package com.climaservice.api.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ClimaServiceDatabaseIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void deveAplicarFlywayEInicializarBancoCorretamente() throws Exception {

        try (Connection connection = dataSource.getConnection();

             PreparedStatement migrationStatement = connection.prepareStatement("""
                     SELECT COUNT(*)
                     FROM flyway_schema_history
                     WHERE version = '1'
                       AND success = true
                     """);

             PreparedStatement tabelaStatement = connection.prepareStatement("""
                     SELECT COUNT(*)
                     FROM information_schema.tables
                     WHERE table_schema = 'public'
                       AND table_name = 'pagamento'
                     """)) {

            ResultSet migrationResult = migrationStatement.executeQuery();

            migrationResult.next();

            int migrations = migrationResult.getInt(1);

            ResultSet tabelaResult = tabelaStatement.executeQuery();

            tabelaResult.next();

            int tabelasPagamento = tabelaResult.getInt(1);

            assertTrue(migrations >= 1);

            assertEquals(1, tabelasPagamento);
        }
    }
}