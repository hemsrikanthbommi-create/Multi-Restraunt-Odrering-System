package com.foodordering.backend.controller;

import com.foodordering.backend.entity.MenuItem;
import com.foodordering.backend.entity.Restaurant;
import com.foodordering.backend.repository.MenuItemRepository;
import com.foodordering.backend.repository.RestaurantRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
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

        return menuItemRepository.findByRestaurantId(restaurantId);
    }

    @PostMapping("/restaurants/{restaurantId}/menu")
    public MenuItem addMenuItem(
            @PathVariable Long restaurantId,
            @RequestBody MenuItem menuItem) {

        Restaurant restaurant = restaurantRepository
                .findById(restaurantId)
                .orElseThrow(() ->
                        new RuntimeException("Restaurant not found"));

        menuItem.setRestaurant(restaurant);

        return menuItemRepository.save(menuItem);
    }
}