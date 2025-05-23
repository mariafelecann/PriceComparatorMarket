package org.mariafelecan.pricecomparatormarket.Repository;

import org.mariafelecan.pricecomparatormarket.Domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Product findByProductId(String productId);
}
