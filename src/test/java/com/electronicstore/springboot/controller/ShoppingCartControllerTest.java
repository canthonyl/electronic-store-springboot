package com.electronicstore.springboot.controller;


import com.electronicstore.springboot.context.CommonContext;
import com.electronicstore.springboot.dto.ShoppingCartRequest;
import com.electronicstore.springboot.service.ShoppingCartService;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Optional;

import static com.electronicstore.springboot.fixture.Examples.*;
import static net.javacrumbs.jsonunit.JsonMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

import com.electronicstore.springboot.model.*;


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
    public void httpPostRequestCreatesNewShoppingCart(){
        when(shoppingCartService.createShoppingCart(any(ShoppingCart.class)))
                .thenAnswer(i -> {ShoppingCart cart = i.getArgument(0, ShoppingCart.class);
                    cart.setId(1L);
                    return cart;})
                ;
        
        String productUri = appContext.getBaseUriBuilder().path("shoppingCarts").toUriString();
        HttpEntity<String> emptyPostRequest = RequestEntity.post(productUri).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).body("{}");
        ResponseEntity<String> response = restTemplate.exchange(productUri, HttpMethod.POST, emptyPostRequest, String.class);

        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode().value());
        String responseJson = response.getBody();

        HttpHeaders responseHeaders = response.getHeaders();
        assertEquals(true, responseHeaders.containsKey(HttpHeaders.LOCATION));

        String location = appContext.getBaseUriBuilder()
                .pathSegment("shoppingCarts","{id}")
                .buildAndExpand(1).toUriString();

        assertEquals(location, responseHeaders.get(HttpHeaders.LOCATION).get(0));

        assertThat(responseJson, jsonNodePresent("shoppingCart"));
        assertThat(responseJson, jsonPartEquals("shoppingCart.id", 1));
    }

    @Test
    public void testGetShoppingCart(){
        Long id = shoppingCart2.getId();
        when(shoppingCartService.getShoppingCart(2L)).thenReturn(Optional.of(shoppingCart2));
        when(shoppingCartService.getShoppingCart(3L)).thenReturn(Optional.empty());

        String cart2Url = appContext.getBaseUriBuilder().pathSegment("shoppingCarts","{id}").buildAndExpand(id).toUriString();
        ResponseEntity<String> cart2Response = restTemplate.getForEntity(cart2Url, String.class);

        assertEquals(HttpStatus.OK.value(), cart2Response.getStatusCode().value());

        String responseJson = cart2Response.getBody();
        assertThat(responseJson, jsonPartEquals("id", id));
        assertThat(responseJson, jsonNodePresent("items[0]"));
        assertThat(responseJson, jsonPartEquals("items[0].id", shoppingCart2.getItems().get(0).getId()));
        assertThat(responseJson, jsonPartEquals("items[0].product.id", shoppingCart2.getItems().get(0).getProduct().getId()));

        String cart3Url = appContext.getBaseUriBuilder().pathSegment("shoppingCarts","{id}").buildAndExpand(3).toUriString();
        ResponseEntity<String> product3response = restTemplate.getForEntity(cart3Url, String.class);
        assertEquals(HttpStatus.NOT_FOUND.value(), product3response.getStatusCode().value());
    }

    @Test
    public void testGetShoppingCartItem(){
        when(shoppingCartService.shoppingCartExists(2L)).thenReturn(true);
        when(shoppingCartService.getShoppingCartItem(2L, 1L)).thenReturn(Optional.of(shoppingCart2.getItems().get(0)));
        when(shoppingCartService.getShoppingCartItem(2L, 2L)).thenReturn(Optional.empty());

        String cart2Item1Url = appContext.getBaseUriBuilder().pathSegment("shoppingCarts","{cartId}","items","{itemId}")
                .build(2L, 1L).toString();
        ResponseEntity<String> cart2Item1Response = restTemplate.getForEntity(cart2Item1Url, String.class);
        assertEquals(HttpStatus.OK.value(), cart2Item1Response.getStatusCode().value());

        String cart2Item1 = cart2Item1Response.getBody();
        assertThat(cart2Item1, jsonPartEquals("id", 1L));
        assertThat(cart2Item1, jsonPartEquals("product.id", 1L));
        assertThat(cart2Item1, jsonPartEquals("quantity", 1));
        assertThat(cart2Item1, jsonPartEquals("price", 11000.0));
        assertThat(cart2Item1, jsonPartEquals("amountBeforeDiscount", 11000.0));
        assertThat(cart2Item1, jsonPartEquals("discountAmount", 2000.0));
        assertThat(cart2Item1, jsonPartEquals("amount", 9000.0));

        when(shoppingCartService.getShoppingCartItem(2L, 2L)).thenReturn(Optional.empty());

        String cart2Item2Url = appContext.getBaseUriBuilder().pathSegment("shoppingCarts","{cartId}","items","{itemId}")
                .build(2L, 2L).toString();
        ResponseEntity<String> cart2Item2Response = restTemplate.getForEntity(cart2Item2Url, String.class);
        assertEquals(HttpStatus.NOT_FOUND.value(), cart2Item2Response.getStatusCode().value());
    }

    @Test
    public void addItemsToShoppingCart() {
        when(shoppingCartService.shoppingCartExists(2L)).thenReturn(true);
        when(shoppingCartService.getShoppingCart(2L)).thenReturn(Optional.of(shoppingCart2));

        String cart2ItemsUrl = appContext.getBaseUriBuilder().pathSegment("shoppingCarts","{cartId}","items")
                .build(2L).toString();

        ShoppingCartRequest request = new ShoppingCartRequest();
        request.setResponseType(ShoppingCartRequest.ResponseType.ShoppingCart);
        request.setShoppingCartItems(shoppingCart2Items);

        String json;
        ResponseEntity<String> cart2Item2Response;
        String responseJson;

        json = gson.toJson(request);
        cart2Item2Response =  restTemplate.postForEntity(cart2ItemsUrl, jsonRequest(json), String.class);
        assertEquals(HttpStatus.ACCEPTED.value(), cart2Item2Response.getStatusCode().value());

        responseJson = cart2Item2Response.getBody();
        assertThat(responseJson, jsonPartEquals("shoppingCart.id", 2L));
        assertThat(responseJson, jsonNodePresent("shoppingCart.items[0]"));
        assertThat(responseJson, jsonPartEquals("shoppingCart.items[0].id", 1L));
        assertThat(responseJson, jsonNodePresent("shoppingCart.items[0].product"));
        assertThat(responseJson, jsonPartEquals("shoppingCart.items[0].product.id", 1L));
        assertThat(responseJson, jsonPartEquals("shoppingCart.items[0].price", 11000.0));

        request.setResponseType(ShoppingCartRequest.ResponseType.None);

        json = gson.toJson(request);
        cart2Item2Response = restTemplate.postForEntity(cart2ItemsUrl, jsonRequest(json), String.class);
        assertEquals(HttpStatus.ACCEPTED.value(), cart2Item2Response.getStatusCode().value());

        responseJson = cart2Item2Response.getBody();
        assertNull(responseJson);

    }

    private HttpEntity<String> jsonRequest(String requestJson) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);
        return requestEntity;
    }

}
