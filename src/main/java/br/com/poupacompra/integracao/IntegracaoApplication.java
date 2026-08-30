package br.com.poupacompra.integracao;

import java.util.Locale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "Integração Poupacompra",
        version = "1.0",
        description = "API de integração do sistema Poupacompra"
))
@SecurityScheme(
  name = "bearerAuth",
  type = SecuritySchemeType.HTTP,
  bearerFormat = "JWT",
  scheme = "bearer",
  description = "Access token JWT enviado no header Authorization"
)
public class IntegracaoApplication {

    public static void main(String[] args) {
				log.info("Iniciando aplicação");
        SpringApplication.run(IntegracaoApplication.class, args);
    }

		@PostConstruct
		public void setup() {
			configuracoesPadroes();
		}

    private static void configuracoesPadroes() {
        log.info("Configurando padrões de data e hora");
        Locale.setDefault(new Locale.Builder()
                .setLanguage("pt")
                .setRegion("BR")
								.build());
    }
}
