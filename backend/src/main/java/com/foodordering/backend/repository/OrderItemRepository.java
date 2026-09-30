package com.foodordering.backend.repository;

import com.foodordering.backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByRestaurantOrderId(Long restaurantOrderId);
}