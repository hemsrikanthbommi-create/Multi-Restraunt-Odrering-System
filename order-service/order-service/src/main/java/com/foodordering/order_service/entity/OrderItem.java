package com.foodordering.order_service.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long restaurantOrderId;

    private Long menuItemId;

    private String itemName;

    private double price;

    private int quantity;

    private double subtotal;

    public OrderItem() {
    }

    public Long getId() {
        return id;
    }

    public Long getRestaurantOrderId() {
        return restaurantOrderId;
    }

    public void setRestaurantOrderId(Long restaurantOrderId) {
        this.restaurantOrderId = restaurantOrderId;
    }

    public Long getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(Long menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
}