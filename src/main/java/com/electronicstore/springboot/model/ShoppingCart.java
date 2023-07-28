package com.electronicstore.springboot.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;

/*import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;*/

import java.util.*;
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
    private Set<ShoppingCartItem> items;


    private double totalAmountBeforeDiscount;

    private double totalDiscountAmount;

    private double totalAmount;

    private transient Map<Long, List<ShoppingCartItem>> itemsByProduct;

    //private transient List<DiscountRule> dealsApplied;

    public ShoppingCart() {
        items = new HashSet<>();
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

    public Set<ShoppingCartItem> getItems() {
        return items;
    }

    public Map<Long, List<ShoppingCartItem>> getItemsByProduct() {
        return itemsByProduct;
    }

    public void setItems(Set<ShoppingCartItem> items) {
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
