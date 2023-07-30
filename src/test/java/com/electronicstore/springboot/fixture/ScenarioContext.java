package com.electronicstore.springboot.fixture;

import com.electronicstore.springboot.model.Product;
import com.electronicstore.springboot.model.ShoppingCart;
import io.cucumber.spring.ScenarioScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScenarioContext {

    public List<Product> productDefinitionList;

    public ResponseEntity<String> response;

    public Map<Long, ShoppingCart> shoppingCarts = new HashMap<>();

}
