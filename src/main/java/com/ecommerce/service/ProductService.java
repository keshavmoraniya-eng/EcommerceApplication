package com.ecommerce.service;

import com.ecommerce.dto.ProductRequest;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductStatus;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;


@Server
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    //Public : list active products with pagination
    public Page<ProductResponse> getAllActive(Pageable pageable){
        return productRepository.findByStatus(ProductStatus.ACTIVE,pageable)
                .map(ProductResponse::from);
    }

    //Public : search by name
    public Page<ProductResponse> search(String query,Pageable pageable){
        return productRepository.findByNameContainingAndStatus(query,ProductStatus.ACTIVE,pageable)
                .map(ProductResponse::from);
    }

    //Public : filter by category
    public Page<ProductResponse> getByCategory(String category,Pageable pageable){
        return productRepository.findByCategoryAndStatus(category,ProductStatus.ACTIVE,pageable)
                .map(ProductResponse::from);
    }

    public ProductResponse getById(Long id){
        Product product=productRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Product not found with id: "+id));
        if(product.getStatus()!=ProductStatus.ACTIVE){
            throw new ResourceNotFoundException("Product not found with id: "+id);
        }
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest request){
        Product product=new Product();
        applyRequest(product,request);
        return ProductResponse.from(productRepository.save(product));
    }

    //Admin : update product
    @Transactional
    public ProductResponse update(Long id,ProductRequest request){
        Product product=productRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Product not found with id: "+id));
        applyRequest(product,request);
        return ProductResponse.from(productRepository.save(product));
    }

    //Admin : delete product
    @Transactional
    public void delete(Long id){
        Product product=productRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Product",id));
        product.setStatus(ProductStatus.INACTIVE);
        productRepository.save(product);
    }


    private void applyRequest(Product product,ProductRequest request){
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(request.getCategory());

        //Auto-flag out-of-stock products as INACTIVE
        if (request.getStockQuantity()==0){
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        }else {
            product.setStatus(ProductStatus.ACTIVE);
        }
    }


}
