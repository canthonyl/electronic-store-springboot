package com.electronicstore.springboot.dao.jdbc;

import com.electronicstore.springboot.dao.EntityDatastore;
import com.electronicstore.springboot.model.DiscountRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;

@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DiscountRuleDatastoreTest {

    @Autowired
    private EntityDatastore<DiscountRule> discountRuleDatastore;

    DiscountRule bulkDiscount1;
    DiscountRule bulkDiscount2;
    DiscountRule bulkDiscount3;

    @BeforeEach
    public void setup(){
        bulkDiscount1 = new DiscountRule();
        bulkDiscount1.setThresholdUnit(2L);
        bulkDiscount1.setThresholdUnitType(DiscountRule.ThresholdType.Qty);
        bulkDiscount1.setApplicableUnit(1L);
        bulkDiscount1.setApplicableUnitType(DiscountRule.ApplicableType.Qty);
        bulkDiscount1.setApplicableDiscount(0.5);
        bulkDiscount1.setOverrideAmount(0.0);
        bulkDiscount1.setThresholdProductType(DiscountRule.ThresholdProductType.Any);
        bulkDiscount1.setRuleGroupId(1L);
        bulkDiscount1.setApplicableProductType(DiscountRule.ApplicableProductType.Identity);
        bulkDiscount1.setDescription("Buy 1 get 50% off second");

        bulkDiscount2 = new DiscountRule();
        bulkDiscount2.setThresholdUnit(3L);
        bulkDiscount2.setThresholdUnitType(DiscountRule.ThresholdType.Qty);
        bulkDiscount2.setApplicableUnit(3L);
        bulkDiscount2.setApplicableUnitType(DiscountRule.ApplicableType.Qty);
        bulkDiscount2.setApplicableDiscount(0.3);
        bulkDiscount2.setOverrideAmount(0.0);
        bulkDiscount2.setThresholdProductType(DiscountRule.ThresholdProductType.Any);
        bulkDiscount2.setRuleGroupId(1L);
        bulkDiscount2.setApplicableProductType(DiscountRule.ApplicableProductType.Identity);
        bulkDiscount2.setDescription("Buy 3 get 30% off");

        bulkDiscount3 = new DiscountRule();
        bulkDiscount3.setThresholdUnit(4L);
        bulkDiscount3.setThresholdUnitType(DiscountRule.ThresholdType.Qty);
        bulkDiscount3.setApplicableUnit(4L);
        bulkDiscount3.setApplicableUnitType(DiscountRule.ApplicableType.Qty);
        bulkDiscount3.setApplicableDiscount(0.35);
        bulkDiscount3.setOverrideAmount(0.0);
        bulkDiscount3.setThresholdProductType(DiscountRule.ThresholdProductType.Any);
        bulkDiscount3.setRuleGroupId(2L);
        bulkDiscount3.setApplicableProductType(DiscountRule.ApplicableProductType.Identity);
        bulkDiscount3.setDescription("Buy 4 get 35% off");
    }

    @Test
    public void persist(){
        Optional<DiscountRule> persisted = discountRuleDatastore.persist(bulkDiscount1);
        assertEquals(true, persisted.isPresent());
    }

    @Test
    public void findValuesIn(){
        discountRuleDatastore.persist(List.of(bulkDiscount1, bulkDiscount2, bulkDiscount3));

        List<DiscountRule> result = discountRuleDatastore.findMatchingValuesIn("rule_group_id", List.of(1L));
        assertEquals(2, result.size());
        assertEquals("Buy 1 get 50% off second", result.get(0).getDescription());
        assertEquals("Buy 3 get 30% off", result.get(1).getDescription());
    }

    @Test
    public void findValuesIn_MultipleCriteria(){
        discountRuleDatastore.persist(List.of(bulkDiscount1, bulkDiscount2, bulkDiscount3));

        Map<String, Collection> criteria = new HashMap<>();
        criteria.put("threshold_unit", List.of(2L));
        criteria.put("applicable_unit", List.of(4L));

        Map<Long, DiscountRule> result = discountRuleDatastore.findMatchingValuesIn(criteria)
                .stream().collect(Collectors.toMap(DiscountRule::getId, Function.identity()));
        assertEquals(2, result.size());
        assertEquals("Buy 1 get 50% off second", result.get(1L).getDescription());
        assertEquals("Buy 4 get 35% off", result.get(3L).getDescription());
    }

}
