/**
 * Propósito: Regras de negócio relacionadas aos produtos.
 * Responsabilidade: Operações CRUD isoladas por Tenant, verificação e atualização de estoque.
 * Papel na Arquitetura: Domain / Service.
 */
package br.com.dht.apibackend.domain.product;

import br.com.dht.apibackend.config.TenantContext;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Page<ProductDTO> list(Pageable pageable) {
        String tenantId = TenantContext.getTenantId();
        return productRepository.findByTenantId(tenantId, pageable)
                .map(ProductDTO::new);
    }

    @Transactional
    public ProductDTO create(ProductDTO dto) {
        String tenantId = TenantContext.getTenantId();
        Product product = new Product(
                tenantId,
                dto.getName(),
                dto.getDescription(),
                dto.getPrice(),
                dto.getStockQuantity()
        );
        return new ProductDTO(productRepository.save(product));
    }

    @Transactional
    public ProductDTO update(UUID id, ProductDTO dto) {
        String tenantId = TenantContext.getTenantId();
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
        
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        
        if (dto.getActive() != null) {
            product.setActive(dto.getActive());
        }
        
        return new ProductDTO(productRepository.save(product));
    }

    @Transactional
    public void delete(UUID id) {
        String tenantId = TenantContext.getTenantId();
        Product product = productRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
        
        // Em um cenário real de vendas, exclusão lógica é preferida
        product.setActive(false);
        productRepository.save(product);
    }
}
