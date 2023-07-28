package com.electronicstore.springboot.controller;


import com.electronicstore.springboot.context.CommonContext;
import com.electronicstore.springboot.model.Product;
import com.electronicstore.springboot.model.ShoppingCart;
import com.electronicstore.springboot.model.ShoppingCartItem;
import com.electronicstore.springboot.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static net.javacrumbs.jsonunit.JsonMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.*;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Profile("test")
//@ActiveProfiles("test")
public class ShoppingCartControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CommonContext appContext;

    @MockBean
    private ShoppingCartService shoppingCartService;

    ShoppingCart shoppingCartWithZeroItem = new ShoppingCart(1L);
    ShoppingCart shoppingCartWithOneItem = new ShoppingCart(2L);
    Product appleMacbookPro = new Product(10L, "Apple MacbookPro", "Apple Macbook Pro");
    ShoppingCartItem oneItem = new ShoppingCartItem(shoppingCartWithOneItem,3L, appleMacbookPro);

    //@Test
    public void testNewShoppingCart(){
        String requestJson = "{\"items\": []}";

        when(shoppingCartService.createShoppingCart(anyList())).thenReturn(shoppingCartWithZeroItem);

        String productUri = appContext.getBaseUriBuilder().path("shoppingCarts").toUriString();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("content-type","application/json");
        HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> response = restTemplate.exchange(productUri, HttpMethod.POST, requestEntity, String.class);

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());
        String responseJson = response.getBody();

        assertThat(responseJson, jsonNodePresent("id"));
        assertThat(responseJson, jsonNodePresent("items"));
        assertThat(responseJson, jsonPartEquals("id", 1));
    }

    //@Test
    public void testNewShoppingCartWithItems(){
        String requestJson = "{\"items\": ["+
                    "{\"product\":{\"id\":"+10+"}}"
                    +"]}";

        shoppingCartWithOneItem.setItems(List.of(oneItem));
        when(shoppingCartService.createShoppingCart(anyList())).thenReturn(shoppingCartWithOneItem);

        String productUri = appContext.getBaseUriBuilder().path("shoppingCarts").toUriString();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(CONTENT_TYPE,"application/json");
        HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> response = restTemplate.exchange(productUri, HttpMethod.POST, requestEntity, String.class);
        HttpHeaders httpHeaders = response.getHeaders();

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());

        String contentLocation = appContext.getBaseUriBuilder().pathSegment("shoppingCarts","{id}").buildAndExpand(shoppingCartWithOneItem.getId()).toUriString();
        assertEquals(List.of(contentLocation), httpHeaders.get(LOCATION));

        String responseJson = response.getBody();

        assertThat(responseJson, jsonNodePresent("id"));
        assertThat(responseJson, jsonNodePresent("items[0]"));
        assertThat(responseJson, jsonPartEquals("id", 2));
        assertThat(responseJson, jsonPartEquals("items[0].id", 3));
        assertThat(responseJson, jsonPartEquals("items[0].product.id", 10));
    }

    //@Test
    public void testGetShoppingCart(){
        shoppingCartWithOneItem.setItems(List.of(oneItem));
        when(shoppingCartService.getShoppingCart(2L)).thenReturn(Optional.of(shoppingCartWithOneItem));

        String productUri = appContext.getBaseUriBuilder().pathSegment("shoppingCarts","{id}").buildAndExpand(2).toUriString();
        ResponseEntity<String> response = restTemplate.getForEntity(productUri, String.class);

        String responseJson = response.getBody();
        assertThat(responseJson, jsonNodePresent("id"));
        assertThat(responseJson, jsonNodePresent("items[0]"));
        assertThat(responseJson, jsonPartEquals("id", 2));
        assertThat(responseJson, jsonPartEquals("items[0].id", 3));
        assertThat(responseJson, jsonPartEquals("items[0].product.id", 10));
    }
}
