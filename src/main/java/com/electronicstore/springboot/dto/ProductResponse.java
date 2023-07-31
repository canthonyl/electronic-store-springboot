package com.electronicstore.springboot.dto;

import com.electronicstore.springboot.model.Product;

import java.util.List;

public class ProductResponse {

    private List<Product> products;

    public ProductResponse(){}

    public ProductResponse(List<Product> list) {
        products = list;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
