package com.electronicstore.springboot.service;

import com.electronicstore.springboot.dao.EntityDatastore;
import com.electronicstore.springboot.fixture.Examples;
import com.electronicstore.springboot.model.ShoppingCart;
import com.electronicstore.springboot.model.ShoppingCartItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShoppingCartServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private EntityDatastore<ShoppingCart> cartDatastore;

    @Autowired
    private EntityDatastore<ShoppingCartItem> cartItemDatastore;


    @BeforeEach
    public void setupProducts(){
        productService.addProducts(List.of(Examples.product1, Examples.product2, Examples.product3));
    }

    @Test
    public void persistedShoppingCart_hasIdAndDefaultValuesPopulated(){
        ShoppingCart request = new ShoppingCart(
                List.of(new ShoppingCartItem(1L, 2),
                        new ShoppingCartItem(2L, 2),
                        new ShoppingCartItem(3L, 2)));

        ShoppingCart resultShoppingCart = shoppingCartService.createShoppingCart(request);

        assertEquals(1L, resultShoppingCart.getId());
        assertEquals(1L, resultShoppingCart.getItems().get(0).getId());
        assertEquals(2L, resultShoppingCart.getItems().get(1).getId());
        assertEquals(3L, resultShoppingCart.getItems().get(2).getId());
    }

    @Test
    public void addItemToShoppingCart_cartItemHasIdAndDefaultValuesPopulated(){
        ShoppingCart request = new ShoppingCart(
                List.of(new ShoppingCartItem(1L, 2),
                        new ShoppingCartItem(2L, 2)));

        ShoppingCart resultShoppingCart = shoppingCartService.createShoppingCart(request);
        shoppingCartService.refreshShoppingCart(resultShoppingCart);
        assertEquals(2, resultShoppingCart.getItems().size());

        ShoppingCartItem newItem = ShoppingCartItem.ofProduct(3L, 2);
        shoppingCartService.addShoppingCartItems(1L, List.of(newItem));

        resultShoppingCart = shoppingCartService.getShoppingCart(1L).get();
        assertEquals(3, resultShoppingCart.getItems().size());
        assertEquals(3L, resultShoppingCart.getItems().get(2).getId());
    }




}
