package com.electronicstore.springboot.controller;

import com.electronicstore.springboot.context.CommonContext;
import com.electronicstore.springboot.model.Product;
import com.electronicstore.springboot.service.ProductService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

import static net.bytebuddy.matcher.ElementMatchers.is;
import static net.javacrumbs.jsonunit.JsonMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;


//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Profile("test")
//@ActiveProfiles("test")
public class ProductControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CommonContext appContext;

    @MockBean
    private ProductService productService;


    Product createdProduct1 = new Product(1L,"Apple Macbook Pro", "Apple Macbook Pro");
    Product createdProduct2 = new Product(2L,"Dell Desktop", "Dell Desktop");
    Product createdProduct3 = new Product(3L, "Mechanical Keyboard", "Mechanical Keyboard");
    List<Product> createdProductList = List.of(createdProduct1, createdProduct2, createdProduct3);

    private Gson gson = new Gson();

    //@Test
    public void testGetProduct(){
        when(productService.getProduct(1L)).thenReturn(Optional.of(createdProduct1));
        when(productService.getProduct(2L)).thenReturn(Optional.of(createdProduct2));
        when(productService.getProduct(3L)).thenReturn(Optional.of(createdProduct3));
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
        assertThat(json1, jsonPartEquals("id", createdProduct1.getId()));
        assertThat(json1, jsonPartEquals("name", createdProduct1.getName()));
        assertThat(json1, jsonPartEquals("description", createdProduct1.getDescription()));

        String json2 = response2.getBody();
        assertThat(json2, jsonPartEquals("id", createdProduct2.getId()));
        assertThat(json2, jsonPartEquals("name", createdProduct2.getName()));
        assertThat(json2, jsonPartEquals("description", createdProduct2.getDescription()));

        String json3 = response3.getBody();
        assertThat(json3, jsonPartEquals("id", createdProduct3.getId()));
        assertThat(json3, jsonPartEquals("name", createdProduct3.getName()));
        assertThat(json3, jsonPartEquals("description", createdProduct3.getDescription()));
    }


    //@Test
    public void testAddProduct(){
        String requestJson = "{ \"list\" : ["+
                "{\"name\":\"Apple Laptop\",\"description\":\"Apple Macbook Pro\"}" +
                ",{\"name\":\"Dell Desktop\",\"description\":\"Dell Inspire Series\"}" +
                ",{\"name\":\"Mechanical Keyboard\",\"description\":\"Mech Keyboard Pro\"}" +
                "]}";

        when(productService.addProducts(anyList())).thenReturn(createdProductList);

        System.out.println(requestJson);

        String productUri = appContext.getBaseUriBuilder().path("products").toUriString();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("content-type","application/json");
        HttpEntity<String> requestEntity = new HttpEntity<>(requestJson, headers);

        ResponseEntity<String> response = restTemplate.exchange(productUri, HttpMethod.POST, requestEntity, String.class);

        assertEquals(HttpStatus.ACCEPTED.value(), response.getStatusCode().value());
        String responseJson = response.getBody();
        System.out.println(responseJson);
        assertThat(responseJson , jsonNodePresent("products[0].product"));
        assertThat(responseJson , jsonNodePresent("products[1].product"));
        assertThat(responseJson , jsonNodePresent("products[2].product"));

        assertThat(responseJson , jsonPartEquals("products[0].product.name", createdProduct1.getName()));
        assertThat(responseJson , jsonPartEquals("products[1].product.name", createdProduct2.getName()));
        assertThat(responseJson , jsonPartEquals("products[2].product.name", createdProduct3.getName()));

        assertThat(responseJson , jsonNodePresent("products[0].attributes"));
        assertThat(responseJson , jsonNodePresent("products[1].attributes"));
        assertThat(responseJson , jsonNodePresent("products[2].attributes"));

        String uri = "http://localhost:"+appContext.serverPort()+"/products/";
        assertThat(responseJson , jsonPartEquals("products[0].attributes.resource", uri+createdProduct1.getId()));
        assertThat(responseJson , jsonPartEquals("products[1].attributes.resource", uri+createdProduct2.getId()));
        assertThat(responseJson , jsonPartEquals("products[2].attributes.resource", uri+createdProduct3.getId()));

        /*assertThat(responseJson, jsonPartEquals("list[0].id",1));
        assertThat(responseJson, jsonPartEquals("list[0].name","Apple Laptop"));*/
    }

}