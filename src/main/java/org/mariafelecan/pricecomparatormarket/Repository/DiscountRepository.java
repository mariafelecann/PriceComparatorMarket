package org.mariafelecan.pricecomparatormarket.Repository;

import org.mariafelecan.pricecomparatormarket.Domain.Discount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountRepository extends JpaRepository<Discount, Long> {
}
