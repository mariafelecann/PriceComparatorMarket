package org.mariafelecan.pricecomparatormarket.Domain;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private BigDecimal targetPrice;

    private boolean triggered;

    private LocalDateTime createdAt;
}
