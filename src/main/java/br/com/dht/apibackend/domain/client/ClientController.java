/**
 * Propósito: Ponto de entrada REST para o domínio de Clientes.
 * Responsabilidade: Receber requisições HTTP e rotear para o Service.
 * Papel na Arquitetura: Domain / Controller.
 */
package br.com.dht.apibackend.domain.client;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientDTO.Response> create(@RequestBody @Valid ClientDTO.Request request) {

        ClientDTO.Response response = clientService.createClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ClientDTO.Response>> listAll() {

        return ResponseEntity.ok(clientService.listAllClients());
    }
}