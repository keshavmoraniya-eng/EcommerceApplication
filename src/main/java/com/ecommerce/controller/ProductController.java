package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.ProductRequest;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product Controller", description = "APIs for managing products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    //Public : list all active products with pagination
    @GetMapping("/list")
    @Operation(summary = "List all active products", description = "Returns a paginated list of all active products")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, @RequestParam(defaultValue = "createdAt") String sortBy){
        Page<ProductResponse> products=productService.getAllActive(
                PageRequest.of(page,size, Sort.by(Sort.Direction.DESC,sortBy))
        );
        return ResponseEntity.ok(new ApiResponse<>(true,"Products retrieved successfully",products));
    }

    //Public : search products by name
    @GetMapping("/search")
    @Operation(summary = "Search products by name", description = "Returns a paginated list of products matching the search query")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        Page<ProductResponse> products=productService.search(query,PageRequest.of(page,size));
        return ResponseEntity.ok(new ApiResponse<>(true,"Search results",products));
    }

    //Public : filter products by category
    @GetMapping("/category/{category}")
    @Operation(summary = "Filter products by category", description = "Returns a paginated list of products belonging to the specified category")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> byCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        Page<ProductResponse> proudcts=productService.getByCategory(category,PageRequest.of(page,size));
        return ResponseEntity.ok(new ApiResponse<>(true,"Products by category",proudcts));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getOne(@PathVariable Long id){
        return ResponseEntity.ok(new ApiResponse<>(true,"Product retrieved successfully",productService.getById(id)));
    }

    //Admin : create product
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new product", description = "Creates a new product with the provided details. Admin access required.",security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody ProductRequest request){
        ProductResponse created=productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true,"Product created successfully",created));
    }

    //Admin : update product
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing product", description = "Updates the details of an existing product. Admin access required.",security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ProductResponse>> update(@PathVariable Long id,@Valid @RequestBody ProductRequest request){
        return ResponseEntity.ok(new ApiResponse<>(true,"Product updated successfully",productService.update(id,request)));
    }


    //Admin : delete product
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a product", description = "Deletes a product by marking it as inactive. Admin access required.",security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id){
        productService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true,"Product deleted successfully",null));
    }

}
