package com.electronicstore.springboot.dao.jdbc;

import com.electronicstore.springboot.dao.ProductDatastore;
import com.electronicstore.springboot.model.Product;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Optional;


public class ProductDatastoreJdbc implements ProductDatastore {

    @Override
    public <S extends Product> S save(S product) {
        return null;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public List<Product> findAllById(Iterable<Long> ids) {
        return null;
    }

    @Override
    public boolean existsById(Long id) {
        return false;
    }

    @Override
    public <S extends Product> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteById(Long id) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {

    }
}
