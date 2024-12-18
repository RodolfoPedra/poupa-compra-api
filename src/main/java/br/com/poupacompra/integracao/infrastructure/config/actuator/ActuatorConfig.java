package br.com.poupacompra.integracao.infrastructure.config.actuator;

import javax.sql.DataSource;

import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.jdbc.DataSourceHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActuatorConfig {
  	  @Bean
	    HealthIndicator dbHealthIndicator(DataSource dataSource) {
	    DataSourceHealthIndicator indicator = new DataSourceHealthIndicator(dataSource);
	    indicator.setQuery("select 1;");
	    return indicator;
	}
}
