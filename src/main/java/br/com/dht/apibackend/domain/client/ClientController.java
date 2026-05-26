/**
 * Propósito: Ponto de entrada REST para o domínio de Clientes.
 * Responsabilidade: Receber requisições HTTP e rotear para o Service.
 * Papel na Arquitetura: Domain / Controller.
 */
package br.com.dht.apibackend.domain.client;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
    public ResponseEntity<Page<ClientDTO.Response>> listAll(Pageable pageable) {
        return ResponseEntity.ok(clientService.listAllClients(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientDTO.Response> update(@PathVariable java.util.UUID id, @RequestBody @Valid ClientDTO.Request request) {
        return ResponseEntity.ok(clientService.updateClient(id, request));
    }
}