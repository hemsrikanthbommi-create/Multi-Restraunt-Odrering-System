package com.foodordering.backend.controller;

import com.foodordering.backend.entity.Cart;
import com.foodordering.backend.entity.Order;
import com.foodordering.backend.entity.User;

import com.foodordering.backend.repository.CartRepository;
import com.foodordering.backend.repository.OrderRepository;
import com.foodordering.backend.repository.UserRepository;

import com.foodordering.backend.service.OrderService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;

    public OrderController(
            OrderRepository orderRepository,
            OrderService orderService,
            UserRepository userRepository,
            CartRepository cartRepository) {

        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
    }

    @PostMapping("/from-cart")
    public Order createOrderFromCart(
            @RequestParam Long userId,
            @RequestParam Long cartId,
            Authentication authentication) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Verify that the logged-in user matches the requested user
        if (!user.getId().equals(userId)) {
            throw new RuntimeException(
                    "Unauthorized order access");
        }

        // Verify that the cart belongs to the logged-in user
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() ->
                        new RuntimeException("Cart not found"));

        if (!user.getId().equals(cart.getUserId())) {
            throw new RuntimeException(
                    "Unauthorized cart access");
        }

        return orderService.createOrderFromCart(
                userId,
                cartId
        );
    }

    @GetMapping("/{id}")
    public Order getOrder(
            @PathVariable Long id,
            Authentication authentication) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        // Only the owner can view this order
        if (!user.getId().equals(order.getUserId())) {
            throw new RuntimeException(
                    "Unauthorized order access");
        }

        return order;
    }

    @GetMapping("/user/{userId}")
    public List<Order> getUserOrders(
            @PathVariable Long userId,
            Authentication authentication) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Only allow the logged-in user to request their orders
        if (!user.getId().equals(userId)) {
            throw new RuntimeException(
                    "Unauthorized order access");
        }

        return orderRepository.findByUserId(userId);
    }

    @PatchMapping("/{id}/status")
    public Order updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Order order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        // Only the order owner can access this endpoint
        if (!user.getId().equals(order.getUserId())) {
            throw new RuntimeException(
                    "Unauthorized order access");
        }

        order.setStatus(status);

        return orderRepository.save(order);
    }
}