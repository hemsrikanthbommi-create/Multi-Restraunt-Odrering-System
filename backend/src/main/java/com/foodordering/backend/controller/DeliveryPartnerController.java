package com.foodordering.backend.controller;

import com.foodordering.backend.entity.DeliveryPartner;
import com.foodordering.backend.repository.DeliveryPartnerRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery-partners")
@CrossOrigin(origins = "http://localhost:5173")
public class DeliveryPartnerController {

    private final DeliveryPartnerRepository deliveryPartnerRepository;

    public DeliveryPartnerController(
            DeliveryPartnerRepository deliveryPartnerRepository) {

        this.deliveryPartnerRepository =
                deliveryPartnerRepository;
    }


    // =====================================================
    // GET ALL DELIVERY PARTNERS
    // =====================================================

    @GetMapping
    public List<DeliveryPartner> getAllPartners(
            Authentication authentication) {

        requireRole(
                authentication,
                "DELIVERY_PARTNER"
        );

        return deliveryPartnerRepository.findAll();
    }


    // =====================================================
    // CREATE DELIVERY PARTNER
    // =====================================================

    @PostMapping
    public DeliveryPartner createPartner(
            @RequestBody DeliveryPartner partner,
            Authentication authentication) {

        requireRole(
                authentication,
                "DELIVERY_PARTNER"
        );


        if (partner.getName() == null ||
                partner.getName().trim().isEmpty()) {

            throw new RuntimeException(
                    "Delivery partner name is required"
            );
        }


        if (partner.getPhone() == null ||
                partner.getPhone().trim().isEmpty()) {

            throw new RuntimeException(
                    "Delivery partner phone is required"
            );
        }


        return deliveryPartnerRepository.save(
                partner
        );
    }


    // =====================================================
    // UPDATE AVAILABILITY
    // =====================================================

    @PatchMapping("/{id}/availability")
    public DeliveryPartner updateAvailability(
            @PathVariable Long id,
            @RequestParam boolean available,
            Authentication authentication) {

        requireRole(
                authentication,
                "DELIVERY_PARTNER"
        );


        DeliveryPartner partner =
                deliveryPartnerRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Delivery partner not found"
                                ));


        partner.setAvailable(
                available
        );


        return deliveryPartnerRepository.save(
                partner
        );
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
}