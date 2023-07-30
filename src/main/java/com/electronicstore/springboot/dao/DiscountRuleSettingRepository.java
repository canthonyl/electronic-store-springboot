package com.electronicstore.springboot.dao;

import com.electronicstore.springboot.model.DiscountRuleSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DiscountRuleSettingRepository extends JpaRepository<DiscountRuleSetting, Long>, BaseRepository<DiscountRuleSetting, Long> {

    @Override
    Optional<DiscountRuleSetting> findById(Long id);

    @Override
    boolean existsById(Long id);

    @Override
    <S extends DiscountRuleSetting> List<S> saveAll(Iterable<S> entities);

    @Override
    void deleteById(Long id);

    @Override
    void deleteAllById(Iterable<? extends Long> ids);

    /*@Query(value = "select o from discount_rule_setting o where o.product_id in :productIds")
    List<DiscountRuleSetting> customQuery(@Param("productIds") Collection<Long> products);
*/
    @Query(value = "select s.* from discount_rule_setting s where s.category_id in :categoryList or s.product_id in :productList", nativeQuery = true)
    List<DiscountRuleSetting> lookupRuleByCategoryOrProduct(Collection<Long> categoryList, Collection<Long> productList);

}
