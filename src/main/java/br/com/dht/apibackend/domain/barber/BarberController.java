/**
 * Propósito: Ponto de entrada REST para o domínio de Barbeiros.
 * Responsabilidade: Receber requisições HTTP e rotear para o Service com validação de papel (role) e tenant.
 * Papel na Arquitetura: Domain / Controller.
 */
package br.com.dht.apibackend.domain.barber;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/barbers")
@RequiredArgsConstructor
public class BarberController {

    private final BarberService barberService;

    @PostMapping
    public ResponseEntity<BarberDTO.Response> create(@RequestBody @Valid BarberDTO.CreateRequest request) {
        BarberDTO.Response response = barberService.createBarber(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<BarberDTO.Response>> listAll(Pageable pageable) {
        return ResponseEntity.ok(barberService.listAllBarbers(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BarberDTO.Response> update(@PathVariable UUID id, @RequestBody @Valid BarberDTO.UpdateRequest request) {
        return ResponseEntity.ok(barberService.updateBarber(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        barberService.deleteBarber(id);
        return ResponseEntity.noContent().build();
    }
}
