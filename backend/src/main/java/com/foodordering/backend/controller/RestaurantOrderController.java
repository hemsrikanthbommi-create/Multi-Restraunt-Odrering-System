package com.foodordering.backend.controller;

import com.foodordering.backend.entity.Order;
import com.foodordering.backend.entity.RestaurantOrder;
import com.foodordering.backend.entity.User;

import com.foodordering.backend.repository.OrderRepository;
import com.foodordering.backend.repository.RestaurantOrderRepository;
import com.foodordering.backend.repository.UserRepository;

import com.foodordering.backend.service.OrderService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurant-orders")
@CrossOrigin(origins = "http://localhost:5173")
public class RestaurantOrderController {

    private final RestaurantOrderRepository restaurantOrderRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;

    public RestaurantOrderController(
            RestaurantOrderRepository restaurantOrderRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            OrderService orderService) {

        this.restaurantOrderRepository =
                restaurantOrderRepository;

        this.orderRepository =
                orderRepository;

        this.userRepository =
                userRepository;

        this.orderService =
                orderService;
    }


    // =====================================================
    // CUSTOMER - GET RESTAURANT ORDERS FOR THEIR ORDER
    // =====================================================

    @GetMapping("/order/{orderId}")
    public List<RestaurantOrder> getRestaurantOrdersByOrder(
            @PathVariable Long orderId,
            Authentication authentication) {

        User user =
                getAuthenticatedUser(authentication);

        requireRole(
                authentication,
                "CUSTOMER"
        );


        Order order =
                orderRepository
                        .findById(orderId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Order not found"
                                ));


        // Make sure the master order belongs
        // to the logged-in customer.
        if (!user.getId().equals(order.getUserId())) {

            throw new RuntimeException(
                    "Unauthorized order access"
            );
        }


        return restaurantOrderRepository
                .findByOrderId(orderId);
    }


    // =====================================================
    // RESTAURANT - GET RESTAURANT ORDERS
    // =====================================================

    @GetMapping("/restaurant/{restaurantId}")
    public List<RestaurantOrder> getRestaurantOrdersByRestaurant(
            @PathVariable Long restaurantId,
            Authentication authentication) {

        requireRole(
                authentication,
                "RESTAURANT"
        );


        return restaurantOrderRepository
                .findByRestaurantId(restaurantId);
    }


    // =====================================================
    // RESTAURANT - UPDATE RESTAURANT ORDER STATUS
    // =====================================================

    @PatchMapping("/{id}/status")
    public RestaurantOrder updateRestaurantOrderStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication) {

        requireRole(
                authentication,
                "RESTAURANT"
        );


        RestaurantOrder restaurantOrder =
                restaurantOrderRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Restaurant order not found"
                                ));


        // Validate the status before saving it.
        validateStatus(status);


        restaurantOrder.setStatus(status);


        RestaurantOrder savedRestaurantOrder =
                restaurantOrderRepository.save(
                        restaurantOrder
                );


        // Synchronize the main customer order.
        orderService.updateMasterOrderStatus(
                savedRestaurantOrder.getOrderId()
        );


        return savedRestaurantOrder;
    }


    // =====================================================
    // GET AUTHENTICATED USER
    // =====================================================

    private User getAuthenticatedUser(
            Authentication authentication) {

        if (authentication == null ||
                authentication.getName() == null) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }


        return userRepository
                .findByEmail(
                        authentication.getName()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        ));
    }


    // =====================================================
    // ROLE CHECK
    // =====================================================

    private void requireRole(
            Authentication authentication,
            String requiredRole) {

        if (authentication == null) {

            throw new RuntimeException(
                    "User is not authenticated"
            );
        }


        String requiredAuthority =
                "ROLE_" + requiredRole;


        boolean hasRole =
                authentication
                        .getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(
                                requiredAuthority::equals
                        );


        if (!hasRole) {

            throw new RuntimeException(
                    "Unauthorized. Required role: "
                            + requiredRole
            );
        }
    }


    // =====================================================
    // STATUS VALIDATION
    // =====================================================

    private void validateStatus(String status) {

        if (status == null ||
                status.trim().isEmpty()) {

            throw new RuntimeException(
                    "Status cannot be empty"
            );
        }


        switch (status) {

            case "ORDER_PLACED":
            case "RESTAURANTS_NOTIFIED":
            case "RESTAURANTS_ACCEPT":
            case "FOOD_PREPARATION":
            case "READY_FOR_PICKUP":
            case "DELIVERY_ASSIGNED":
            case "PICKED_UP":
            case "OUT_FOR_DELIVERY":
            case "DELIVERED":

                break;


            default:

                throw new RuntimeException(
                        "Invalid restaurant order status: "
                                + status
                );
        }
    }
}