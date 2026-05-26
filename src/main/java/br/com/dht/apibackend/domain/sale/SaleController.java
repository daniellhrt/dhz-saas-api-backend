/**
 * Propósito: Expor as rotas HTTP para gerenciamento de vendas (comandas).
 * Responsabilidade: Receber as chamadas REST de finalização de agendamentos e vendas diretas.
 * Papel na Arquitetura: Interface Adapters / Controller.
 */
package br.com.dht.apibackend.domain.sale;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @GetMapping
    public ResponseEntity<Page<SaleDTO>> list(Pageable pageable) {
        return ResponseEntity.ok(saleService.list(pageable));
    }

    @PostMapping
    public ResponseEntity<SaleDTO> create(@RequestBody @Valid SaleDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saleService.create(dto));
    }

    @DeleteMapping("/appointment/{appointmentId}")
    public ResponseEntity<Void> deleteByAppointmentId(@PathVariable java.util.UUID appointmentId) {
        saleService.deleteSaleByAppointmentId(appointmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<SaleDTO> getByAppointmentId(@PathVariable java.util.UUID appointmentId) {
        return ResponseEntity.ok(saleService.getByAppointmentId(appointmentId));
    }
}
