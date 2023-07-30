package com.electronicstore.springboot.model;

import jakarta.persistence.*;

import java.util.LinkedList;
import java.util.List;

@Entity
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shopping_cart_id_seq")
    @SequenceGenerator(name = "shopping_cart_id_seq", sequenceName = "shopping_cart_id_seq", allocationSize = 1)
    private Long id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "shopping_cart_item")
    private List<Item> items;

    private transient double totalAmountBeforeDiscount;

    private transient double totalDiscountAmount;

    private transient double totalAmount;

    public ShoppingCart() {
        items = new LinkedList<>();
    }

    public ShoppingCart(Long id) {
        this();
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getTotalDiscountAmount() {
        return totalDiscountAmount;
    }

    public void setTotalDiscountAmount(double totalDiscountAmount) {
        this.totalDiscountAmount = totalDiscountAmount;
    }

    public double getTotalAmountBeforeDiscount() {
        return totalAmountBeforeDiscount;
    }

    public void setTotalAmountBeforeDiscount(double totalAmountBeforeDiscount) {
        this.totalAmountBeforeDiscount = totalAmountBeforeDiscount;
    }

    /*public List<DiscountRule> getDealsApplied() {
        return dealsApplied;
    }

    public void setDealsApplied(List<DiscountRule> dealsApplied) {
        this.dealsApplied = dealsApplied;
    }*/
}
