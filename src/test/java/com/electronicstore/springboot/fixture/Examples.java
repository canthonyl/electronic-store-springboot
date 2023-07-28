package com.electronicstore.springboot.fixture;

import com.electronicstore.springboot.model.Product;
import com.electronicstore.springboot.model.ShoppingCart;
import com.electronicstore.springboot.model.ShoppingCartItem;

import java.util.List;

public class Examples {

    public static Product ofProduct(long id, String name, String description, double price, long categoryId) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setDescription(description);
        p.setPrice(price);
        p.setCategoryId(categoryId);
        return p;
    }

    public static Product ofProduct(String name, String description, double price, long categoryId) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setPrice(price);
        p.setCategoryId(categoryId);
        return p;
    }


    public static Product product1 = ofProduct(1L,"Apple Macbook Pro", "Apple Macbook Pro", 11000, 2L);
    public static Product product2 = ofProduct(2L,"Dell Desktop", "Dell Desktop", 5000, 1L);
    public static Product product3 = ofProduct(3L, "Mechanical Keyboard", "Mechanical Keyboard", 678, 3L);

    public static List<Product> productList = List.of(product1, product2, product3);

    public static ShoppingCart shoppingCart1 = new ShoppingCart(1L);

    public static ShoppingCart shoppingCart2 = new ShoppingCart(2L);
    static {
        ShoppingCartItem item1 = new ShoppingCartItem();
        item1.setId(1L);
        item1.setQuantity(1);
        item1.setPrice(11000);
        item1.setAmountBeforeDiscount(11000);
        item1.setDiscountAmount(2000);
        item1.setAmount(9000);
        item1.setProduct(product1);

        shoppingCart2.setItems(List.of(item1));
    }

}
