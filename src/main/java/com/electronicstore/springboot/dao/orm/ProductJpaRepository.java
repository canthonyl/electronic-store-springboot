package com.electronicstore.springboot.dao.orm;

import com.electronicstore.springboot.dao.Datastore;
import com.electronicstore.springboot.dao.EntityRepository;
import com.electronicstore.springboot.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
//@Cacheable
@Transactional
public interface ProductJpaRepository extends JpaRepository<Product, Long>
{

    @Override
    <S extends Product> S save(S product);

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
