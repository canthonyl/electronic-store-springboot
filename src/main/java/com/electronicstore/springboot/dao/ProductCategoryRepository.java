package com.electronicstore.springboot.dao;

import com.electronicstore.springboot.model.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
//@Cacheable
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long>, BaseRepository<ProductCategory, Long> {

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
}
