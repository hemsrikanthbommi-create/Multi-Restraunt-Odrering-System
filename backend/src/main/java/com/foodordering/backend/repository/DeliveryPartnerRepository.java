package com.foodordering.backend.repository;

import com.foodordering.backend.entity.DeliveryPartner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryPartnerRepository
        extends JpaRepository<DeliveryPartner, Long> {
}