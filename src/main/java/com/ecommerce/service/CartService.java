package com.ecommerce.service;

import com.ecommerce.dto.CartItemRequest;
import com.ecommerce.dto.CartItemResponse;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.entity.ProductStatus;
import com.ecommerce.entity.User;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartItemRepository;
import com.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;


    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    //view cart
    public CartResponse getCart(User user){
        List<CartItemResponse> items=cartItemRepository.findByUser(user)
                .stream().map(CartItemResponse::from)
                .toList();

        BigDecimal total=items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        return new CartResponse(items,total,items.size());
    }

    //Add item
    @Transactional
    public CartResponse addItem(User user, CartItemRequest request){
        Product product=productRepository.findById(request.getProductId())
                .orElseThrow(()->new ResourceNotFoundException("Product",request.getProductId()));

        if (product.getStatus()== ProductStatus.INACTIVE){
            throw new IllegalArgumentException("Product is not available");
        }

        if (product.getStockQuantity()<request.getQuantity()){
            throw new IllegalArgumentException("Insufficient stock. Available: "+product.getStockQuantity());
        }

        CartItem cartItem=cartItemRepository.findByUserAndProductId(user,request.getProductId()).
                orElseGet(()->{
                    CartItem c=new CartItem();
                    c.setUser(user);
                    c.setProduct(product);
                    c.setUnitPrice(product.getPrice());
                    c.setQuantity(0);
                    return c;
                });

        int newQty=cartItem.getQuantity()+request.getQuantity();
        if (newQty>product.getStockQuantity()){
            throw new IllegalArgumentException("Total quantity exceeds stock. Available: "+product.getStockQuantity());
        }

        cartItem.setQuantity(newQty);
        cartItemRepository.save(cartItem);
        return getCart(user);
    }

    // Update quantity of a specific cart item
    @Transactional
    public CartResponse updateItem(User user,Long cartItemId,int quantity){
        CartItem cartItem=cartItemRepository.findById(cartItemId)
                .orElseThrow(()->new ResourceNotFoundException("Cart item",cartItemId));

        if (!cartItem.getUser().getId().equals(user.getId())){
            throw new IllegalArgumentException("Cart item does not belong to current user");
        }

        if (quantity<=0){
            cartItemRepository.delete(cartItem);
        }else {
            if (quantity>cartItem.getProduct().getStockQuantity()){
                throw new IllegalArgumentException("Insufficient stock. Available: "+cartItem.getProduct().getStockQuantity());
            }
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        return getCart(user);
    }


    //Remove single item
    @Transactional
    public CartResponse removeItem(User user,Long cartItemId){
        CartItem cartItem=cartItemRepository.findById(cartItemId)
                .orElseThrow(()->new ResourceNotFoundException("Cart item",cartItemId));

        if (!cartItem.getUser().getId().equals(user.getId())){
            throw new IllegalArgumentException("Cart item does not belong to current user");
        }

        cartItemRepository.delete(cartItem);
        return getCart(user);
    }

    //clear entire cart
    @Transactional
    public void clearCart(User user){
        cartItemRepository.deleteByUser(user);
    }

}
