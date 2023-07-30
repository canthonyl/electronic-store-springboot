package com.electronicstore.springboot.dao.test.test;

import com.electronicstore.springboot.dao.BaseRepository;
import com.electronicstore.springboot.model.test.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long>, BaseRepository<Cart, Long> {

    @Override
    Optional<Cart> findById(Long id);

    @Override
    List<Cart> findAllById(Iterable<Long> ids);

    @Override
    <S extends Cart> S save(S entity);

    @Override
    <S extends Cart> List<S> saveAll(Iterable<S> items);
}
