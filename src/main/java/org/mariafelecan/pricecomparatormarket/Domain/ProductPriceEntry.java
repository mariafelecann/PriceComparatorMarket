package org.mariafelecan.pricecomparatormarket.Domain;

import jakarta.persistence.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "price_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPriceEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String store;
    private LocalDate date;

    private BigDecimal price;
    private String currency;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
