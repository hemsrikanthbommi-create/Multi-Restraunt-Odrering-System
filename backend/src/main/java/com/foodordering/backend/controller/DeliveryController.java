package com.foodordering.backend.controller;

import com.foodordering.backend.entity.Delivery;
import com.foodordering.backend.entity.Order;
import com.foodordering.backend.entity.RestaurantOrder;
import com.foodordering.backend.entity.User;

import com.foodordering.backend.repository.DeliveryRepository;
import com.foodordering.backend.repository.OrderRepository;
import com.foodordering.backend.repository.RestaurantOrderRepository;
import com.foodordering.backend.repository.UserRepository;

import com.foodordering.backend.service.OrderService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@CrossOrigin(origins = "http://localhost:5173")
public class DeliveryController {

    private final DeliveryRepository deliveryRepository;
    private final RestaurantOrderRepository restaurantOrderRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;

    public DeliveryController(
            DeliveryRepository deliveryRepository,
            RestaurantOrderRepository restaurantOrderRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            OrderService orderService) {

        this.deliveryRepository = deliveryRepository;
        this.restaurantOrderRepository =
                restaurantOrderRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.orderService = orderService;
    }


    // =====================================================
    // GET ALL DELIVERIES
    // =====================================================
    // Only delivery partners can see the complete
    // delivery list.
    // =====================================================

    @GetMapping
    public List<Delivery> getAllDeliveries(
            Authentication authentication) {

        requireRole(
                authentication,
                "DELIVERY_PARTNER"
        );

        return deliveryRepository.findAll();
    }


    // =====================================================
    // GET SINGLE DELIVERY
    // =====================================================
    // Customer:
    //   Can view only their own delivery.
    //
    // Delivery Partner:
    //   Can view their assigned delivery.
    // =====================================================

