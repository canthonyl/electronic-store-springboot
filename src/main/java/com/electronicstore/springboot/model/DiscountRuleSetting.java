package com.electronicstore.springboot.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

import java.util.Set;

@Entity
public class DiscountRuleSetting {

    public enum Group { all, category, product}

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "discount_rule_setting_id_seq")
    @SequenceGenerator(name = "discount_rule_setting_id_seq", sequenceName = "discount_rule_setting_id_seq", allocationSize = 1)
    private Long id;

    private Long categoryId;

    private Long productId;

    private Long ruleGroupId;

    private String setting;

    public static class Definition {
        public Set<Long> categoryList;
        public Set<Long> productList;
    }

    private transient Definition definition;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSetting() {
        return setting;
    }

    public void setSetting(String setting) {
        this.setting = setting;
    }

    public Definition getDefinition() {

        return definition;
    }

    public void setDefinition(Definition definition) {
        this.definition = definition;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getRuleGroupId() {
        return ruleGroupId;
    }

    public void setRuleGroupId(Long ruleGroupId) {
        this.ruleGroupId = ruleGroupId;
    }
}
