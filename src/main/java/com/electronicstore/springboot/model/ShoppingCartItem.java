package com.electronicstore.springboot.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class ShoppingCartItem {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="shopping_cart_item_id_seq")
    @SequenceGenerator(name = "shopping_cart_item_id_seq", sequenceName = "shopping_cart_item_id_seq", allocationSize = 1)
    private Long id;

    @Column(table="shopping_cart_item", name="shopping_cart_id")
    private Long shoppingCartId;

    @ManyToOne
    private Product product;

    private int quantity;

    private double price;

    private double amountBeforeDiscount;

    private double discountAmount;

    private double amount;

    private transient List<String> discountApplied;

    public ShoppingCartItem(){}

    public ShoppingCartItem(Long shoppingCartId, Long productId, int quantity) {
        this.shoppingCartId = shoppingCartId;
        this.quantity = quantity;
        this.product = new Product(productId);

    }


    public ShoppingCartItem(Long id, Long shoppingCartId, Long productId) {
        this.id= id;
        this.shoppingCartId = shoppingCartId;
        this.product = new Product(productId);
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getShoppingCartId() {
        return shoppingCartId;
    }

    public void setShoppingCartId(Long shoppingCartId) {
        this.shoppingCartId = shoppingCartId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getAmountBeforeDiscount() {
        return amountBeforeDiscount;
    }

    public void setAmountBeforeDiscount(double amountBeforeDiscount) {
        this.amountBeforeDiscount = amountBeforeDiscount;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public List<String> getDiscountApplied() {
        return discountApplied;
    }

    public void setDiscountApplied(List<String> discountApplied) {
        this.discountApplied = discountApplied;
    }
}
