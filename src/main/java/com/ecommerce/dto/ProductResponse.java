package com.ecommerce.dto;

import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private String category;
    private String imageUrl;
    private Double price;
    private Integer stockQuantity;
    private ProductStatus status;
    private Instant createdAt;

    public static ProductResponse from(Product product){
        ProductResponse dto=new ProductResponse();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setCategory(product.getCategory());
        dto.setImageUrl(product.getImageUrl());
        dto.setPrice(product.getPrice().doubleValue());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setStatus(product.getStatus());
        dto.setCreatedAt(product.getCreatedAt());
        return dto;
    }

}
