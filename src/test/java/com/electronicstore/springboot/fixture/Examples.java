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
        ShoppingCartItem cart2Item1 = new ShoppingCartItem();
        cart2Item1.setId(1L);
        cart2Item1.setShoppingCart(shoppingCart2);
        cart2Item1.setQuantity(1);
        cart2Item1.setPrice(11000);
        cart2Item1.setAmountBeforeDiscount(11000);
        cart2Item1.setDiscountAmount(2000);
        cart2Item1.setAmount(9000);
        cart2Item1.setProduct(product1);

        shoppingCart2.setItems(List.of(cart2Item1));
    }
    public static List<ShoppingCartItem> shoppingCart2Items = List.of(
           new ShoppingCartItem(1L, 1L, 1L)
    );

}
