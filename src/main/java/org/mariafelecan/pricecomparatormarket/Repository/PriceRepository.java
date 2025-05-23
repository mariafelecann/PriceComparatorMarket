package org.mariafelecan.pricecomparatormarket.Repository;


import org.mariafelecan.pricecomparatormarket.Domain.ProductPriceEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceRepository extends JpaRepository<ProductPriceEntry, Long> {
}
