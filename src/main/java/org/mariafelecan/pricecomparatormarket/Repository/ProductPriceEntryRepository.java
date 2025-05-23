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

    List<ProductPriceEntry> findByProductIdAndDateBetween(Long productId, LocalDate startDate, LocalDate endDate);

    List<ProductPriceEntry> findByProductIdAndStoreNameAndDateBetween(Long productId, String storeName, LocalDate startDate, LocalDate endDate);

    @Query("SELECT ppe FROM ProductPriceEntry ppe WHERE ppe.product = :product ORDER BY ppe.date DESC, ppe.id DESC")
    Optional<ProductPriceEntry> findLatestPriceForProduct(@Param("product") Product product);

    @Query("SELECT ppe FROM ProductPriceEntry ppe WHERE ppe.product = :product AND ppe.store = :storeName ORDER BY ppe.date DESC, ppe.id DESC")
    Optional<ProductPriceEntry> findLatestPriceForProductAndStore(@Param("product") Product product, @Param("storeName") String storeName);

    @Query("SELECT ppe FROM ProductPriceEntry ppe WHERE ppe.product = :product AND ppe.date <= :date ORDER BY ppe.date DESC, ppe.id DESC")
    Optional<ProductPriceEntry> findLatestPriceForProductBeforeDate(@Param("product") Product product, @Param("date") LocalDate date);

    Optional<ProductPriceEntry> findTopByProductAndStoreNameAndDateLessThanEqualOrderByDateDesc(Product product, String storeName, LocalDate date);

}
