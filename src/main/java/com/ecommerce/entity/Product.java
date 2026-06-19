package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "products")
@Getter
@Setter
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false,precision = 10,scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity=0;

    private String imageUrl;

    @Column(nullable = false)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status=ProductStatus.ACTIVE;

    @Column(name = "created_at",updatable = false)
    private Instant createdAt=Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt=Instant.now();

    @PreUpdate
    public void onUpdate(){
        this.updatedAt=Instant.now();
    }
}
