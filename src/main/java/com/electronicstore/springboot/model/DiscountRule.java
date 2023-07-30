package com.electronicstore.springboot.model;

//import javax.persistence.Entity;

import jakarta.persistence.*;

@Entity
public class DiscountRule {

    public enum ThresholdType {Qty, Amount}
    public enum ApplicableType {Qty, Amount}

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "discount_rule_id_seq")
    @SequenceGenerator(name = "discount_rule_id_seq", sequenceName = "discount_rule_id_seq", allocationSize = 1)
    private Long id;

    private long thresholdUnit;

    @Enumerated(EnumType.STRING)
    private ThresholdType thresholdUnitType;

    private long applicableUnit;

    @Enumerated(EnumType.STRING)
    private ApplicableType applicableUnitType;

    private double applicableDiscount;

    private double overrideAmount;

    private String description;

    private long ruleGroupId;

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

    public long getThresholdUnit() {
        return thresholdUnit;
    }

    public void setThresholdUnit(long thresholdUnit) {
        this.thresholdUnit = thresholdUnit;
    }

    public ApplicableType getApplicableUnitType() {
        return applicableUnitType;
    }

    public void setApplicableUnitType(ApplicableType applicableUnitType) {
        this.applicableUnitType = applicableUnitType;
    }

    public long getApplicableUnit() {
        return applicableUnit;
    }

    public void setApplicableUnit(long applicableUnit) {
        this.applicableUnit = applicableUnit;
    }

    public double getApplicableDiscount() {
        return applicableDiscount;
    }

    public void setApplicableDiscount(double applicableDiscount) {
        this.applicableDiscount = applicableDiscount;
    }

    public double getOverrideAmount() {
        return overrideAmount;
    }

    public void setOverrideAmount(double overrideAmount) {
        this.overrideAmount = overrideAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getRuleGroupId() {
        return ruleGroupId;
    }

    public void setRuleGroupId(long ruleGroupId) {
        this.ruleGroupId = ruleGroupId;
    }
}
