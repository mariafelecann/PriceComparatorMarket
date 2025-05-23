package org.mariafelecan.pricecomparatormarket.Repository;

import org.mariafelecan.pricecomparatormarket.Domain.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {
}
