package com.foodordering.backend.controller;

import com.foodordering.backend.entity.Cart;
import com.foodordering.backend.entity.CartItem;
import com.foodordering.backend.entity.MenuItem;
import com.foodordering.backend.entity.User;

import com.foodordering.backend.repository.CartItemRepository;
import com.foodordering.backend.repository.CartRepository;
import com.foodordering.backend.repository.MenuItemRepository;
import com.foodordering.backend.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5173")
public class CartController {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    public CartController(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            MenuItemRepository menuItemRepository,
            UserRepository userRepository) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.menuItemRepository = menuItemRepository;
        this.userRepository = userRepository;
    }


    // =====================================================
    // CREATE CART
    // =====================================================

    @PostMapping("/{userId}")
    public Cart createCart(
            @PathVariable Long userId,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        verifyUser(user, userId);

        Cart existingCart = cartRepository
                .findByUserId(userId)
                .orElse(null);

        if (existingCart != null) {
            return existingCart;
        }

        Cart cart = new Cart();

        cart.setUserId(userId);

        return cartRepository.save(cart);
    }


    // =====================================================
    // GET USER CART
    // =====================================================

    @GetMapping("/{userId}")
    public Cart getCart(
            @PathVariable Long userId,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        verifyUser(user, userId);

        return cartRepository
                .findByUserId(userId)
                .orElse(null);
    }


    // =====================================================
    // ADD ITEM TO CART
    // =====================================================

    @PostMapping("/{cartId}/items")
    public CartItem addItem(
            @PathVariable Long cartId,
            @RequestParam Long menuItemId,
            @RequestParam int quantity,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        Cart cart = cartRepository
                .findById(cartId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Cart not found"));

        // Make sure this cart belongs to
        // the currently logged-in user.
        verifyCartOwnership(cart, user);


        if (quantity <= 0) {

            throw new RuntimeException(
                    "Quantity must be greater than zero");
        }


        MenuItem menuItem = menuItemRepository
                .findById(menuItemId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Menu item not found"));


        if (!menuItem.isAvailable()) {

            throw new RuntimeException(
                    "Menu item is currently unavailable");
        }


        CartItem cartItem = new CartItem();

        cartItem.setCartId(cart.getId());

        cartItem.setMenuItem(menuItem);

        cartItem.setQuantity(quantity);

        return cartItemRepository.save(cartItem);
    }


    // =====================================================
    // GET CART ITEMS
    // =====================================================

    @GetMapping("/{cartId}/items")
    public List<CartItem> getCartItems(
            @PathVariable Long cartId,
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        Cart cart = cartRepository
                .findById(cartId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Cart not found"));

        // Prevent one user from reading
        // another user's cart items.
        verifyCartOwnership(cart, user);

        return cartItemRepository
                .findByCartId(cartId);
    }


    // =====================================================
    // GET AUTHENTICATED USER
    // =====================================================

    private User getAuthenticatedUser(
            Authentication authentication) {

        if (authentication == null ||
                authentication.getName() == null) {

            throw new RuntimeException(
                    "User is not authenticated");
        }

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"));
    }


    // =====================================================
    // VERIFY USER OWNERSHIP
    // =====================================================

    private void verifyUser(
            User user,
            Long requestedUserId) {

        if (!user.getId().equals(requestedUserId)) {

            throw new RuntimeException(
                    "Unauthorized cart access");
        }
    }


    // =====================================================
    // VERIFY CART OWNERSHIP
    // =====================================================

    private void verifyCartOwnership(
            Cart cart,
            User user) {

        if (!user.getId().equals(cart.getUserId())) {

            throw new RuntimeException(
                    "Unauthorized cart access");
        }
    }
}