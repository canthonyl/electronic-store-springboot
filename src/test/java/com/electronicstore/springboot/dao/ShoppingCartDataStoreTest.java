package com.electronicstore.springboot.dao;

import com.electronicstore.springboot.fixture.Examples;
import com.electronicstore.springboot.model.Product;
import com.electronicstore.springboot.model.ShoppingCart;
import com.electronicstore.springboot.model.TestItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@ActiveProfiles("test")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
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

    @Test
    public void testSaveShoppingCartWithItemsReturnsUniqueIdentifiers(){
        //ensure product is created
        productRepository.save(Examples.product1);
        productRepository.save(Examples.product2);
        productRepository.save(Examples.product3);

        assertTrue(productRepository.existsById(1L));
        assertTrue(productRepository.existsById(2L));
        assertTrue(productRepository.existsById(3L));

        Product product1 = new Product(1L);
        Product product2 = new Product(2L);
        Product product3 = new Product(3L);

        ShoppingCart instance1 = new ShoppingCart();
        instance1.setTestItem(List.of(
            new TestItem("Test Item 1"),
            new TestItem("Test Item 2"),
            new TestItem("Test Item 3"))
        );
        shoppingCartRepository.save(instance1);
        assertEquals(1L, instance1.getId());
        /*assertEquals(1L, instance1.getTestItem().get(0).getId());
        assertEquals(2L, instance1.getTestItem().get(1).getId());
        assertEquals(3L, instance1.getTestItem().get(2).getId());*/

        ShoppingCart instance2 = shoppingCartRepository.findById(1L).get();
        assertEquals(1L, instance2.getTestItem().get(0).getId());
        assertEquals(2L, instance2.getTestItem().get(1).getId());
        assertEquals(3L, instance2.getTestItem().get(2).getId());

        //keep behavior in test case
        assertNotEquals(instance1, instance2);
    }



}
