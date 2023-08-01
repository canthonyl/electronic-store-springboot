package com.electronicstore.springboot.service;

import com.electronicstore.springboot.dao.ProductRepository;
import com.electronicstore.springboot.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Optional<Product> getProduct(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> addProducts(List<Product> list) {
        return productRepository.saveAll(list);
    }

    public Optional<Product> removeProduct(Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            productRepository.deleteById(id);
        }
        return product;
    }

    /*
    public List<Product> removeProducts(List<Long> productIds) {
        List<Product> product = productRepository.findAllById(productIds);
        productRepository.deleteAllById(productIds);
        return product;
    }*/


}
