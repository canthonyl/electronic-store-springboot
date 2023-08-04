package com.electronicstore.springboot.dao;

import com.electronicstore.springboot.model.Product;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface EntityRepository<E> {

    <S extends E> S save(S product);

    Optional<E> findById(Long id);

    List<E> findAllById(Iterable<Long> ids);

    List<E> findAll();

    boolean existsById(Long id);

    <S extends E> List<S> saveAll(Iterable<S> entities);

    void deleteById(Long id);

    void deleteAllById(Iterable<? extends Long> ids);


}
