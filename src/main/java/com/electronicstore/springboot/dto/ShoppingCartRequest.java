package com.electronicstore.springboot.dto;

import com.electronicstore.springboot.model.ShoppingCartItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static com.electronicstore.springboot.dto.ShoppingCartRequest.ResponseType.Auto;

@Getter
@Setter
public class ShoppingCartRequest {

    public enum ResponseType { Auto, ShoppingCart, None }

    private ResponseType responseType;

    @NotEmpty
    private List<@Valid ShoppingCartItem> shoppingCartItems;

    public ShoppingCartRequest() {
        responseType = Auto;
    }

}
//TODO web session