    @GetMapping("/{id}")
    public Delivery getDelivery(
            @PathVariable Long id,
            Authentication authentication) {

        User user =
                getAuthenticatedUser(authentication);

        Delivery delivery =
                deliveryRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery not found"
                                ));


        if (hasRole(
                authentication,
                "CUSTOMER")) {

            verifyCustomerDeliveryOwnership(
                    delivery,
                    user
            );

        } else if (hasRole(
                authentication,
                "DELIVERY_PARTNER")) {

            verifyDeliveryPartnerAssignment(
                    delivery,
                    user
            );

        } else {

            throw new RuntimeException(
                    "Unauthorized delivery access"
            );
        }


        return delivery;
    }


    // =====================================================
    // GET DELIVERIES FOR RESTAURANT ORDER
    // =====================================================
    // Customer can view deliveries belonging to
    // their own order.
    //
    // Delivery partner can view deliveries assigned
    // to them.
    // =====================================================

    @GetMapping("/restaurant-order/{restaurantOrderId}")
    public List<Delivery> getByRestaurantOrder(
            @PathVariable Long restaurantOrderId,
            Authentication authentication) {

        User user =
                getAuthenticatedUser(authentication);


        RestaurantOrder restaurantOrder =
                restaurantOrderRepository
                        .findById(restaurantOrderId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Restaurant order not found"
                                ));


        List<Delivery> deliveries =
                deliveryRepository
                        .findByRestaurantOrderId(
                                restaurantOrderId
                        );


        if (hasRole(
                authentication,
                "CUSTOMER")) {

            verifyRestaurantOrderCustomerOwnership(
                    restaurantOrder,
                    user
            );

            return deliveries;
        }


        if (hasRole(
                authentication,
                "DELIVERY_PARTNER")) {

            return deliveries.stream()
                    .filter(delivery ->
                            user.getId().equals(
                                    delivery
                                            .getDeliveryPartnerId()
                            )
                    )
                    .toList();
        }


        throw new RuntimeException(
                "Unauthorized delivery access"
        );
    }


    // =====================================================
    // CREATE DELIVERY / ASSIGN DELIVERY PARTNER
    // =====================================================
    //
    // This is an orchestration/admin operation.
    //
    // For now it is allowed for RESTAURANT users.
    // Later this can be moved to an ADMIN /
    // ORCHESTRATOR service.
    // =====================================================

    @PostMapping
    public Delivery createDelivery(
            @RequestParam Long restaurantOrderId,
            @RequestParam Long deliveryPartnerId,
            Authentication authentication) {

        requireRole(
                authentication,
                "RESTAURANT"
        );


        RestaurantOrder restaurantOrder =
                restaurantOrderRepository
                        .findById(restaurantOrderId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Restaurant order not found"
                                ));


        User deliveryPartner =
                userRepository
                        .findById(deliveryPartnerId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery partner not found"
                                ));


        if (!"DELIVERY_PARTNER".equals(
                deliveryPartner.getRole())) {

            throw new RuntimeException(
                    "Selected user is not a delivery partner"
            );
        }


        // Prevent duplicate active assignments.
        List<Delivery> existingDeliveries =
                deliveryRepository
                        .findByRestaurantOrderId(
                                restaurantOrderId
                        );


        boolean alreadyAssigned =
                existingDeliveries.stream()
                        .anyMatch(delivery ->
                                !"DELIVERED".equals(
                                        delivery.getStatus()
                                ));


        if (alreadyAssigned) {

            throw new RuntimeException(
                    "A delivery is already assigned to this restaurant order"
            );
        }


        Delivery delivery =
                new Delivery();


        delivery.setRestaurantOrderId(
                restaurantOrderId
        );

        delivery.setDeliveryPartnerId(
                deliveryPartnerId
        );

        delivery.setStatus(
                "ASSIGNED"
        );

        delivery.setAssignedAt(
                LocalDateTime.now()
        );


        restaurantOrder.setStatus(
                "DELIVERY_ASSIGNED"
        );


        restaurantOrderRepository.save(
                restaurantOrder
        );


        orderService.updateMasterOrderStatus(
                restaurantOrder.getOrderId()
        );


        return deliveryRepository.save(
                delivery
        );
    }


    // =====================================================
    // UPDATE DELIVERY STATUS
    // =====================================================
    //
    // Only the assigned delivery partner can change
    // the delivery status.
    // =====================================================

    @PatchMapping("/{id}/status")
    public Delivery updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication) {

        User user =
                getAuthenticatedUser(authentication);


        requireRole(
                authentication,
                "DELIVERY_PARTNER"
        );


        Delivery delivery =
                deliveryRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery not found"
                                ));


        verifyDeliveryPartnerAssignment(
                delivery,
                user
        );


        validateDeliveryStatus(status);


        delivery.setStatus(status);


        if ("PICKED_UP".equals(status)) {

            delivery.setPickedUpAt(
                    LocalDateTime.now()
            );
        }


        if ("DELIVERED".equals(status)) {

            delivery.setDeliveredAt(
                    LocalDateTime.now()
            );
        }


        Delivery savedDelivery =
                deliveryRepository.save(
                        delivery
                );


        RestaurantOrder restaurantOrder =
                restaurantOrderRepository
                        .findById(
                                savedDelivery
                                        .getRestaurantOrderId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Restaurant order not found"
                                ));


        // Synchronize restaurant-order status.
        if ("PICKED_UP".equals(status)) {

            restaurantOrder.setStatus(
                    "PICKED_UP"
            );

        } else if (
                "OUT_FOR_DELIVERY".equals(status)) {

            restaurantOrder.setStatus(
                    "OUT_FOR_DELIVERY"
            );

        } else if (
                "DELIVERED".equals(status)) {

            restaurantOrder.setStatus(
                    "DELIVERED"
            );
        }


        RestaurantOrder savedRestaurantOrder =
                restaurantOrderRepository.save(
                        restaurantOrder
                );


        // Synchronize master customer order.
        orderService.updateMasterOrderStatus(
                savedRestaurantOrder.getOrderId()
        );


        return savedDelivery;
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
    // CUSTOMER DELIVERY OWNERSHIP
    // =====================================================

    private void verifyCustomerDeliveryOwnership(
            Delivery delivery,
            User user) {

        RestaurantOrder restaurantOrder =
                getRestaurantOrderForDelivery(
                        delivery
                );


        verifyRestaurantOrderCustomerOwnership(
                restaurantOrder,
                user
        );
    }


    // =====================================================
    // CUSTOMER RESTAURANT ORDER OWNERSHIP
    // =====================================================

    private void verifyRestaurantOrderCustomerOwnership(
            RestaurantOrder restaurantOrder,
            User user) {

        Order order =
                orderRepository
                        .findById(
                                restaurantOrder.getOrderId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Order not found"
                                ));


        if (!user.getId().equals(
                order.getUserId())) {

            throw new RuntimeException(
                    "Unauthorized delivery access"
            );
        }
    }


    // =====================================================
    // DELIVERY PARTNER ASSIGNMENT
    // =====================================================

    private void verifyDeliveryPartnerAssignment(
            Delivery delivery,
            User user) {

        if (!user.getId().equals(
                delivery.getDeliveryPartnerId())) {

            throw new RuntimeException(
                    "This delivery is not assigned to you"
            );
        }
    }


    // =====================================================
    // GET RESTAURANT ORDER
    // =====================================================

    private RestaurantOrder getRestaurantOrderForDelivery(
            Delivery delivery) {

        return restaurantOrderRepository
                .findById(
                        delivery
                                .getRestaurantOrderId()
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Restaurant order not found"
                        ));
    }


    // =====================================================
    // CHECK ROLE
    // =====================================================

    private boolean hasRole(
            Authentication authentication,
            String role) {

        if (authentication == null) {
            return false;
        }


        String requiredAuthority =
                "ROLE_" + role;


        return authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(
                        requiredAuthority::equals
                );
    }


    // =====================================================
    // REQUIRE ROLE
    // =====================================================

    private void requireRole(
            Authentication authentication,
            String role) {

        if (!hasRole(
                authentication,
                role)) {

            throw new RuntimeException(
                    "Unauthorized. Required role: "
                            + role
            );
        }
    }


    // =====================================================
    // VALIDATE DELIVERY STATUS
    // =====================================================

    private void validateDeliveryStatus(
            String status) {

        if (status == null ||
                status.trim().isEmpty()) {

            throw new RuntimeException(
                    "Delivery status cannot be empty"
            );
        }


        switch (status) {

            case "ASSIGNED":
            case "PICKED_UP":
            case "OUT_FOR_DELIVERY":
            case "DELIVERED":

                break;


            default:

                throw new RuntimeException(
                        "Invalid delivery status: "
                                + status
                );
        }
    }
}