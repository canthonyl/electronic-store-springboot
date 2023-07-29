package com.electronicstore.springboot.dao;

import com.electronicstore.springboot.model.ShoppingCartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingCartItemRepository
        //extends ListCrudRepository<ShoppingCartItem, Long> {
        //extends BaseRepository<ShoppingCartItem, Long> {
        extends JpaRepository<ShoppingCartItem, Long> {

    @Override
    Optional<ShoppingCartItem> findById(Long id);

    @Override
    <S extends ShoppingCartItem> List<S> saveAll(Iterable<S> list);

}
