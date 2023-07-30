package com.electronicstore.springboot.dao;

import com.electronicstore.springboot.model.ShoppingCartTestItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShoppingCartTestItemRepository
    extends JpaRepository<ShoppingCartTestItem, Long>, BaseRepository<ShoppingCartTestItem, Long> {

    @Override
    Optional<ShoppingCartTestItem> findById(Long id);

    @Override
    <S extends ShoppingCartTestItem> S save(S entity);

}
