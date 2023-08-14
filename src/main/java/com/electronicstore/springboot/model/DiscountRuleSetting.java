package com.electronicstore.springboot.model;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;


@Getter
@Setter
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

}
