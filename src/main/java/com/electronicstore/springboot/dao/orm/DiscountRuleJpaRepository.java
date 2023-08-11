package com.electronicstore.springboot.dao.orm;

import com.electronicstore.springboot.model.DiscountRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRuleJpaRepository extends JpaRepository<DiscountRule, Long> {

    @Override
    Optional<DiscountRule> findById(Long id);

    @Override
    List<DiscountRule> findAllById(Iterable<Long> id);

    @Override
    boolean existsById(Long id);

    @Override
    <S extends DiscountRule> List<S> saveAll(Iterable<S> entities);

    @Override
    void deleteById(Long id);

    @Override
    void deleteAllById(Iterable<? extends Long> ids);

    //List<DiscountRule> findAllByRuleGroupId(List<Long> groupIds);
    @Query(value = "select s.* from discount_rule s where s.rule_group_id in :ruleGroupList", nativeQuery = true)
    List<DiscountRule> lookupRuleByGroupId(Collection<Long> ruleGroupList);


}
