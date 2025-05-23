package org.mariafelecan.pricecomparatormarket.Repository;

import org.mariafelecan.pricecomparatormarket.Domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByProductId(String productId);

    @Query("SELECT p FROM Product p WHERE LOWER(p.productId) = LOWER(:productIdParam)")
    Optional<Product> findByProductIdCustomCaseInsensitive(@Param("productIdParam") String productId);

    Optional<Product> findByNameIgnoreCaseAndBrandIgnoreCase(String name, String brand);
    List<Product> findByCategoryIgnoreCase(String category);
    List<Product> findByBrandIgnoreCase(String brand);
    List<Product> findByNameContainingIgnoreCase(String name);
}
