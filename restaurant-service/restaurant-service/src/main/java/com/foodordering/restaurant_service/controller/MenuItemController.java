package com.foodordering.restaurant_service.controller;

import com.foodordering.restaurant_service.entity.MenuItem;
import com.foodordering.restaurant_service.entity.Restaurant;

import com.foodordering.restaurant_service.repository.MenuItemRepository;
import com.foodordering.restaurant_service.repository.RestaurantRepository;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")

public class MenuItemController {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    public MenuItemController(
            MenuItemRepository menuItemRepository,
            RestaurantRepository restaurantRepository) {

        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @GetMapping("/restaurants/{restaurantId}/menu")
    public List<MenuItem> getMenuByRestaurant(
            @PathVariable Long restaurantId) {

        return menuItemRepository
                .findByRestaurantId(restaurantId);
    }

    @PostMapping("/restaurants/{restaurantId}/menu")
    public MenuItem addMenuItem(
            @PathVariable Long restaurantId,
            @RequestBody MenuItem menuItem) {

        Restaurant restaurant =
                restaurantRepository
                        .findById(restaurantId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Restaurant not found"
                                ));

        menuItem.setRestaurant(restaurant);

        return menuItemRepository.save(
                menuItem
        );
    }
}
