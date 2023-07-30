package com.electronicstore.springboot.dao;

import com.electronicstore.springboot.model.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long>, BaseRepository<ShoppingCart, Long> {

    @Override
    boolean existsById(Long id);

    @Override
    Optional<ShoppingCart> findById(Long id);

    @Override
    <S extends ShoppingCart> S save(S entity);

}
