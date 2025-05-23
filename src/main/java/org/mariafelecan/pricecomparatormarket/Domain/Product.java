package org.mariafelecan.pricecomparatormarket.Domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false)
    private String name;

    @Column(name = "brand")
    private String brand;

    @Column(name = "product_category")
    private String category;

    @Column(name = "package_quantity")
    private double grammage;

    @Column(name = "package_unit")
    private String unit;
}
