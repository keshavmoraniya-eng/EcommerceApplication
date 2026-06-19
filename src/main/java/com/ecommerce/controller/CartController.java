package com.ecommerce.controller;

import com.ecommerce.dto.ApiResponse;
import com.ecommerce.dto.CartItemRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;


    public CartController(CartService cartService, UserRepository userRepository) {
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    @GetMapping("/get_cart")
    @Operation(summary = "View current user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@AuthenticationPrincipal UserDetails userDetails){
        User user=resolveUser(userDetails);
        return ResponseEntity.ok(new ApiResponse<>(true,"Cart retried",cartService.getCart(user)));
    }

    @PostMapping("/add/items")
    @Operation(summary = "Add a product to cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody CartItemRequest request){
        User user=resolveUser(userDetails);
        return ResponseEntity.ok(new ApiResponse<>(true,"Item added to cart",cartService.addItem(user,request)));
    }

    @PutMapping("/update/items/{cartItemId}")
    @Operation(summary = "Update quantity of a cart item")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(@AuthenticationPrincipal UserDetails userDetails,@PathVariable Long cartItemId,@RequestParam int quantity){
        User user=resolveUser(userDetails);
        return ResponseEntity.ok(new ApiResponse<>(true,"Cart updated",cartService.updateItem(user,cartItemId,quantity)));
    }

    @DeleteMapping("/delete/items/{cartItemId}")
    @Operation(summary = "Remove a specific item from cart")
    public ResponseEntity<ApiResponse<CartResponse>> deleteItem(@AuthenticationPrincipal UserDetails userDetails,@PathVariable Long cartItemId){
        User user=resolveUser(userDetails);
        return ResponseEntity.ok(new ApiResponse<>(true,"Item removed",cartService.removeItem(user,cartItemId)));
    }

    @DeleteMapping("/clear/cart")
    @Operation(summary = "Clear the entire cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal UserDetails userDetails){
        User user=resolveUser(userDetails);
        cartService.clearCart(user);
        return ResponseEntity.ok(new ApiResponse<>(true,"Cart cleared",null));
    }

    private User resolveUser(UserDetails userDetails){
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(()->new IllegalArgumentException("User not found"));
    }
}
