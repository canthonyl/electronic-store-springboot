package com.electronicstore.springboot.dao;

import com.electronicstore.springboot.model.ShoppingCartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingCartItemRepository extends JpaRepository<ShoppingCartItem, Long>, BaseRepository<ShoppingCartItem, Long> {

    @Override
    Optional<ShoppingCartItem> findById(Long id);

    @Override
    <S extends ShoppingCartItem> List<S> saveAll(Iterable<S> list);

}
