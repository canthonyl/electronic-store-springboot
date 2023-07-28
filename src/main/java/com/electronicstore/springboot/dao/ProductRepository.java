package com.electronicstore.springboot.dao;

import com.electronicstore.springboot.model.Product;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
//@Cacheable
public interface ProductRepository
        extends ListCrudRepository<Product, Long> {
        ////extends JpaRepository<Product, Long> {

    @Override
    Optional<Product> findById(Long id);

    @Override
    List<Product> findAllById(Iterable<Long> ids);

    @Override
    boolean existsById(Long id);

    @Override
    <S extends Product> List<S> saveAll(Iterable<S> entities);

    @Override
    void deleteById(Long id);

    @Override
    void deleteAllById(Iterable<? extends Long> ids);

}
