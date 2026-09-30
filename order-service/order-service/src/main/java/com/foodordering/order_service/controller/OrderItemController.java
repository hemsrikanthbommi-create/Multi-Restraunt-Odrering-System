package com.foodordering.order_service.controller;

import com.foodordering.order_service.entity.Order;
import com.foodordering.order_service.entity.OrderItem;
import com.foodordering.order_service.entity.User;

import com.foodordering.order_service.repository.OrderItemRepository;
import com.foodordering.order_service.repository.OrderRepository;
import com.foodordering.order_service.repository.RestaurantOrderRepository;
import com.foodordering.order_service.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    private final OrderItemRepository orderItemRepository;
    private final RestaurantOrderRepository restaurantOrderRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderItemController(
            OrderItemRepository orderItemRepository,
            RestaurantOrderRepository restaurantOrderRepository,
            OrderRepository orderRepository,
            UserRepository userRepository) {

        this.orderItemRepository = orderItemRepository;
        this.restaurantOrderRepository = restaurantOrderRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/restaurant-order/{restaurantOrderId}")
    public List<OrderItem> getOrderItems(
            @PathVariable Long restaurantOrderId,
            Authentication authentication) {

        User user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        // Find the restaurant-specific order
        var restaurantOrder =
                restaurantOrderRepository
                        .findById(restaurantOrderId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Restaurant order not found"));

        // Find the main customer order
        Order order = orderRepository
                .findById(restaurantOrder.getOrderId())
                .orElseThrow(() ->
                        new RuntimeException("Order not found"));

        // Make sure the order belongs to the logged-in user
        if (!user.getId().equals(order.getUserId())) {

            throw new RuntimeException(
                    "Unauthorized order item access");
        }

        return orderItemRepository
                .findByRestaurantOrderId(restaurantOrderId);
    }
}
