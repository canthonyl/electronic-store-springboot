package com.electronicstore.springboot.dao;

import com.electronicstore.springboot.fixture.Examples;
import com.electronicstore.springboot.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@ActiveProfiles("test")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShoppingCartDataStoreTest {

    @Autowired
    ShoppingCartRepository cartRepository;

    @Autowired
    ShoppingCartItemRepository cartItemRepository;

    @Autowired
    ProductRepository productRepository;

    @Test
    public void testSaveShoppingCartReturnsUniqueIdentifier(){
        ShoppingCart shoppingCart1 = new ShoppingCart();
        cartRepository.save(shoppingCart1);
        ShoppingCart shoppingCart2 = new ShoppingCart();
        cartRepository.save(shoppingCart2);
        assertEquals(1L, shoppingCart1.getId());
        assertEquals(2L, shoppingCart2.getId());
    }


    @Test
    public void testSaveShoppingCartWithItemsReturnsUniqueIdentifiers(){
        productRepository.save(Examples.product1);
        productRepository.save(Examples.product2);
        productRepository.save(Examples.product3);

        assertTrue(productRepository.existsById(1L));
        assertTrue(productRepository.existsById(2L));
        assertTrue(productRepository.existsById(3L));

        ShoppingCart instance1 = new ShoppingCart(List.of(
                new ShoppingCartItem(1L, 3),
                new ShoppingCartItem(2L, 2),
                new ShoppingCartItem(3L, 1)
        ));
        assertNull(instance1.getId());
        assertNull(instance1.getItems().get(0).getId());
        assertNull(instance1.getItems().get(1).getId());
        assertNull(instance1.getItems().get(2).getId());
        assertNull(instance1.getItems().get(0).getShoppingCart().getId());

        cartRepository.save(instance1);
        assertEquals(1L, instance1.getId());
        assertEquals(1L, instance1.getItems().get(0).getId());
        assertEquals(2L, instance1.getItems().get(1).getId());
        assertEquals(3L, instance1.getItems().get(2).getId());
        assertEquals(1L, instance1.getItems().get(0).getShoppingCart().getId());

        ShoppingCart instance2 = cartRepository.findById(1L).get();
        assertEquals(3, instance2.getItems().size());
        assertEquals(1L, instance2.getItems().get(0).getId());
        assertEquals(2L, instance2.getItems().get(1).getId());
        assertEquals(3L, instance2.getItems().get(2).getId());

        assertEquals(3, instance2.getItems().get(0).getQuantity());
        assertEquals(2, instance2.getItems().get(1).getQuantity());
        assertEquals(1, instance2.getItems().get(2).getQuantity());
/*
        ShoppingCartItem item1Request = new ShoppingCartItem(1L, 1L, 1L);
        ShoppingCartItem item2Request = new ShoppingCartItem(2L, 1L, 1L);
        ShoppingCartItem item3Request = new ShoppingCartItem(3L, 1L, 1L);

        item1Request.setQuantity(13);
        item2Request.setQuantity(12);
        item3Request.setQuantity(11);*/

        /*cartItemRepository.save(item1Request);
        cartItemRepository.save(item2Request);
        cartItemRepository.save(item3Request);*/
        /*cartItemRepository.saveAll(List.of(item1Request, item2Request, item3Request));

        assertEquals(13, cartItemRepository.findById(1L).get().getQuantity());
        assertEquals(12, cartItemRepository.findById(2L).get().getQuantity());
        assertEquals(11, cartItemRepository.findById(3L).get().getQuantity());

        instance1 = cartRepository.findById(1L).get();
        assertEquals(13, instance1.getItems().get(0).getQuantity());
        assertEquals(12, instance1.getItems().get(1).getQuantity());
        assertEquals(11, instance1.getItems().get(2).getQuantity());*/

    }

    @Test
    public void attachItemsToExistingShoppingCart(){
        productRepository.save(Examples.product1);
        productRepository.save(Examples.product2);
        productRepository.save(Examples.product3);
        productRepository.save(new Product(4L, "Apple iPad Pro", "Apple iPad Pro", 7000.0, 3L));

        assertTrue(productRepository.existsById(1L));
        assertTrue(productRepository.existsById(2L));
        assertTrue(productRepository.existsById(3L));
        assertTrue(productRepository.existsById(4L));

        ShoppingCart cart1 = cartRepository.save(new ShoppingCart());
        assertEquals(1L, cart1.getId());

        List<ShoppingCartItem> request = List.of(
                new ShoppingCartItem(1L,1L, 2),
                new ShoppingCartItem(1L,2L, 1)
        );
        cartItemRepository.saveAll(request);
        assertEquals(1L, request.get(0).getId());
        assertEquals(2L, request.get(1).getId());


        /*cart1.setItems(request);
        cartRepository.save(cart1);

        cart1 = cartRepository.findById(1L).get();
        assertEquals(1L, cart1.getItems().get(0).getId());
        assertEquals(2L, cart1.getItems().get(1).getId());
        assertEquals(2, cart1.getItems().get(0).getQuantity());
        assertEquals(1, cart1.getItems().get(1).getQuantity());


        ShoppingCartItem newItem1 = new ShoppingCartItem(1L, 3L, 3);
        ShoppingCartItem newItem2 = new ShoppingCartItem(1L, 4L, 1);
        cartItemRepository.saveAll(List.of(newItem1, newItem2));*/

        /*cart1.getItems().add(new Item(3L, 3));
        cartRepository.save(cart1);*/

        /*cart1 = cartRepository.findById(1L).get();
        Map<Long, ShoppingCartItem> map = cart1.getItems().stream().collect(Collectors.toMap(ShoppingCartItem::getId, Function.identity()));

        assertEquals(2, map.get(1L).getQuantity());
        assertEquals(1, map.get(2L).getQuantity());
        assertEquals(3, map.get(3L).getQuantity());
        assertEquals(1, map.get(4L).getQuantity());*/
    }

}
