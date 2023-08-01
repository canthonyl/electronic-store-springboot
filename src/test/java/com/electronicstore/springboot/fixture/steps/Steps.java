package com.electronicstore.springboot.fixture.steps;

import com.electronicstore.springboot.context.CommonContext;
import com.electronicstore.springboot.dao.*;
import com.electronicstore.springboot.dto.ProductRequest;
import com.electronicstore.springboot.fixture.ScenarioContext;
import com.electronicstore.springboot.model.*;
import com.electronicstore.springboot.service.ProductService;
import com.electronicstore.springboot.service.ShoppingCartService;
import com.google.gson.Gson;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.support.HttpRequestWrapper;

import static java.util.stream.Collectors.toMap;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static org.assertj.core.util.introspection.CaseFormatUtils.toCamelCase;
import static org.hamcrest.MatcherAssert.assertThat;

import javax.sql.DataSource;

import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.*;

public class Steps {

    @Autowired
    CommonContext appContext;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ShoppingCartItemRepository shoppingCartItemRepository;

    @Autowired
    DataSource dataSource;

    @Autowired
    TestRestTemplate restTemplate;

    //@Autowired
    ScenarioContext scenarioContext;

    @Autowired
    ProductService productService;

    @Autowired
    ProductCategoryRepository productCategoryRepository;

    @Autowired
    DiscountRuleRepository discountRuleRepository;

    @Autowired
    DiscountRuleSettingRepository discountRuleSettingRepository;

    @Autowired
    ShoppingCartService shoppingCartService;

    Gson gson = new Gson();


    @Before
    public void beforeScenario() {
        scenarioContext = new ScenarioContext();
        //scenarioContext.webTestClient = WebTestClient.bindToServer().baseUrl(appContext.getBaseUriBuilder().toUriString()).build();
        scenarioContext.shoppingCarts = new HashMap<>();
    }

    @After
    public void afterScenario(){
        /*ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScripts(new ClassPathResource("schema.sql"));
        populator.execute(dataSource);*/
    }

    @Given("the following products")
    public void theFollowingProducts(List<Product> products) {
        scenarioContext.productDefinitionList = products;
    }

    //@Given("the following products in {word} table")
    @Given("the following products persisted")
    public void theFollowingProductsInTable(List<Product> dataTable) {
        // populate table
        productRepository.saveAll(dataTable);
        storeTableState("products");
    }

    private void storeTableState(String tableName){
            //todo
    }

    @When("a POST request containing products is sent to {string}")
    public void anHttpRequestWithBodyIsSent(String resource) {
        ProductRequest productRequest = new ProductRequest(scenarioContext.productDefinitionList);
        RequestEntity<String> request = RequestEntity.post(resource)
                .contentType(MediaType.APPLICATION_JSON)
                .body(gson.toJson(productRequest));
        scenarioContext.response = restTemplate.exchange(resource, POST, request, String.class);
    }

    /*@When("a GET request is sent to {string}")
    public void aRequestIsSentToProductsEndpoint(String resource) {
        scenarioContext.response = scenarioContext.webTestClient.get()
                .uri(resource)
                .exchange();
    }*/

