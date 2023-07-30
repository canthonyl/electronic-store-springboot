package com.electronicstore.springboot.dao.test;

import com.electronicstore.springboot.dao.test.test.CartItemRepository;
import com.electronicstore.springboot.dao.test.test.CartRepository;
import com.electronicstore.springboot.model.test.Cart;
import com.electronicstore.springboot.model.test.CartItem;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

//@ActiveProfiles("test")
//@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestRepo {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    //@Test
    public void createNewCartWithNoItems(){
        Cart cart = new Cart();
        cart = cartRepository.save(cart);
        assertEquals(1L, cart.getId());
    }

    //@Test
    public void createNewCartWithInitialItems(){
        Cart cart = new Cart();
        List<CartItem> items = List.of(new CartItem(), new CartItem(), new CartItem());
        cart.setItems(items);
        cart.getItems().get(0).setQuantity(1);
        cart.getItems().get(1).setQuantity(2);
        cart.getItems().get(2).setQuantity(3);

        cartRepository.save(cart);
        assertEquals(1L, cart.getId());
        assertEquals(1L, cart.getItems().get(0).getId());
        assertEquals(2L, cart.getItems().get(1).getId());
        assertEquals(3L, cart.getItems().get(2).getId());
    }

    //@Test
    public void attachNewItemsToExistingCart(){
        Cart cart = new Cart();
        cartRepository.save(cart);
        Cart cart2 = new Cart();
        cartRepository.save(cart2);

        Cart cart1Req = new Cart(); cart1Req.setId(1L);
        List<CartItem> cart1Items = List.of(new CartItem());
        cart1Items.get(0).setQuantity(1);
        cart1Items.get(0).setCart(cart1Req);
        cartItemRepository.saveAll(cart1Items);

        Cart cart2Req = new Cart(); cart2Req.setId(2L);
        List<CartItem> cart2Items = List.of(new CartItem(), new CartItem());
        cart2Items.get(0).setQuantity(2);
        cart2Items.get(0).setCart(cart2Req);
        cart2Items.get(1).setQuantity(3);
        cart2Items.get(1).setCart(cart2Req);
        cartItemRepository.saveAll(cart2Items);

        assertEquals(1L, cart1Items.get(0).getId());
        assertEquals(2L, cart2Items.get(0).getId());
        assertEquals(3L, cart2Items.get(1).getId());
    }

}
