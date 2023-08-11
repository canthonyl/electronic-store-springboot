package com.electronicstore.springboot.fixture;

import com.electronicstore.springboot.model.Product;
import com.electronicstore.springboot.model.ShoppingCart;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScenarioContext {

    public List<Product> productDefinitionList;

    public ResponseEntity<String> response;

    public Map<Long, ShoppingCart> shoppingCarts = new HashMap<>();

}
