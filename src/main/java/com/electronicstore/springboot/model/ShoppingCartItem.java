package com.electronicstore.springboot.model;

//import javax.persistence.*;
import jakarta.persistence.*;

import java.util.List;
import java.util.Objects;

@Entity
//@IdClass(ShoppingCartItemId.class)
public class ShoppingCartItem {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator="shopping_cart_item_id_seq")
    @SequenceGenerator(name = "shopping_cart_item_id_seq", sequenceName = "shopping_cart_item_id_seq", allocationSize = 1)
    private Long id;

    //@Id
    //private Long shoppingCartId;

    //bi-directional
    @ManyToOne//(cascade = CascadeType.DETACH)
    @JoinColumn(name="shopping_cart_id", referencedColumnName="id")
    private ShoppingCart shoppingCart;

    @ManyToOne
    private Product product;

    private int quantity;

    private double price;

    private double amountBeforeDiscount;

    private double discountAmount;

    private double amount;

    private String productOptions;

    private transient List<String> discountApplied;

    //private Map<String, String> attributes;

    public ShoppingCartItem() {
        //attributes = new LinkedHashMap<>();
    }

    public ShoppingCartItem(ShoppingCart shoppingCart, Long id) {
        this();
        this.shoppingCart = shoppingCart;
        this.id = id;
    }

    public ShoppingCartItem(ShoppingCart shoppingCart, Long id, Product product) {
        this(shoppingCart, id);
        this.product = product;
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

    public ShoppingCart getShoppingCart() {
        return shoppingCart;
    }

    public void setShoppingCart(ShoppingCart shoppingCart) {
        this.shoppingCart = shoppingCart;
    }

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

    public String getProductOptions() {
        return productOptions;
    }

    public void setProductOptions(String productOptions) {
        this.productOptions = productOptions;
    }

    public List<String> getDiscountApplied() {
        return discountApplied;
    }

    public void setDiscountApplied(List<String> discountApplied) {
        this.discountApplied = discountApplied;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ShoppingCartItem)) {
            return false;
        }
        ShoppingCartItem other = (ShoppingCartItem) o;
        return this.id.equals(other.id);
    }

    public int hashCode() {
        return Objects.hash(id);
    }
}
