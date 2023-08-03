package com.electronicstore.springboot.dao.orm.test.test;

import com.electronicstore.springboot.model.test.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Override
    Optional<CartItem> findById(Long id);

    @Override
    List<CartItem> findAllById(Iterable<Long> ids);

    @Override
    <S extends CartItem> S save(S entity);

    @Override
    <S extends CartItem> List<S> saveAll(Iterable<S> items);

}
