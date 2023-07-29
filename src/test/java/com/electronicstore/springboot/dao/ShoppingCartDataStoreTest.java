package com.electronicstore.springboot.dao;

import com.electronicstore.springboot.model.Product;
import com.electronicstore.springboot.model.ShoppingCart;
import com.electronicstore.springboot.model.ShoppingCartItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShoppingCartDataStoreTest {

    @Autowired
    ShoppingCartRepository shoppingCartRepository;

    @Autowired
    ShoppingCartItemRepository shoppingCartItemRepository;

    @Autowired
    ProductRepository productRepository;

    @Test
    public void testSaveShoppingCartReturnsUniqueIdentifier(){
        ShoppingCart shoppingCart1 = new ShoppingCart();
        shoppingCartRepository.save(shoppingCart1);
        ShoppingCart shoppingCart2 = new ShoppingCart();
        shoppingCartRepository.save(shoppingCart2);
        assertEquals(1L, shoppingCart1.getId());
        assertEquals(2L, shoppingCart2.getId());
    }

    //@Test
    public void testSaveShoppingCartWithItemsReturnsUniqueIdentifiers(){
        //ensure product is created
        assertTrue(productRepository.existsById(1L));
        assertTrue(productRepository.existsById(2L));
        assertTrue(productRepository.existsById(3L));

        Product product1 = new Product(1L);
        Product product2 = new Product(2L);
        Product product3 = new Product(3L);

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setItems(List.of(
                new ShoppingCartItem(product1, 1),
                new ShoppingCartItem(product2, 1),
                new ShoppingCartItem(product3, 1)));

        shoppingCartRepository.save(shoppingCart);
        assertEquals(1L, shoppingCart.getItems().get(0).getId());

    }



}
