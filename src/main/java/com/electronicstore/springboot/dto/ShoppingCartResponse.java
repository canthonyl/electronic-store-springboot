package com.electronicstore.springboot.dto;

import com.electronicstore.springboot.model.ShoppingCart;

public class ShoppingCartResponse {

    private ShoppingCart shoppingCart;

    public ShoppingCartResponse(ShoppingCart s) {
        shoppingCart = s;
    }

    public ShoppingCart getShoppingCart() {
        return shoppingCart;
    }

    public void setShoppingCart(ShoppingCart shoppingCart) {
        this.shoppingCart = shoppingCart;
    }
}
