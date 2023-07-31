package com.electronicstore.springboot.model.test;

import jakarta.persistence.*;

@Entity
//@Table(name="cart_item")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cart_item_id_seq")
    @SequenceGenerator(name = "cart_item_id_seq", sequenceName = "cart_item_id_seq", allocationSize = 1)
    private Long id;

    /*
    @Column(name="cart_id", table="cart_item")
    private Long cartId;
    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId;}
    */

    @ManyToOne
    private Cart cart;

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    private int quantity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
