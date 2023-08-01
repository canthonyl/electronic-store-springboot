package com.electronicstore.springboot.controller;

import com.electronicstore.springboot.context.CommonContext;
import com.electronicstore.springboot.dto.ProductRequest;
import com.electronicstore.springboot.dto.ProductResponse;
import com.electronicstore.springboot.model.Product;
import com.electronicstore.springboot.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private CommonContext appContext;

    @Autowired
    private ProductService productService;

    //TODO investigate caching of requests
    @GetMapping(path="{id}", produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Product> getProduct(@PathVariable Long id){
        return productService.getProduct(id)
                .map(value -> ResponseEntity.ok().body(value))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponse> addProducts(@Valid @RequestBody ProductRequest request){
        List<Product> products = productService.addProducts(request.getList());

        ProductResponse response = new ProductResponse(products);
        return ResponseEntity.accepted()
                .header(HttpHeaders.LOCATION, products.stream().map(this::toResource).toArray(String[]::new))
                .body(response);
    }

    @DeleteMapping(path = "{id}")
    public ResponseEntity<Product> deleteProduct(@PathVariable Long id){
        return productService.removeProduct(id)
                .map(value -> ResponseEntity.accepted().body(value))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private String toResource(Product product) {
        return appContext.getBaseUriBuilder().pathSegment("products", "{id}").build(product.getId()).toString();
    }


}
