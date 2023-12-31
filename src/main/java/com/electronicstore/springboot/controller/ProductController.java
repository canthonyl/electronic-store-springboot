package com.electronicstore.springboot.controller;

import com.electronicstore.springboot.context.CommonContext;
import com.electronicstore.springboot.dto.ProductRequest;
import com.electronicstore.springboot.dto.ProductResponse;
import com.electronicstore.springboot.model.Product;
import com.electronicstore.springboot.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private CommonContext appContext;

    @Autowired
    private ProductService productService;

    @GetMapping(path="{id}", produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Product> getProduct(@PathVariable Long id){
        return productService.getProduct(id)
                .map(value -> ResponseEntity.ok().body(value))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponse> addProducts(@Valid @RequestBody ProductRequest request){
        List<Product> products = productService.addProducts(request.getList());

        ProductResponse response = new ProductResponse(products);
        return ResponseEntity.ok()
                .header(HttpHeaders.LOCATION, products.stream().map(this::toResource).toArray(String[]::new))
                .body(response);
    }

    @DeleteMapping(path = "{id}")
    public ResponseEntity<Product> deleteProduct(@PathVariable Long id){
        return productService.removeProduct(id)
                .map(p -> ResponseEntity.ok(p))
                .orElse(ResponseEntity.notFound().build());
    }

    private String toResource(Product product) {
        return appContext.getBaseUriBuilder().pathSegment("products", "{id}").build(product.getId()).toString();
    }


}
