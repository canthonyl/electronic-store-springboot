package com.electronicstore.springboot.dto;

import com.electronicstore.springboot.model.ShoppingCartItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

import static com.electronicstore.springboot.dto.ShoppingCartRequest.ResponseType.Auto;

public class ShoppingCartRequest {

    public enum ResponseType { Auto, ShoppingCart, None }

    private ResponseType responseType;

    @NotEmpty
    private List<@Valid ShoppingCartItem> shoppingCartItems;

    public ShoppingCartRequest() {
        responseType = Auto;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public List<ShoppingCartItem> getShoppingCartItems() {
        return shoppingCartItems;
    }

    public void setShoppingCartItems(List<ShoppingCartItem> shoppingCartItems) {
        this.shoppingCartItems = shoppingCartItems;
    }
}
//TODO web session