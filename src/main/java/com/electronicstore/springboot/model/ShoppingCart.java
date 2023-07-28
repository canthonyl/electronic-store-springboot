package com.electronicstore.springboot.model;

import jakarta.persistence.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
public class ShoppingCart {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="shopping_cart_id_seq")
    @SequenceGenerator(name = "shopping_cart_id_seq", sequenceName = "shopping_cart_id_seq", allocationSize = 1)
    private Long id;

    //uni-directional
    //@OneToMany
    //@JoinColumn(name="shoppingCartId", referencedColumnName="id")

    //bi-directional
    @OneToMany(mappedBy="shoppingCart", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
    private List<ShoppingCartItem> items;

    private transient double totalAmountBeforeDiscount;

    private transient double totalDiscountAmount;

    private transient double totalAmount;

    private transient Map<Long, List<ShoppingCartItem>> itemsByProduct;

    //private transient List<DiscountRule> dealsApplied;

    public ShoppingCart() {
        items = new LinkedList<>();
        itemsByProduct = new HashMap<>();
        //dealsApplied = new LinkedList<>();
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

    public List<ShoppingCartItem> getItems() {
        return items;
    }

    public Map<Long, List<ShoppingCartItem>> getItemsByProduct() {
        return itemsByProduct;
        //return items.stream().collect(groupingBy(i -> i.getProduct().getId(), mapping(Function.identity(), toList())));
    }

    public void setItems(List<ShoppingCartItem> items) {
        this.items = items;
        this.itemsByProduct = items.stream().collect(Collectors.groupingBy(i -> i.getProduct().getId(),
                Collectors.toList()));
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
