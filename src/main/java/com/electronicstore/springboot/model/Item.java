package com.electronicstore.springboot.model;

//import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

@Embeddable
//@Entity
//@JsonIdentityInfo(generator= ObjectIdGenerators.PropertyGenerator.class, property="id")
public class Item {

    //@Id
    //@GeneratedValue(strategy= GenerationType.SEQUENCE, generator="shopping_cart_item_id_seq")
    //@SequenceGenerator(name = "shopping_cart_item_id_seq", sequenceName = "shopping_cart_item_id_seq", allocationSize = 1)
    @Column(name="id", insertable = false)
    private Long id;

    //@Id
    //private Long shoppingCartId;

    //bi-directional
    //@JsonIgnoreProperties
    //@ManyToOne//(cascade = CascadeType.DETACH)
    //@JoinColumn(name="shopping_cart_id", referencedColumnName="id")
    //private ShoppingCart shoppingCart;

    @ManyToOne
    private Product product;

    private int quantity;

    private double price;

    private double amountBeforeDiscount;

    private double discountAmount;

    private double amount;

    private transient List<String> discountApplied;

    public Item(){}

    public Item(long productId, int qty) {
        product = new Product(productId);
        quantity = qty;
    }

    public Item(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /*public Long getShoppingCartId() {
        return shoppingCartId;
    }

    public void setShoppingCartId(Long shoppingCartId) {
        this.shoppingCartId = shoppingCartId;
    }*/

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getAmountBeforeDiscount() {
        return amountBeforeDiscount;
    }

    public void setAmountBeforeDiscount(double amountBeforeDiscount) {
        this.amountBeforeDiscount = amountBeforeDiscount;
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

    public boolean equals(Object o) {
        if (!(o instanceof Item)) {
            return false;
        }
        Item other = (Item) o;
        return this.id.equals(other.id);
    }

    public int hashCode() {
        return Objects.hash(id);
    }
}
