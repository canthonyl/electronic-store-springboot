package com.electronicstore.springboot.service;

import com.electronicstore.springboot.dao.EntityDatastore;
import com.electronicstore.springboot.model.DiscountRule;
import com.electronicstore.springboot.model.DiscountRuleSetting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DealService {

    @Autowired
    private EntityDatastore<DiscountRuleSetting> discountRuleSettingDatastore;

    @Autowired
    private EntityDatastore<DiscountRule> discountRuleDatastore;

    public List<DiscountRuleSetting> lookupRuleByCategoryOrProduct(Collection<Long> catId, Collection<Long> productId) {
        Map<String, Collection> criteria = new HashMap<>();
        if (catId.size()>0) {
            criteria.put("category_id", catId);
        }
        if (productId.size()>0) {
            criteria.put("product_id", productId);
        }
        if (criteria.size()>0) {
            return discountRuleSettingDatastore.findMatchingValuesIn(criteria);
        } else {
            return Collections.emptyList();
        }
    }

    public List<DiscountRule> lookupRuleByGroupId(Collection<Long> ruleGroupId) {
        if (ruleGroupId.size()>0) {
            return discountRuleDatastore.findMatchingValuesIn("rule_group_id", ruleGroupId);
        } else {
            return Collections.emptyList();
        }
    }

}
