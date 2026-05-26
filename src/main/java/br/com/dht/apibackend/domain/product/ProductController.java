/**
 * Propósito: Expor as rotas HTTP para gerenciamento de produtos.
 * Responsabilidade: Receber as chamadas REST, acionar o serviço apropriado e retornar respostas paginadas ou unitárias.
 * Papel na Arquitetura: Interface Adapters / Controller.
 */
package br.com.dht.apibackend.domain.product;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> list(Pageable pageable) {
        return ResponseEntity.ok(productService.list(pageable));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> create(@RequestBody @Valid ProductDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> update(@PathVariable UUID id, @RequestBody @Valid ProductDTO dto) {
        return ResponseEntity.ok(productService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
