package com.electronicstore.springboot.dto;

import com.electronicstore.springboot.model.Product;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductResponse {

    private List<Product> products;

    public ProductResponse(){}

    public ProductResponse(List<Product> list) {
        products = list;
    }

}
