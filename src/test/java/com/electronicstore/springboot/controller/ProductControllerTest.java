package com.electronicstore.springboot.controller;

import com.electronicstore.springboot.context.CommonContext;
import com.electronicstore.springboot.dto.ProductRequest;
import com.electronicstore.springboot.fixture.Examples;
import com.electronicstore.springboot.model.Product;
import com.electronicstore.springboot.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

import static com.electronicstore.springboot.fixture.Examples.*;
import static net.bytebuddy.matcher.ElementMatchers.is;
import static net.javacrumbs.jsonunit.JsonMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ProductControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CommonContext appContext;

    @MockBean
    private ProductService productService;

    private Gson gson = new Gson();

    @Test
    public void testGetProduct(){
        when(productService.getProduct(1L)).thenReturn(Optional.of(product1));
        when(productService.getProduct(2L)).thenReturn(Optional.of(product2));
        when(productService.getProduct(3L)).thenReturn(Optional.of(product3));
        when(productService.getProduct(4L)).thenReturn(Optional.empty());

        UriComponentsBuilder uri = appContext.getBaseUriBuilder().pathSegment("products","{id}");

        ResponseEntity<String> response1 = restTemplate.getForEntity(uri.buildAndExpand(1).toUriString(), String.class);
        ResponseEntity<String> response2 = restTemplate.getForEntity(uri.buildAndExpand(2).toUriString(), String.class);
        ResponseEntity<String> response3 = restTemplate.getForEntity(uri.buildAndExpand(3).toUriString(), String.class);
        ResponseEntity<String> notFoundResponse4 = restTemplate.getForEntity(uri.buildAndExpand(4).toUriString(), String.class);

        assertEquals(HttpStatus.OK.value(), response1.getStatusCode().value());
        assertEquals(HttpStatus.OK.value(), response2.getStatusCode().value());
        assertEquals(HttpStatus.OK.value(), response3.getStatusCode().value());
        assertEquals(HttpStatus.NOT_FOUND.value(), notFoundResponse4.getStatusCode().value());

        String json1 = response1.getBody();
        assertThat(json1, jsonPartEquals("id", product1.getId()));
        assertThat(json1, jsonPartEquals("name", product1.getName()));
        assertThat(json1, jsonPartEquals("description", product1.getDescription()));
        assertThat(json1, jsonPartEquals("price", product1.getPrice()));
        assertThat(json1, jsonPartEquals("categoryId", product1.getCategoryId()));

        String json2 = response2.getBody();
        assertThat(json2, jsonPartEquals("id", product2.getId()));
        assertThat(json2, jsonPartEquals("name", product2.getName()));
        assertThat(json2, jsonPartEquals("description", product2.getDescription()));
        assertThat(json2, jsonPartEquals("price", product2.getPrice()));
        assertThat(json2, jsonPartEquals("categoryId", product2.getCategoryId()));

        String json3 = response3.getBody();
        assertThat(json3, jsonPartEquals("id", product3.getId()));
        assertThat(json3, jsonPartEquals("name", product3.getName()));
        assertThat(json3, jsonPartEquals("description", product3.getDescription()));
        assertThat(json3, jsonPartEquals("price", product3.getPrice()));
        assertThat(json3, jsonPartEquals("categoryId", product3.getCategoryId()));
    }


    @Test
    public void testAddProduct(){
        ProductRequest request = new ProductRequest(Arrays.asList(
                Examples.ofProduct("Apple Macbook Pro", "Apple Macbook Pro", 11000, 2L),
                Examples.ofProduct("Dell Desktop", "Dell Desktop", 5000, 1L),
                Examples.ofProduct("Mechanical Keyboard", "Mechanical Keyboard", 678, 3L)
        ));

        UriComponentsBuilder uri = appContext.getBaseUriBuilder().pathSegment("products","{id}");
        List<String> list = Arrays.asList(
                uri.build(1).toString(),
                uri.build(2).toString(),
                uri.build(3).toString()
        );

        when(productService.addProducts(anyList())).thenReturn(productList);

        String json = gson.toJson(request);
        String productUri = appContext.getBaseUriBuilder().path("products").toUriString();

        ResponseEntity<String> response = restTemplate.exchange(productUri, HttpMethod.POST, jsonRequest(json), String.class);

        String responseJson = response.getBody();
        assertEquals(HttpStatus.ACCEPTED.value(), response.getStatusCode().value());
        assertTrue(response.getHeaders().containsKey(HttpHeaders.LOCATION));
        List<String> resultList = response.getHeaders().get(HttpHeaders.LOCATION);
        assertEquals(list, resultList);

        assertThat(responseJson , jsonNodePresent("products[0]"));
        assertThat(responseJson , jsonNodePresent("products[1]"));
        assertThat(responseJson , jsonNodePresent("products[2]"));

        assertThat(responseJson , jsonPartEquals("products[0].id", product1.getId()));
        assertThat(responseJson , jsonPartEquals("products[1].id", product2.getId()));
        assertThat(responseJson , jsonPartEquals("products[2].id", product3.getId()));

        assertThat(responseJson , jsonPartEquals("products[0].name", product1.getName()));
        assertThat(responseJson , jsonPartEquals("products[1].name", product2.getName()));
        assertThat(responseJson , jsonPartEquals("products[2].name", product3.getName()));

        assertThat(responseJson , jsonPartEquals("products[0].description", product1.getDescription()));
        assertThat(responseJson , jsonPartEquals("products[1].description", product2.getDescription()));
        assertThat(responseJson , jsonPartEquals("products[2].description", product3.getDescription()));

        assertThat(responseJson , jsonPartEquals("products[0].price", product1.getPrice()));
        assertThat(responseJson , jsonPartEquals("products[1].price", product2.getPrice()));
        assertThat(responseJson , jsonPartEquals("products[2].price", product3.getPrice()));

        assertThat(responseJson , jsonPartEquals("products[0].categoryId", product1.getCategoryId()));
        assertThat(responseJson , jsonPartEquals("products[1].categoryId", product2.getCategoryId()));
        assertThat(responseJson , jsonPartEquals("products[2].categoryId", product3.getCategoryId()));
    }

    private HttpEntity<String> jsonRequest(String requestJson) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);
        return requestEntity;
    }

}