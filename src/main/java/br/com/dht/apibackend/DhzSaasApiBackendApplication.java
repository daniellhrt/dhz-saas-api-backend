/**
 * Propósito: Classe de inicialização da aplicação Spring Boot.
 * Responsabilidade: Bootstrap da API e configuração do contexto Spring.
 * Papel na Arquitetura: Entrypoint.
 */
package br.com.dht.apibackend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class DhzSaasApiBackendApplication {

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
	}

	public static void main(String[] args) {
		SpringApplication.run(DhzSaasApiBackendApplication.class, args);
	}

}
