package com.electronicstore.springboot.dao;

import com.electronicstore.springboot.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductDatastore {

    <S extends Product> S save(S product);

    Optional<Product> findById(Long id);

    List<Product> findAllById(Iterable<Long> ids);

    boolean existsById(Long id);

    <S extends Product> List<S> saveAll(Iterable<S> entities);

    void deleteById(Long id);

    void deleteAllById(Iterable<? extends Long> ids);
}
