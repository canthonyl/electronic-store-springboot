package com.electronicstore.springboot.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
@Entity
@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="id")
@JsonIgnoreProperties({"shoppingCartId"})
public class ShoppingCartItem {

    public static ShoppingCartItem ofShoppingCart(Long id) {
        ShoppingCartItem item = new ShoppingCartItem();
        item.setShoppingCartId(id);
        return item;
    }

    public static ShoppingCartItem ofProduct(Long productId, Integer quantity) {
        ShoppingCartItem item = new ShoppingCartItem();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }

    public static ShoppingCartItem ofProductWithDiscount(Long shoppingCartId, Long productId, Integer quantity, Double price, Double amountBeforeDiscount, Double discountAmount, Double amount) {
        ShoppingCartItem item = new ShoppingCartItem();

        item.setProductId(productId);
        item.setQuantity(quantity);
        item.setPrice(price);
        item.setAmountBeforeDiscount(amountBeforeDiscount);
        item.setDiscountAmount(discountAmount);
        item.setAmount(amount);
        return item;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shopping_cart_item_id_seq")
    @SequenceGenerator(name = "shopping_cart_item_id_seq", sequenceName = "shopping_cart_item_id_seq", allocationSize = 1)
    private Long id;

    private Long shoppingCartId;

    @Positive
    private Long productId;

    @Positive
    private Integer quantity;

    private Double price;

    private Double amountBeforeDiscount;

    private Double discountAmount;

    private Double amount;

    private transient List<String> discountApplied;

    public ShoppingCartItem() {
        discountApplied = new LinkedList<>();
    }

    public ShoppingCartItem(Long productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public ShoppingCartItem(Long shoppingCartId, Long productId, int quantity) {
        //this.shoppingCart = new ShoppingCart(shoppingCartId);
        this.shoppingCartId = shoppingCartId;
        this.productId = productId;
        this.quantity = quantity;
    }

}

