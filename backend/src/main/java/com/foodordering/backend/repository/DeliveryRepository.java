package com.foodordering.backend.repository;

import com.foodordering.backend.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    List<Delivery> findByRestaurantOrderId(Long restaurantOrderId);

    Optional<Delivery> findByDeliveryPartnerId(Long deliveryPartnerId);
}