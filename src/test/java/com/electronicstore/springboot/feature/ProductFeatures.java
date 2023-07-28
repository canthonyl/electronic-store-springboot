package com.electronicstore.springboot.feature;


import com.electronicstore.springboot.context.CommonContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

//@Profile("test")
//@ActiveProfiles("test")
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ProductFeatures {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CommonContext appContext;

    //@Test
    public void getProductByIdReturnsProductDetails(){
        //WebTestClient testClient = WebTestClient.bindToServer().build();

        /*Product product1 = new Product("Apple Macbook Pro", "Apple Macbook Pro");
        Product product2 = new Product("Dell Desktop", "Dell Desktop");
        Product product3 = new Product("Mechanical Keyboard", "Mechanical Keyboard");
        ProductRequest request = new ProductRequest(List.of(product1, product2, product3));

        ResponseEntity<ProductResponse> postResponse = restTemplate.postForEntity(appContext.getBaseUriBuilder().path("products").toUriString(), request, ProductResponse.class);

        assertEquals(HttpStatus.ACCEPTED.value(), postResponse.getStatusCode().value());

        List<ProductData> responseList = postResponse.getBody().getProducts();
        assertEquals(3, responseList.size());

        String resource1 = responseList.get(0).getAttributeValue(ProductController.ATTRIBUTE_RESOURCE);
        String resource2 = responseList.get(1).getAttributeValue(ProductController.ATTRIBUTE_RESOURCE);
        String resource3 = responseList.get(2).getAttributeValue(ProductController.ATTRIBUTE_RESOURCE);
        System.out.println("resource1 = "+resource1);
        System.out.println("resource2 = "+resource2);
        System.out.println("resource3 = "+resource3);

        ResponseEntity<Product> response1 = restTemplate.getForEntity(resource1, Product.class);
        ResponseEntity<Product> response2 = restTemplate.getForEntity(resource2, Product.class);
        ResponseEntity<Product> response3 = restTemplate.getForEntity(resource3, Product.class);

        assertEquals(HttpStatus.OK.value(), response1.getStatusCode().value());
        assertEquals(HttpStatus.OK.value(), response2.getStatusCode().value());
        assertEquals(HttpStatus.OK.value(), response3.getStatusCode().value());

        assertEquals(product1.getName(), response1.getBody().getName());
        assertEquals(product2.getName(), response2.getBody().getName());
        assertEquals(product3.getName(), response3.getBody().getName());*/
    }

    //@Test
    public void retrieveDeletedProductsReturnNotFound(){

        /*Product product1 = new Product("Apple Macbook Pro", "Apple Macbook Pro");
        ProductRequest request = new ProductRequest(List.of(product1));

        ResponseEntity<ProductResponse> postResponse = restTemplate.postForEntity(appContext.getBaseUriBuilder().path("products").toUriString(), request, ProductResponse.class);
        List<ProductData> responseList = postResponse.getBody().getProducts();
        assertEquals(1, responseList.size());

        String resourceUrl = responseList.get(0).getAttributeValue(ProductController.ATTRIBUTE_RESOURCE);

        System.out.println("resourceUrl = "+resourceUrl);

        ResponseEntity<Product> getResponse = restTemplate.getForEntity(resourceUrl, Product.class);
        assertEquals(HttpStatus.OK.value(), getResponse.getStatusCode().value());

        ResponseEntity<Product> deleteResponse = restTemplate.exchange(resourceUrl, HttpMethod.DELETE, null, Product.class);
        assertEquals(HttpStatus.ACCEPTED.value(), deleteResponse.getStatusCode().value());

        getResponse = restTemplate.getForEntity(resourceUrl, Product.class);
        assertEquals(HttpStatus.NOT_FOUND.value(), getResponse.getStatusCode().value());
*/

    }

/*    @BeforeEach
    public void startServer(){
        appContext.getWebServer().start();
    }

    @AfterEach
    public void stopServer(){
        appContext.getWebServer().shutDownGracefully(r -> {});
    }
*/

}
