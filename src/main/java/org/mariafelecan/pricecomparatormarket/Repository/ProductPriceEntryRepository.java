package org.mariafelecan.pricecomparatormarket.Repository;


import org.mariafelecan.pricecomparatormarket.Domain.Product;
import org.mariafelecan.pricecomparatormarket.Domain.ProductPriceEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductPriceEntryRepository extends JpaRepository<ProductPriceEntry, Long> {
    Optional<ProductPriceEntry> findTopByProductAndStoreAndDateLessThanEqualOrderByDateDesc(Product product, String store, LocalDate date);

    List<ProductPriceEntry> findByProductProductIdAndDateBetween(String productId, LocalDate startDate, LocalDate endDate);

    List<ProductPriceEntry> findByProductProductIdAndStoreAndDateBetween(
            String productId, String store, LocalDate startDate, LocalDate endDate);

    Optional<ProductPriceEntry> findFirstByProductOrderByDateDescIdDesc(Product product);

    Optional<ProductPriceEntry> findFirstByProductAndStoreOrderByDateDescIdDesc(Product product, String store);

}
