package ar.edu.utn.frc.tup.piii.integration.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

/**
 * Configuración específica para tests de integración.
 * Se encarga de cargar los datos de prueba en la base de datos.
 */
@TestConfiguration
@Profile("integration")
public class IntegrationTestConfig {

    /**
     * Inicializa la base de datos con datos específicos para tests de integración.
     */
    @Bean
    @Primary
    public DataSourceInitializer dataSourceInitializer(DataSource dataSource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("test-data.sql"));
        populator.setContinueOnError(false);

        initializer.setDatabasePopulator(populator);
        initializer.setEnabled(true);

        return initializer;
    }
}