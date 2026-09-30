package com.foodordering.order_service.repository;

import com.foodordering.order_service.entity.RestaurantOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantOrderRepository extends JpaRepository<RestaurantOrder, Long> {

    List<RestaurantOrder> findByOrderId(Long orderId);

    List<RestaurantOrder> findByRestaurantId(Long restaurantId);
}