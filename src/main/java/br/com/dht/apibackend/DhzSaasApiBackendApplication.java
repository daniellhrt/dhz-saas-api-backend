/**
 * Propósito: Classe de inicialização da aplicação Spring Boot.
 * Responsabilidade: Bootstrap da API e configuração do contexto Spring.
 * Papel na Arquitetura: Entrypoint.
 */
package br.com.dht.apibackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DhzSaasApiBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(DhzSaasApiBackendApplication.class, args);
	}

}
