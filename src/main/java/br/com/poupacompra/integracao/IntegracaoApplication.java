package br.com.poupacompra.integracao;

import java.util.Locale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
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
