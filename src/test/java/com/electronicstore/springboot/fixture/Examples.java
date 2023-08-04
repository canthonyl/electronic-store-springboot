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

    public static ShoppingCart shoppingCart2WithProduct1 = new ShoppingCart(2L);
    public static ShoppingCart shoppingCart2WithProduct12 = new ShoppingCart(2L);
    public static ShoppingCart shoppingCart2WithProduct123 = new ShoppingCart(2L);

    public static ShoppingCartItem calculatedCart2Product1Qty1 = new ShoppingCartItem(2L, 1L, 1);
    public static ShoppingCartItem calculatedCart2Product2Qty1 = new ShoppingCartItem(2L, 2L, 1);
    public static ShoppingCartItem calculatedCart2Product3Qty1 = new ShoppingCartItem(2L, 3L, 1);

    public static ShoppingCart shoppingCart2WithProduct1Request = new ShoppingCart(2L);
    public static ShoppingCart shoppingCart2WithProduct12Request = new ShoppingCart(2L);
    public static ShoppingCart shoppingCart2WithProduct123Request = new ShoppingCart(2L);

    public static ShoppingCartItem cart2Product1Qty1Request = new ShoppingCartItem(2L, 1L, 1);
    public static ShoppingCartItem cart2Product2Qty1Request = new ShoppingCartItem(2L, 2L, 1);
    public static ShoppingCartItem cart2Product3Qty1Request = new ShoppingCartItem(2L, 3L, 1);


    static {
        /*ShoppingCartItem itemWithCalculation = new ShoppingCartItem();
        itemWithCalculation.setId(1L);
        itemWithCalculation.setShoppingCart(shoppingCart2WithProduct1);
        itemWithCalculation.setProductId(product1.getId());
        itemWithCalculation.setQuantity(1);
        itemWithCalculation.setPrice(11000);
        itemWithCalculation.setAmountBeforeDiscount(11000);
        itemWithCalculation.setDiscountAmount(2000);
        itemWithCalculation.setAmount(9000);*/
        //shoppingCart2WithProduct1.setItems(List.of(itemWithCalculation));

        shoppingCart2WithProduct1Request.setItems(List.of(cart2Product1Qty1Request));
        shoppingCart2WithProduct12Request.setItems(List.of(cart2Product1Qty1Request, cart2Product2Qty1Request));
        shoppingCart2WithProduct123Request.setItems(List.of(cart2Product1Qty1Request, cart2Product2Qty1Request, cart2Product3Qty1Request));

        calculatedCart2Product1Qty1.setPrice(11000.0);
        calculatedCart2Product1Qty1.setAmountBeforeDiscount(11000.0);
        calculatedCart2Product1Qty1.setDiscountAmount(2000.0);
        calculatedCart2Product1Qty1.setAmount(9000.0);

        calculatedCart2Product2Qty1.setPrice(600.0);
        calculatedCart2Product2Qty1.setAmountBeforeDiscount(600.0);
        calculatedCart2Product2Qty1.setDiscountAmount(100.0);
        calculatedCart2Product2Qty1.setAmount(500.0);

        calculatedCart2Product3Qty1.setPrice(500.0);
        calculatedCart2Product3Qty1.setAmountBeforeDiscount(500.0);
        calculatedCart2Product3Qty1.setDiscountAmount(100.0);
        calculatedCart2Product3Qty1.setAmount(400.0);

        shoppingCart2WithProduct1.setItems(List.of(calculatedCart2Product1Qty1));
        shoppingCart2WithProduct1.setTotalAmountBeforeDiscount(11000);
        shoppingCart2WithProduct1.setTotalDiscountAmount(2000);
        shoppingCart2WithProduct1.setTotalAmount(9000);

        shoppingCart2WithProduct12.setItems(List.of(calculatedCart2Product1Qty1, calculatedCart2Product2Qty1));
        shoppingCart2WithProduct12.setTotalAmountBeforeDiscount(11600);
        shoppingCart2WithProduct12.setTotalDiscountAmount(2100);
        shoppingCart2WithProduct12.setTotalAmount(9500);

        shoppingCart2WithProduct123.setItems(List.of(calculatedCart2Product1Qty1, calculatedCart2Product2Qty1, calculatedCart2Product3Qty1));
        shoppingCart2WithProduct123.setTotalAmountBeforeDiscount(12100);
        shoppingCart2WithProduct123.setTotalDiscountAmount(2200);
        shoppingCart2WithProduct123.setTotalAmount(9900);
    }


}
