package com.electronicstore.springboot.dao.orm;

import com.electronicstore.springboot.dao.Datastore;
import com.electronicstore.springboot.dao.EntityRepository;
import com.electronicstore.springboot.model.Product;
import com.electronicstore.springboot.model.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
//@Cacheable
public interface ProductCategoryJpaRepository extends JpaRepository<ProductCategory, Long> {

    @Override
    Optional<ProductCategory> findById(Long id);

    @Override
    boolean existsById(Long id);

    @Override
    <S extends ProductCategory> List<S> saveAll(Iterable<S> entities);

    @Override
    void deleteById(Long id);

    @Override
    void deleteAllById(Iterable<? extends Long> ids);

    @Override
    List<ProductCategory> findAll();

}
