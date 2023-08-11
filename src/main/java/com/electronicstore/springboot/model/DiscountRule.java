package com.electronicstore.springboot.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;

@Entity
public class DiscountRule {

    public enum ThresholdType {Qty, Amount}
    public enum ApplicableType {Qty, Amount}

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

    private Long ruleGroupId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ThresholdType getThresholdUnitType() {
        return thresholdUnitType;
    }

    public void setThresholdUnitType(ThresholdType thresholdUnitType) {
        this.thresholdUnitType = thresholdUnitType;
    }

    public Long getThresholdUnit() {
        return thresholdUnit;
    }

    public void setThresholdUnit(Long thresholdUnit) {
        this.thresholdUnit = thresholdUnit;
    }

    public ApplicableType getApplicableUnitType() {
        return applicableUnitType;
    }

    public void setApplicableUnitType(ApplicableType applicableUnitType) {
        this.applicableUnitType = applicableUnitType;
    }

    public Long getApplicableUnit() {
        return applicableUnit;
    }

    public void setApplicableUnit(Long applicableUnit) {
        this.applicableUnit = applicableUnit;
    }

    public Double getApplicableDiscount() {
        return applicableDiscount;
    }

    public void setApplicableDiscount(Double applicableDiscount) {
        this.applicableDiscount = applicableDiscount;
    }

    public Double getOverrideAmount() {
        return overrideAmount;
    }

    public void setOverrideAmount(Double overrideAmount) {
        this.overrideAmount = overrideAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getRuleGroupId() {
        return ruleGroupId;
    }

    public void setRuleGroupId(Long ruleGroupId) {
        this.ruleGroupId = ruleGroupId;
    }
}
