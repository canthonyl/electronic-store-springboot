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

    /*@Autowired
    ShoppingCartTestItemRepository testItemRepository;*/

    @Test
    public void testSaveShoppingCartReturnsUniqueIdentifier(){
        ShoppingCart shoppingCart1 = new ShoppingCart();
        cartRepository.save(shoppingCart1);
        ShoppingCart shoppingCart2 = new ShoppingCart();
        cartRepository.save(shoppingCart2);
        assertEquals(1L, shoppingCart1.getId());
        assertEquals(2L, shoppingCart2.getId());
    }

   /* @Test
    public void testItem(){

        productRepository.save(Examples.product1);
        productRepository.save(Examples.product2);
        productRepository.save(Examples.product3);

        assertTrue(productRepository.existsById(1L));
        assertTrue(productRepository.existsById(2L));
        assertTrue(productRepository.existsById(3L));

        Product product1 = new Product(1L);
        Product product2 = new Product(2L);
        Product product3 = new Product(3L);

        ShoppingCart cart1 = new ShoppingCart();
        cart1.setTestItem(List.of(new TestItem("cart 1 item 1", product2)));
        cartRepository.save(cart1);

        ShoppingCart cart2 = new ShoppingCart();
        cart2.setTestItem(List.of(
                new TestItem("cart 2 item 1", product3),
                new TestItem("cart 2 item 2", product1)));
        cartRepository.save(cart2);

        List<ShoppingCartTestItem> all = testItemRepository.findAll();
        assertEquals(3, all.size());

        Collections.sort(all, Comparator.comparingLong(ShoppingCartTestItem::getId));

        assertEquals(1L, all.get(0).getId());
        assertEquals(2L, all.get(0).getProductId());
        assertEquals("cart 1 item 1", all.get(0).getText());

        assertEquals(2L, all.get(1).getId());
        assertEquals(3L, all.get(1).getProductId());
        assertEquals("cart 2 item 1", all.get(1).getText());

        assertEquals(3L, all.get(2).getId());
        assertEquals(1L, all.get(2).getProductId());
        assertEquals("cart 2 item 2", all.get(2).getText());
    }*/

    @Test
    public void testSaveShoppingCartWithItemsReturnsUniqueIdentifiers(){
        productRepository.save(Examples.product1);
        productRepository.save(Examples.product2);
        productRepository.save(Examples.product3);

        assertTrue(productRepository.existsById(1L));
        assertTrue(productRepository.existsById(2L));
        assertTrue(productRepository.existsById(3L));

        ShoppingCart instance1 = new ShoppingCart();
        instance1.setItems(List.of(
            new Item(1L, 3),
            new Item(2L, 2),
            new Item(3L, 1)
        ));

        cartRepository.save(instance1);
        assertEquals(1L, instance1.getId());
        /*shoppingCartRepository.refresh(instance1);*/

        ShoppingCart instance2 = cartRepository.findById(1L).get();
        assertEquals(1L, instance2.getItems().get(0).getId());
        assertEquals(2L, instance2.getItems().get(1).getId());
        assertEquals(3L, instance2.getItems().get(2).getId());

        assertEquals(3, instance2.getItems().get(0).getQuantity());
        assertEquals(2, instance2.getItems().get(1).getQuantity());
        assertEquals(1, instance2.getItems().get(2).getQuantity());

        ShoppingCartItem item1Request = new ShoppingCartItem(1L, 1L, 1L);
        ShoppingCartItem item2Request = new ShoppingCartItem(2L, 1L, 1L);
        ShoppingCartItem item3Request = new ShoppingCartItem(3L, 1L, 1L);

        item1Request.setQuantity(13);
        item2Request.setQuantity(12);
        item3Request.setQuantity(11);

        /*cartItemRepository.save(item1Request);
        cartItemRepository.save(item2Request);
        cartItemRepository.save(item3Request);*/
        cartItemRepository.saveAll(List.of(item1Request, item2Request, item3Request));

        assertEquals(13, cartItemRepository.findById(1L).get().getQuantity());
        assertEquals(12, cartItemRepository.findById(2L).get().getQuantity());
        assertEquals(11, cartItemRepository.findById(3L).get().getQuantity());

        instance1 = cartRepository.findById(1L).get();
        assertEquals(13, instance1.getItems().get(0).getQuantity());
        assertEquals(12, instance1.getItems().get(1).getQuantity());
        assertEquals(11, instance1.getItems().get(2).getQuantity());

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

        List<Item> request = List.of(
                new Item(1L, 2),
                new Item(2L, 1)
        );

        cart1.setItems(request);
        cartRepository.save(cart1);

        cart1 = cartRepository.findById(1L).get();
        assertEquals(1L, cart1.getItems().get(0).getId());
        assertEquals(2L, cart1.getItems().get(1).getId());
        assertEquals(2, cart1.getItems().get(0).getQuantity());
        assertEquals(1, cart1.getItems().get(1).getQuantity());


        ShoppingCartItem newItem1 = new ShoppingCartItem(1L, 3L, 3);
        ShoppingCartItem newItem2 = new ShoppingCartItem(1L, 4L, 1);
        cartItemRepository.saveAll(List.of(newItem1, newItem2));

        /*cart1.getItems().add(new Item(3L, 3));
        cartRepository.save(cart1);*/

        cart1 = cartRepository.findById(1L).get();
        Map<Long, Item> map = cart1.getItems().stream().collect(Collectors.toMap(Item::getId, Function.identity()));

        assertEquals(2, map.get(1L).getQuantity());
        assertEquals(1, map.get(2L).getQuantity());
        assertEquals(3, map.get(3L).getQuantity());
        assertEquals(1, map.get(4L).getQuantity());
    }

}
