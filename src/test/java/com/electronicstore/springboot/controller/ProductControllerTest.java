package com.electronicstore.springboot.controller;

import com.electronicstore.springboot.context.CommonContext;
import com.electronicstore.springboot.dto.ProductRequest;
import com.electronicstore.springboot.service.ProductService;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.electronicstore.springboot.fixture.Examples.*;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonNodePresent;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonPartEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@ActiveProfiles("test")
@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CommonContext appContext;

    @MockBean
    private ProductService productService;

    private Gson gson = new Gson();

    @Test
    public void httpGetProduct_returnsOK_productDetailsInResponseBody(){
        when(productService.getProduct(1L)).thenReturn(Optional.of(product1));
        when(productService.getProduct(2L)).thenReturn(Optional.of(product2));
        when(productService.getProduct(3L)).thenReturn(Optional.of(product3));

        String product1Url = productUrl(1L);
        String product2Url = productUrl(2L);
        String product3Url = productUrl(3L);

        ResponseEntity<String> response1 = restTemplate.getForEntity(product1Url, String.class);
        ResponseEntity<String> response2 = restTemplate.getForEntity(product2Url, String.class);
        ResponseEntity<String> response3 = restTemplate.getForEntity(product3Url, String.class);

        assertEquals(HttpStatus.OK.value(), response1.getStatusCode().value());
        assertEquals(HttpStatus.OK.value(), response2.getStatusCode().value());
        assertEquals(HttpStatus.OK.value(), response3.getStatusCode().value());

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
    public void httpGetProduct_returnNotFound(){
        when(productService.getProduct(1L)).thenReturn(Optional.empty());
        ResponseEntity<String> response = restTemplate.getForEntity(productUrl(1L), String.class);
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
    }

    @Test
    public void httpPostProduct_returnsAccepted_productDetailsInResponseBody(){
        when(productService.addProducts(anyList())).thenReturn(productList);

        String productUri = appContext.getBaseUriBuilder().path("products").toUriString();
        String json = gson.toJson(new ProductRequest(productList));

        RequestEntity<String> request = RequestEntity.post(productUri).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).body(json);
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        HttpStatusCode responseStatus = response.getStatusCode();
        assertEquals(HttpStatus.OK.value(), responseStatus.value());

        HttpHeaders httpHeaders = response.getHeaders();
        List<String> locationList = httpHeaders.get(HttpHeaders.LOCATION);
        assertEquals(productUrls(1L, 2L, 3L), locationList);

        String responseJson = response.getBody();
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

    @Test
    public void httpPostProduct_badRequest(){
        String productUrl = appContext.getBaseUriBuilder().path("products").toUriString();

        String empty = "";
        String invalidJson = "hello world!";
        String emptyObject = "{}";
        String emptyRequest = "{ \"list\":[] }";
        String missingFields = "{ \"list\":["+
                    "{ \"name\": \"product1\"},"+
                    "{ \"name\": \"product2\"},"+
                    "{ \"name\": \"product3\"}"+
                "] }";

        RequestEntity<String> request;
        ResponseEntity<String> response;
        HttpStatusCode statusCode;

        request = RequestEntity.post(productUrl).contentType(MediaType.APPLICATION_JSON).body(empty);
        response = restTemplate.exchange(request, String.class);
        statusCode = response.getStatusCode();
        assertEquals(HttpStatus.BAD_REQUEST.value(), statusCode.value());

        request = RequestEntity.post(productUrl).contentType(MediaType.APPLICATION_JSON).body(invalidJson);
        response = restTemplate.exchange(request, String.class);
        statusCode = response.getStatusCode();
        assertEquals(HttpStatus.BAD_REQUEST.value(), statusCode.value());

        request = RequestEntity.post(productUrl).contentType(MediaType.APPLICATION_JSON).body(emptyObject);
        response = restTemplate.exchange(request, String.class);
        statusCode = response.getStatusCode();
        assertEquals(HttpStatus.BAD_REQUEST.value(), statusCode.value());

        request = RequestEntity.post(productUrl).contentType(MediaType.APPLICATION_JSON).body(emptyRequest);
        response = restTemplate.exchange(request, String.class);
        statusCode = response.getStatusCode();
        assertEquals(HttpStatus.BAD_REQUEST.value(), statusCode.value());

        request = RequestEntity.post(productUrl).contentType(MediaType.APPLICATION_JSON).body(missingFields);
        response = restTemplate.exchange(request, String.class);
        statusCode = response.getStatusCode();
        assertEquals(HttpStatus.BAD_REQUEST.value(), statusCode.value());
    }

    @Test
    public void httpDeleteProduct_returnsAccepted_deletedProductDetailInResponseBody(){
        when(productService.removeProduct(1L)).thenReturn(Optional.of(product1));

        RequestEntity<Void> request = RequestEntity.delete(productUrl(1L)).build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        int responseCode  = response.getStatusCode().value();
        assertEquals(HttpStatus.OK.value(), responseCode);

        String responseBody = response.getBody();
        assertThat(responseBody , jsonNodePresent("id"));
        assertThat(responseBody , jsonNodePresent("name"));
        assertThat(responseBody , jsonPartEquals("id", 1));
        assertThat(responseBody , jsonPartEquals("name", "Apple Macbook Pro"));
    }

    @Test
    public void httpDeleteProduct_returnsNotFound(){
        when(productService.removeProduct(1L)).thenReturn(Optional.empty());

        RequestEntity<Void> request = RequestEntity.delete(productUrl(1L)).build();
        ResponseEntity<String> response = restTemplate.exchange(request, String.class);

        int responseCode  = response.getStatusCode().value();
        assertEquals(HttpStatus.NOT_FOUND.value(), responseCode);
    }


    private String productUrl(Long id) {
        return appContext.getBaseUriBuilder()
                .pathSegment("products","{id}")
                .build(id)
                .toString();
    }

    private List<String> productUrls(Long... ids) {
        return Arrays.stream(ids).map(this::productUrl).toList();
    }


}