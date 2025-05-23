package org.mariafelecan.pricecomparatormarket.Repository;

import org.mariafelecan.pricecomparatormarket.Domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Product findByProductId(String productId);
    Optional<Product> findByNameIgnoreCaseAndBrandIgnoreCase(String name, String brand); // Potentially needed for CSV
    List<Product> findByCategoryIgnoreCase(String category);
    List<Product> findByBrandIgnoreCase(String brand);
    List<Product> findByNameContainingIgnoreCase(String name);
}
