package com.electronicstore.springboot.dto;

import com.electronicstore.springboot.model.Product;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Getter
@Setter
public class ProductRequest {

    @NotEmpty
    private List<@Valid Product> list;

    public ProductRequest(){
        list = new ArrayList<>();
    }

    public ProductRequest(List<Product> productList){
        list = productList;
    }

}
