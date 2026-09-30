package com.foodordering.restaurant_service.controller;

import com.foodordering.restaurant_service.entity.Restaurant;
import com.foodordering.restaurant_service.repository.RestaurantRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")

public class RestaurantController {

    private final RestaurantRepository restaurantRepository;

    public RestaurantController(
            RestaurantRepository restaurantRepository) {

        this.restaurantRepository = restaurantRepository;
    }

    @GetMapping
    public List<Restaurant> getAllRestaurants() {

        return restaurantRepository.findAll();
    }

    @GetMapping("/{id}")
    public Restaurant getRestaurantById(
            @PathVariable Long id) {

        return restaurantRepository
                .findById(id)
                .orElse(null);
    }

    @PostMapping
    public Restaurant createRestaurant(
            @RequestBody Restaurant restaurant) {

        return restaurantRepository.save(
                restaurant
        );
    }
}
