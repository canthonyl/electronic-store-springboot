package com.electronicstore.springboot.service;

import com.electronicstore.springboot.dao.EntityDatastore;
import com.electronicstore.springboot.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private EntityDatastore<Product> productDatastore;

    public Optional<Product> getProduct(Long id) {
        return productDatastore.find(id);
    }

    public List<Product> getProducts(Collection<Long> ids) { return productDatastore.find(ids); }

    public List<Product> addProducts(List<Product> list) {
        return productDatastore.persist(list);
    }

    public Optional<Product> removeProduct(Long id) {
        Optional<Product> product = productDatastore.find(id);
        if (product.isPresent()) {
            productDatastore.remove(id);
        }
        return product;
    }


}