    @When("a {word} request is sent to {string}")
    public void anHttpRequestIsSent(String httpMethod, String resource) {
        HttpEntity<String> emptyPostRequest = RequestEntity.post(resource).header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).body("{}");
        switch(httpMethod) {
            case "GET" -> scenarioContext.response = restTemplate.exchange(resource, GET, null, String.class);
            case "POST" -> scenarioContext.response = restTemplate.exchange(resource, POST, emptyPostRequest, String.class);
            case "DELETE" -> scenarioContext.response = restTemplate.exchange(resource, DELETE, null, String.class);
            default -> fail("TODO httpMethod "+httpMethod);
        }
    }

    @When("a {word} request is sent to {string} with body")
    public void anHttpRequestWithBodyIsSent(String httpMethodStr, String resource, String json) {
        HttpMethod method = HttpMethod.valueOf(httpMethodStr);
        RequestEntity<String> request = RequestEntity
                .method(method, resource)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);

        scenarioContext.response = restTemplate.exchange(resource, method, request, String.class);
    }

    @Then("http status {word} is received")
    public void verifyHttpStatusReceived(String status) {
        HttpStatus httpStatus = HttpStatus.valueOf(status);
        //scenarioContext.webTestClientResponse.expectStatus().isEqualTo(HttpStatusCode.valueOf(httpStatus.value()));
        assertEquals(httpStatus.value(), scenarioContext.response.getStatusCode().value());
    }

    @Then("{string} in Http Header contains the following values")
    public void locationInHttpHeaderContainsTheFollowingLinks(String headerKey, List<String> list) {
        //scenarioContext.webTestClientResponse.expectHeader().values(headerKey, containsInAnyOrder(list.toArray(String[]::new)));
        HttpHeaders headers = scenarioContext.response.getHeaders();
        assertTrue(headers.containsKey(headerKey));
        assertThat(headers.get(headerKey), containsInAnyOrder(list.toArray(String[]::new)));
    }

    @Then("the following response body")
    public void theFollowingInResponseBody(String json) {
        System.out.println("json = "+json);
        //scenarioContext.webTestClientResponse.expectBody().consumeWith(json().isEqualTo(json));
        assertJsonEquals(json, scenarioContext.response.getBody());
    }


    @Given("the following product categories")
    public void setupProductCategory(List<ProductCategory> list){
        productCategoryRepository.saveAll(list);
    }

    @Given("the following products in the electronic store")
    public void setupProducts(List<Product> productList){
        productService.addProducts(productList);
    }

    @Given("a discount rule for {string}")
    public void setupDiscountRule(String description, DiscountRule rule){
        rule.setDescription(description);
        discountRuleRepository.save(rule);
    }

    @Given("the following discount rules")
    public void setupDiscountRule(List<DiscountRule> list) {
        discountRuleRepository.saveAll(list);
    }

    @Given("the following rule settings")
    public void setupRuleSetting(List<DiscountRuleSetting> rule){
        discountRuleSettingRepository.saveAll(rule);
    }

    @Given("an empty shopping cart with id {int} is created")
    @When("a new cart is created with id {int}")
    public void aNewCartIsCreated(long cartId) {
        ShoppingCart cart = shoppingCartService.createShoppingCart(new ShoppingCart());
        assertEquals(cartId, cart.getId().longValue());
        scenarioContext.shoppingCarts.put(cart.getId(), cart);
    }

    @Given("a shopping cart with id {int} is created with the following items")
    public void aNewCartIsCreatedWithItems(long cartId, List<ShoppingCartItem> items) {
        ShoppingCart shoppingCart = new ShoppingCart(items);
        shoppingCartService.createShoppingCart(shoppingCart);
        assertEquals(cartId, shoppingCart.getId().longValue());
        scenarioContext.shoppingCarts.put(cartId, shoppingCart);
    }

    /*@Given("a shopping cart with id {int} is created with the following items")
    public void aNewCartIsCreatedWithItems(long cartId, List<ShoppingCartItem> initialItems) {
        ShoppingCart cart = shoppingCartService.createShoppingCart(Collections.emptyList());
        assertEquals(cartId, cart.getId().longValue());
        shoppingCartService.addShoppingCartItems(cartId, initialItems);
        cart = shoppingCartService.getShoppingCart(cart.getId()).get();
        scenarioContext.shoppingCarts.put(cart.getId(), cart);
    }*/

    @When("the following items are added to the shopping cart id {int}")
    public void productIsAddedToShoppingCartId(long cartId, List<ShoppingCartItem> items) {
        ShoppingCart cart = new ShoppingCart(cartId);
        items.forEach(i-> i.setShoppingCart(cart));
        shoppingCartService.addShoppingCartItems(cartId, items);
    }

    @When("shopping cart id {int} is refreshed")
    public void shoppingCartIsRefreshed(long cartId) {
        Optional<ShoppingCart> result = shoppingCartService.getShoppingCart(cartId);
        assertTrue(result.isPresent());
        ShoppingCart cart = result.get();
        scenarioContext.shoppingCarts.put(cart.getId(), cart);
    }

    @Then("shopping cart id {int} is refreshed with the following items")
    public void shoppingCartItemListIsAfterRefresh(long cartId, List<ShoppingCartItem> list) {
        shoppingCartIsRefreshed(cartId);
        shoppingCartItemListIs(cartId, list);
    }

    @Then("shopping cart id {int} contains the following items")
    public void shoppingCartItemListIs(long cartId, List<ShoppingCartItem> list) {
        ShoppingCart cart = scenarioContext.shoppingCarts.get(cartId);
        assertShoppingCartItemsEquals(list, cart.getItems());
    }

    @Then("shopping cart id {int} is refreshed with the following total amounts")
    public void shoppingCartAmountIsAfterRefresh(long cartId, List<Map<String,String>> list) {
        shoppingCartIsRefreshed(cartId);
        shoppingCartAmountIs(cartId, list);
    }

    @Then("shopping cart id {int} has the following total amounts")
    public void shoppingCartAmountIs(long cartId, List<Map<String, String>> list) {
        Map<String, Double> valueMap = toFieldValueMap(list);
        ShoppingCart cart = scenarioContext.shoppingCarts.get(cartId);

        assertEquals(valueMap.get("totalAmountBeforeDiscount").doubleValue(), cart.getTotalAmountBeforeDiscount(), 0.001);
        assertEquals(valueMap.get("totalDiscountAmount").doubleValue(), cart.getTotalDiscountAmount(),0.001);
        assertEquals(valueMap.get("totalAmount").doubleValue(), cart.getTotalAmount(),0.001);
    }

    private Map<String, Double> toFieldValueMap(List<Map<String, String>> list) {
        Map<String, Double> map = new HashMap<>();
        System.out.println("list = "+list);

        for (Map<String, String> e : list) {
            Map<String, String> adjKey = new HashMap<>();
            e.forEach((k,v) -> adjKey.put(toCamelCase(k), v));
            System.out.println("adjKey = "+adjKey);

            String field = toCamelCase(adjKey.get("field"));
            String value = adjKey.get("value");
            map.put(field, Double.valueOf(value));
        }
        return map;
    }

    private void assertShoppingCartItemsEquals(Collection<ShoppingCartItem> c1, Collection<ShoppingCartItem> c2) {
        Map<Long, ShoppingCartItem> map1 = c1.stream().collect(toMap(ShoppingCartItem::getId, Function.identity()));
        Map<Long, ShoppingCartItem> map2 = c2.stream().collect(toMap(ShoppingCartItem::getId, Function.identity()));
        assertEquals(map1.keySet(), map2.keySet());

        map1.forEach((id, item1) -> {
            ShoppingCartItem item2 = map2.get(id);
            assertEquals(item1.getProductId(), item2.getProductId());
            assertEquals(item1.getQuantity(), item2.getQuantity());
            assertEquals(item1.getPrice(), item2.getPrice(), 0.001);
            assertEquals(item1.getAmountBeforeDiscount(), item2.getAmountBeforeDiscount(), 0.001);
            assertEquals(item1.getDiscountAmount(), item2.getDiscountAmount(), 0.001);
            assertEquals(item1.getAmount(), item2.getAmount(), 0.001);
            assertEquals(toSet(item1.getDiscountApplied()), toSet(item2.getDiscountApplied()));
        });

    }

    private Set<String> toSet(List<String> list) {
        if (list == null) return Collections.emptySet();
        return list.stream().collect(Collectors.toSet());
    }

}
