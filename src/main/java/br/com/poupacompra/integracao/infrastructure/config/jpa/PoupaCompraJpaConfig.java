package br.com.poupacompra.integracao.infrastructure.config.jpa;

import java.util.Objects;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import com.zaxxer.hikari.HikariDataSource;

import lombok.NoArgsConstructor;

@DependsOn("migrationService")
@NoArgsConstructor
@Configuration(proxyBeanMethods = false)
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "poupaCompraEntityManagerFactory",
  transactionManagerRef = "poupaCompraTransactionManager",
  enableDefaultTransactions = false
)
public class PoupaCompraJpaConfig {

      @SuppressWarnings("unchecked")
    protected static <T> T createDataSource(final DataSourceProperties properties, final Class<? extends DataSource> type) {
        return (T) properties.initializeDataSourceBuilder().type(type).build();
    }
    
    @Primary
    @Bean(name = "poupaCompraDataSourceProperties")
    @ConfigurationProperties("spring.datasource.poupacompra")
    DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }
    
    @Primary
    @Bean(name = "poupaCompraJpaProperties")
    @ConfigurationProperties("spring.datasource.poupacompra.jpa")
    JpaProperties jpaProperties() {
        return new JpaProperties();
    }   
    
    @Primary
    @Bean(name = "poupaCompraDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.poupacompra.hikari")
    HikariDataSource dataSource(final @Qualifier("poupaCompraDataSourceProperties") DataSourceProperties properties) {
        final HikariDataSource dataSource = createDataSource(properties, HikariDataSource.class);
        if (StringUtils.hasText(properties.getName())) {
            dataSource.setPoolName(properties.getName());
        }
        return dataSource;
    }
    
    @Primary
    @Bean(name = "poupaCompraEntityManagerFactory")
    LocalContainerEntityManagerFactoryBean entityManagerFactory(
        final EntityManagerFactoryBuilder builder,
        final @Qualifier("poupaCompraDataSource") DataSource dataSource,
        final @Qualifier("poupaCompraJpaProperties") JpaProperties jpaProperties) {
        return builder.dataSource(dataSource).properties(jpaProperties.getProperties()).packages("azulseguros.apidadosapolice.model.entity.poupacompra").persistenceUnit("poupacomprapu").build();
    }
    
    @Primary
    @Bean(name = "poupaCompraTransactionManager")
    PlatformTransactionManager customerTransactionManager(
        final @Qualifier("poupaCompraEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }
}
