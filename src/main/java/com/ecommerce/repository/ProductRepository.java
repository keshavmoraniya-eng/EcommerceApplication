package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {
    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    Page<Product> findByCategoryAndStatus(String category, ProductStatus status, Pageable pageable);
    Page<Product> findByNameContainingAndStatus(String name, ProductStatus status, Pageable pageable);

}
