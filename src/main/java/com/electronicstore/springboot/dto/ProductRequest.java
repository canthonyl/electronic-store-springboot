package com.electronicstore.springboot.dto;

import com.electronicstore.springboot.model.Product;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.LinkedList;
import java.util.List;

public class ProductRequest {

    @NotEmpty
    private List<@Valid Product> list;

    public ProductRequest(){
        list = new LinkedList<>();
    }

    public ProductRequest(List<Product> productList){
        this();
        list = productList;
    }

    public List<Product> getList() {
        return list;
    }

    public void setList(List<Product> list) {
        this.list = list;
    }
}
