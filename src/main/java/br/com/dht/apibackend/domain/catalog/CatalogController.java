/**
 * Propósito: Ponto de entrada REST para o Catálogo de Serviços.
 * Papel na Arquitetura: Domain / Controller.
 */
package br.com.dht.apibackend.domain.catalog;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @PostMapping
    public ResponseEntity<ServiceItemDTO.Response> create(@RequestBody @Valid ServiceItemDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createService(request));
    }

    @GetMapping
    public ResponseEntity<List<ServiceItemDTO.Response>> listActive() {
        return ResponseEntity.ok(catalogService.listActiveServices());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        catalogService.deactivateService(id);
        return ResponseEntity.noContent().build();
    }
}