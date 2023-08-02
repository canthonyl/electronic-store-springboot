package com.electronicstore.springboot.controller;


import com.electronicstore.springboot.context.CommonContext;
import com.electronicstore.springboot.dto.ShoppingCartRequest;
import com.electronicstore.springboot.model.ShoppingCart;
import com.electronicstore.springboot.model.ShoppingCartItem;
import com.electronicstore.springboot.service.ShoppingCartService;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Optional;

import static com.electronicstore.springboot.fixture.Examples.*;
import static net.javacrumbs.jsonunit.JsonMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;


@ActiveProfiles("test")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ShoppingCartControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CommonContext appContext;

    @MockBean
    private ShoppingCartService shoppingCartService;

    private Gson gson = new Gson();

    @Test
    public void httpGet_shoppingCart_OK(){
        when(shoppingCartService.getShoppingCart(2L)).thenReturn(Optional.of(shoppingCart2WithProduct123));

        RequestEntity<Void> request = RequestEntity.get(shoppingCartUrl(2L)).build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        HttpStatusCode statusCode = response.getStatusCode();
        assertEquals(HttpStatus.OK.value(), statusCode.value());

        String responseJson = response.getBody();
        assertThat(responseJson, jsonNodePresent("id"));
        assertThat(responseJson, jsonNodePresent("items[0]"));
        assertThat(responseJson, jsonNodePresent("items[1]"));
        assertThat(responseJson, jsonNodePresent("items[2]"));
    }

    @Test
    public void httpGet_shoppingCart_NotFound(){
        when(shoppingCartService.getShoppingCart(3L)).thenReturn(Optional.empty());

        RequestEntity<Void> request = RequestEntity.get(shoppingCartUrl(3L)).build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        HttpStatusCode statusCode = response.getStatusCode();
        assertEquals(HttpStatus.NOT_FOUND.value(), statusCode.value());
    }

    @Test
    public void httpGet_cartItem_OK(){
        when(shoppingCartService.shoppingCartExists(2L)).thenReturn(true);
        when(shoppingCartService.getShoppingCartItem(2L, 1L)).thenReturn(Optional.of(calculatedCart2Product1Qty1));

        RequestEntity<Void> request = RequestEntity.get(cartItemUrl(2L, 1L)).build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        HttpStatusCode statusCode = response.getStatusCode();
        assertEquals(HttpStatus.OK.value(), statusCode.value());

        String body = response.getBody();
        assertThat(body, jsonNodePresent("id"));
        assertThat(body, jsonNodePresent("productId"));
        assertThat(body, jsonNodePresent("amount"));
    }

    @Test
    public void httpGet_cartItem_NotFound(){
        when(shoppingCartService.shoppingCartExists(2L)).thenReturn(true);
        when(shoppingCartService.getShoppingCartItem(2L, 2L)).thenReturn(Optional.empty());

        RequestEntity<Void> request = RequestEntity.get(cartItemUrl(2L, 2L)).build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        HttpStatusCode statusCode = response.getStatusCode();
        assertEquals(HttpStatus.NOT_FOUND.value(), statusCode.value());
    }

    @Test
    public void httpPost_newShoppingCartWithItems_created(){
        when(shoppingCartService.createShoppingCart(any())).thenReturn(shoppingCart2WithProduct1);
        when(shoppingCartService.getShoppingCart(2L)).thenReturn(Optional.of(shoppingCart2WithProduct1));

        String shoppingCartsUrl = appContext.getBaseUriBuilder().path("shoppingCarts").toUriString();

        ShoppingCartRequest cartRequest = new ShoppingCartRequest();
        cartRequest.setResponseType(ShoppingCartRequest.ResponseType.ShoppingCart);
        cartRequest.setShoppingCartItems(shoppingCart2WithProduct1Request.getItems());

        RequestEntity<String> request = RequestEntity.post(shoppingCartsUrl).contentType(MediaType.APPLICATION_JSON).body(gson.toJson(cartRequest));
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        HttpStatusCode statusCode = response.getStatusCode();
        assertEquals(HttpStatus.CREATED.value(), statusCode.value());

        HttpHeaders headers = response.getHeaders();
        assertEquals(shoppingCartUrl(2L), headers.get(HttpHeaders.LOCATION).get(0));

        String responseJson = response.getBody();
        System.out.println("responseJson = "+responseJson);
        assertThat(responseJson, jsonNodePresent("shoppingCart.id"));
        assertThat(responseJson, jsonNodePresent("shoppingCart.items"));
        assertThat(responseJson, jsonNodePresent("shoppingCart.totalAmount"));
    }

    @Test
    public void httpPost_addItemToShoppingCart() {
        when(shoppingCartService.shoppingCartExists(2L)).thenReturn(true);
        when(shoppingCartService.getShoppingCart(2L)).thenReturn(Optional.of(shoppingCart2WithProduct12));

        ShoppingCartRequest cartRequest = new ShoppingCartRequest();
        cartRequest.setResponseType(ShoppingCartRequest.ResponseType.None);
        cartRequest.setShoppingCartItems(List.of(cart2Product2Qty1Request));

        RequestEntity<String> request = RequestEntity.post(postItemsToShoppingCartUrl(2L))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(gson.toJson(cartRequest));

        ResponseEntity<String> response =  restTemplate.exchange(request, String.class);

        HttpStatusCode httpStatus = response.getStatusCode();
        assertEquals(HttpStatus.OK.value(), httpStatus.value());
    }

    @Test
    public void httpPost_addItemToShoppingCart_withEntireShoppingCartResponse() {
        when(shoppingCartService.shoppingCartExists(2L)).thenReturn(true);
        when(shoppingCartService.getShoppingCart(2L)).thenReturn(Optional.of(shoppingCart2WithProduct12));

        ShoppingCartRequest cartRequest = new ShoppingCartRequest();
        cartRequest.setResponseType(ShoppingCartRequest.ResponseType.ShoppingCart);
        cartRequest.setShoppingCartItems(List.of(cart2Product2Qty1Request));

        RequestEntity<String> request = RequestEntity.post(postItemsToShoppingCartUrl(2L))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(gson.toJson(cartRequest));

        ResponseEntity<String> response =  restTemplate.exchange(request, String.class);

        HttpStatusCode httpStatusCode = response.getStatusCode();
        assertEquals(HttpStatus.OK.value(), httpStatusCode.value());

        String responseJson = response.getBody();
        assertThat(responseJson, jsonPartEquals("shoppingCart.id", 2L));
        assertThat(responseJson, jsonNodePresent("shoppingCart.items[0]"));
        assertThat(responseJson, jsonNodePresent("shoppingCart.items[1]"));
    }

    @Test
    public void httpPut_updateItemQuantity_queryParam(){
        when(shoppingCartService.getShoppingCartItem(2L, 1L)).thenReturn(Optional.of(calculatedCart2Product1Qty1));

        String url = putUpdateItemsQuantityUrl(2L, 1L, 2);
        RequestEntity<Void> request = RequestEntity.put(url).build();
        ResponseEntity<Void> response =  restTemplate.exchange(request, Void.class);

        HttpStatusCode httpStatus = response.getStatusCode();
        assertEquals(HttpStatus.NO_CONTENT.value(), httpStatus.value());
    }

    @Test
    public void httpPut_updateItemQuantity_jsonRequest(){
        when(shoppingCartService.getShoppingCartItem(2L, 1L)).thenReturn(Optional.of(calculatedCart2Product1Qty1));

        String url = cartItemUrl(2L, 1L);
        ShoppingCartItem item = new ShoppingCartItem(1L, 1);

        RequestEntity<String> request = RequestEntity.put(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(gson.toJson(item));

        ResponseEntity<Void> response =  restTemplate.exchange(request, Void.class);

        HttpStatusCode httpStatus = response.getStatusCode();
        assertEquals(HttpStatus.NO_CONTENT.value(), httpStatus.value());
    }

    @Test
    public void httpPut_notFound_updateQuantityQueryParam(){
        when(shoppingCartService.getShoppingCartItem(2L, 1L)).thenReturn(Optional.empty());

        String url = putUpdateItemsQuantityUrl(2L, 1L, 2);
        RequestEntity<Void> request = RequestEntity.put(url).build();
        ResponseEntity<Void> response =  restTemplate.exchange(request, Void.class);

        HttpStatusCode httpStatus = response.getStatusCode();
        assertEquals(HttpStatus.NOT_FOUND.value(), httpStatus.value());
    }

    @Test
    public void httpPut_notFound_updateQuantityJsonRequest(){
        when(shoppingCartService.getShoppingCartItem(2L, 1L)).thenReturn(Optional.empty());

        String url = cartItemUrl(2L, 1L);
        ShoppingCartItem item = new ShoppingCartItem(1L, 1);

        RequestEntity<String> request = RequestEntity.put(url)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(gson.toJson(item));

        ResponseEntity<Void> response =  restTemplate.exchange(request, Void.class);

        HttpStatusCode httpStatus = response.getStatusCode();
        assertEquals(HttpStatus.NOT_FOUND.value(), httpStatus.value());
    }

    @Test
    public void httpDelete_shoppingCart(){
        when(shoppingCartService.removeShoppingCart(2L)).thenReturn(Optional.of(shoppingCart2WithProduct1));

        RequestEntity<Void> request = RequestEntity.delete(shoppingCartUrl(2L)).build();
        ResponseEntity<Void> response =  restTemplate.exchange(request, Void.class);

        HttpStatusCode httpStatus = response.getStatusCode();
        assertEquals(HttpStatus.NO_CONTENT.value(), httpStatus.value());
    }

    @Test
    public void httpDelete_shoppingCartItem(){
        when(shoppingCartService.removeShoppingCartItem(2L,1L)).thenReturn(Optional.of(calculatedCart2Product1Qty1));

        RequestEntity<Void> request = RequestEntity.delete(cartItemUrl(2L,1L)).build();
        ResponseEntity<Void> response =  restTemplate.exchange(request, Void.class);

        HttpStatusCode httpStatus = response.getStatusCode();
        assertEquals(HttpStatus.NO_CONTENT.value(), httpStatus.value());
    }


    @Test
    public void httpDelete_notFound_shoppingCart(){
        when(shoppingCartService.removeShoppingCart(2L)).thenReturn(Optional.empty());

        RequestEntity<Void> request = RequestEntity.delete(shoppingCartUrl(2L)).build();
        ResponseEntity<Void> response =  restTemplate.exchange(request, Void.class);

        HttpStatusCode httpStatus = response.getStatusCode();
        assertEquals(HttpStatus.NOT_FOUND.value(), httpStatus.value());
    }

    @Test
    public void httpDelete_notFound_shoppingCartItem(){
        when(shoppingCartService.removeShoppingCartItem(2L,1L)).thenReturn(Optional.empty());

        RequestEntity<Void> request = RequestEntity.delete(cartItemUrl(2L,1L)).build();
        ResponseEntity<Void> response =  restTemplate.exchange(request, Void.class);

        HttpStatusCode httpStatus = response.getStatusCode();
        assertEquals(HttpStatus.NOT_FOUND.value(), httpStatus.value());
    }

    private String shoppingCartUrl(Long cartId) {
        return appContext.getBaseUriBuilder()
                .pathSegment("shoppingCarts","{cartId}")
                .build(cartId)
                .toString();
    }

    private String postItemsToShoppingCartUrl(Long cartId) {
        return appContext.getBaseUriBuilder()
                .pathSegment("shoppingCarts","{cartId}","items")
                .build(cartId)
                .toString();
    }


    private String putUpdateItemsQuantityUrl(Long cartId, Long itemId, int quantity) {
        return appContext.getBaseUriBuilder()
                .pathSegment("shoppingCarts","{cartId}","items", "{itemId}", "")
                .queryParam("quantity","{quantity}")
                .build(cartId, itemId, quantity)
                .toString();
    }

    private String cartItemUrl(Long cartId, Long itemId) {
        return appContext.getBaseUriBuilder()
                .pathSegment("shoppingCarts","{cartId}","items","{itemId}")
                .build(cartId, itemId)
                .toString();
    }


}
