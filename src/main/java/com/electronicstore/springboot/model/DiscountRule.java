package com.electronicstore.springboot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class DiscountRule {

    public enum ThresholdType {Qty, Amount}
    public enum ApplicableType {Qty, Amount}
    public enum ThresholdProductType {Any, All}
    public enum ApplicableProductType {Identity, All /*,Any?*/}

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "discount_rule_id_seq")
    @SequenceGenerator(name = "discount_rule_id_seq", sequenceName = "discount_rule_id_seq", allocationSize = 1)
    private Long id;

    private Long thresholdUnit;

    @Enumerated(EnumType.STRING)
    private ThresholdType thresholdUnitType;

    private Long applicableUnit;

    @Enumerated(EnumType.STRING)
    private ApplicableType applicableUnitType;

    private Double applicableDiscount;

    private Double overrideAmount;

    private String description;

    @Enumerated(EnumType.STRING)
    private ThresholdProductType thresholdProductType;

    private Long ruleGroupId;

    @Enumerated(EnumType.STRING)
    private ApplicableProductType applicableProductType;

    private Long applicableRuleGroupId;

}
