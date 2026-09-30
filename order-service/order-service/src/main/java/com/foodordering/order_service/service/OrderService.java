package com.foodordering.order_service.service;

import com.foodordering.order_service.entity.CartItem;
import com.foodordering.order_service.entity.Order;
import com.foodordering.order_service.entity.OrderItem;
import com.foodordering.order_service.entity.RestaurantOrder;
import com.foodordering.order_service.repository.CartItemRepository;
import com.foodordering.order_service.repository.OrderItemRepository;
import com.foodordering.order_service.repository.OrderRepository;
import com.foodordering.order_service.repository.RestaurantOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RestaurantOrderRepository restaurantOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;

    public OrderService(
            OrderRepository orderRepository,
            RestaurantOrderRepository restaurantOrderRepository,
            OrderItemRepository orderItemRepository,
            CartItemRepository cartItemRepository) {

        this.orderRepository = orderRepository;
        this.restaurantOrderRepository = restaurantOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional
    public Order createOrderFromCart(Long userId, Long cartId) {

        List<CartItem> cartItems =
                cartItemRepository.findByCartId(cartId);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Calculate total order amount
        double totalAmount = cartItems.stream()
                .mapToDouble(item ->
                        item.getMenuItem().getPrice()
                                * item.getQuantity())
                .sum();

        // Create main customer order
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus("ORDER_SPLIT");

        Order savedOrder = orderRepository.save(order);

        // Group cart items by restaurant
        Map<Long, List<CartItem>> itemsByRestaurant =
                cartItems.stream()
                        .collect(Collectors.groupingBy(
                                item -> item.getMenuItem()
                                        .getRestaurant()
                                        .getId()
                        ));

        // Create restaurant-specific orders
        for (Map.Entry<Long, List<CartItem>> entry :
                itemsByRestaurant.entrySet()) {

            Long restaurantId = entry.getKey();

            List<CartItem> restaurantItems = entry.getValue();

            double restaurantAmount =
                    restaurantItems.stream()
                            .mapToDouble(item ->
                                    item.getMenuItem().getPrice()
                                            * item.getQuantity())
                            .sum();

            RestaurantOrder restaurantOrder =
                    new RestaurantOrder();

            restaurantOrder.setOrderId(savedOrder.getId());
            restaurantOrder.setRestaurantId(restaurantId);
            restaurantOrder.setAmount(restaurantAmount);
            restaurantOrder.setStatus("RESTAURANTS_NOTIFIED");

            RestaurantOrder savedRestaurantOrder =
                    restaurantOrderRepository.save(restaurantOrder);

            // Create order items
            for (CartItem cartItem : restaurantItems) {

                OrderItem orderItem = new OrderItem();

                orderItem.setRestaurantOrderId(
                        savedRestaurantOrder.getId()
                );

                orderItem.setMenuItemId(
                        cartItem.getMenuItem().getId()
                );

                orderItem.setItemName(
                        cartItem.getMenuItem().getName()
                );

                orderItem.setPrice(
                        cartItem.getMenuItem().getPrice()
                );

                orderItem.setQuantity(
                        cartItem.getQuantity()
                );

                orderItem.setSubtotal(
                        cartItem.getMenuItem().getPrice()
                                * cartItem.getQuantity()
                );

                orderItemRepository.save(orderItem);
            }
               }

        // Clear cart after successful order creation
        cartItemRepository.deleteByCartId(cartId);

        return savedOrder;
    }

    public void updateMasterOrderStatus(Long orderId) {

    Order order = orderRepository.findById(orderId)
            .orElseThrow(() ->
                    new RuntimeException("Order not found"));

    List<RestaurantOrder> restaurantOrders =
            restaurantOrderRepository.findByOrderId(orderId);

    if (restaurantOrders.isEmpty()) {
        return;
    }

    boolean allDelivered = restaurantOrders.stream()
            .allMatch(ro ->
                    "DELIVERED".equals(ro.getStatus()));

    boolean anyOutForDelivery = restaurantOrders.stream()
            .anyMatch(ro ->
                    "OUT_FOR_DELIVERY".equals(ro.getStatus()));

    boolean anyPickedUp = restaurantOrders.stream()
            .anyMatch(ro ->
                    "PICKED_UP".equals(ro.getStatus()));

    boolean anyDeliveryAssigned = restaurantOrders.stream()
            .anyMatch(ro ->
                    "DELIVERY_ASSIGNED".equals(ro.getStatus()));

    boolean anyReady = restaurantOrders.stream()
            .anyMatch(ro ->
                    "READY_FOR_PICKUP".equals(ro.getStatus()));

    boolean anyPreparing = restaurantOrders.stream()
            .anyMatch(ro ->
                    "FOOD_PREPARATION".equals(ro.getStatus()));

    boolean anyRestaurantsNotified = restaurantOrders.stream()
            .anyMatch(ro ->
                    "RESTAURANTS_NOTIFIED".equals(ro.getStatus()));

    if (allDelivered) {

        order.setStatus("DELIVERED");

    } else if (anyOutForDelivery) {

        order.setStatus("OUT_FOR_DELIVERY");

    } else if (anyPickedUp) {

        order.setStatus("PICKED_UP");

    } else if (anyDeliveryAssigned) {

        order.setStatus("DELIVERY_ASSIGNED");

    } else if (anyReady) {

        order.setStatus("READY_FOR_PICKUP");

    } else if (anyPreparing) {

        order.setStatus("FOOD_PREPARATION");

    } else if (anyRestaurantsNotified) {

        order.setStatus("RESTAURANTS_NOTIFIED");

    } else {

        order.setStatus("ORDER_SPLIT");
    }

    orderRepository.save(order);
}
}