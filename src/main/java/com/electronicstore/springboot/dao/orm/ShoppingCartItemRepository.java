package com.electronicstore.springboot.dao.orm;

import com.electronicstore.springboot.model.ShoppingCartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingCartItemRepository extends JpaRepository<ShoppingCartItem, Long> {

    @Override
    Optional<ShoppingCartItem> findById(Long id);

    @Override
    <S extends ShoppingCartItem> List<S> saveAll(Iterable<S> list);

    @Override
    void deleteById(Long id);

    @Query(value = "select s.* from shopping_cart_item s where s.shopping_cart_id=:cartId and s.id=:itemId", nativeQuery = true)
    List<ShoppingCartItem> lookupShoppingCartItemById(Long cartId, Long itemId);

}
