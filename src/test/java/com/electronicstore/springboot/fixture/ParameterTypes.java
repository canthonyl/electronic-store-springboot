package com.electronicstore.springboot.fixture;

import com.electronicstore.springboot.model.DiscountRule;
import com.electronicstore.springboot.model.DiscountRuleSetting;
import com.electronicstore.springboot.model.Product;
import com.electronicstore.springboot.model.ProductCategory;
import com.electronicstore.springboot.model.ShoppingCartItem;
import com.google.gson.Gson;
import io.cucumber.java.DataTableType;
import io.cucumber.java.ParameterType;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.util.introspection.CaseFormatUtils.toCamelCase;

public class ParameterTypes {

    private Gson gson = new Gson();

    @DataTableType
    public ProductCategory convertProductCategory(Map<String, String> entry) {
        Map<String, String> map = new HashMap<>();
        entry.forEach((k,v) -> map.put(toCamelCase(k), v));

        ProductCategory p = new ProductCategory();
        Optional.ofNullable(map.get("id")).map(Long::valueOf).ifPresent(p::setId);
        Optional.ofNullable(map.get("name")).ifPresent(p::setName);
        return p;
    }

    @DataTableType
    public Product convertProduct(Map<String, String> entry) {
        Map<String, String> map = new HashMap<>();
        entry.forEach((k,v) -> map.put(toCamelCase(k), v));

        Product p = new Product();
        Optional.ofNullable(map.get("id")).map(Long::valueOf).ifPresent(p::setId);
        Optional.ofNullable(map.get("name")).ifPresent(p::setName);
        Optional.ofNullable(map.get("description")).ifPresent(p::setDescription);
        Optional.ofNullable(map.get("price")).map(Double::valueOf).ifPresent(p::setPrice);
        Optional.ofNullable(map.get("categoryId")).map(Long::valueOf).ifPresent(p::setCategoryId);

        return p;
    }

    @DataTableType
    public DiscountRule convertDiscountRule(Map<String, String> entry) {
        Map<String, String> map = new HashMap<>();
        entry.forEach((k,v) -> map.put(toCamelCase(k), v));

        DiscountRule p = new DiscountRule();
        Optional.ofNullable(map.get("id")).map(Long::valueOf).ifPresent(p::setId);
        Optional.ofNullable(map.get("thresholdUnit")).map(Long::valueOf).ifPresent(p::setThresholdUnit);
        Optional.ofNullable(map.get("thresholdUnitType")).map(DiscountRule.ThresholdType::valueOf).ifPresent(p::setThresholdUnitType);
        Optional.ofNullable(map.get("applicableUnit")).map(Long::valueOf).ifPresent(p::setApplicableUnit);
        Optional.ofNullable(map.get("applicableUnitType")).map(DiscountRule.ApplicableType::valueOf).ifPresent(p::setApplicableUnitType);
        Optional.ofNullable(map.get("applicableDiscount")).map(Double::valueOf).ifPresent(p::setApplicableDiscount);
        Optional.ofNullable(map.get("overrideAmount")).map(Double::valueOf).ifPresent(p::setOverrideAmount);
        Optional.ofNullable(map.get("description")).ifPresent(p::setDescription);
        Optional.ofNullable(map.get("ruleGroupId")).map(Long::valueOf).ifPresent(p::setRuleGroupId);
        return p;
    }

    @DataTableType
    public DiscountRuleSetting convertDiscountRuleSetting(Map<String, String> entry) {
        Map<String, String> map = new HashMap<>();
        entry.forEach((k,v) -> map.put(toCamelCase(k), v));

        DiscountRuleSetting p = new DiscountRuleSetting();
        Optional.ofNullable(map.get("id")).map(Long::valueOf).ifPresent(p::setId);
        Optional.ofNullable(map.get("ruleGroupId")).map(Long::valueOf).ifPresent(p::setRuleGroupId);
        Optional.ofNullable(map.get("categoryId")).map(Long::valueOf).ifPresent(p::setCategoryId);
        Optional.ofNullable(map.get("productId")).map(Long::valueOf).ifPresent(p::setProductId);

        return p;
    }

    @DataTableType
    public ShoppingCartItem convertShoppingCartItem(Map<String, String> entry) {
        Map<String, String> map = new HashMap<>();
        entry.forEach((k,v) -> map.put(toCamelCase(k), v));

        ShoppingCartItem e = new ShoppingCartItem();
        Optional.ofNullable(map.get("id")).map(Long::valueOf).ifPresent(e::setId);
        Optional.ofNullable(map.get("cartId")).map(Long::valueOf).ifPresent(e::setShoppingCartId);
        Optional.ofNullable(map.get("productId")).map(Long::valueOf).ifPresent(e::setProductId);
        Optional.ofNullable(map.get("quantity")).map(Integer::valueOf).ifPresent(e::setQuantity);
        Optional.ofNullable(map.get("price")).map(Double::valueOf).ifPresent(e::setPrice);
        Optional.ofNullable(map.get("amountBeforeDiscount")).map(Double::valueOf).ifPresent(e::setAmountBeforeDiscount);
        Optional.ofNullable(map.get("discountAmount")).map(Double::valueOf).ifPresent(e::setDiscountAmount);
        Optional.ofNullable(map.get("amount")).map(Double::valueOf).ifPresent(e::setAmount);
        Optional.ofNullable(map.get("discountApplied")).map(this::toList).ifPresent(e::setDiscountApplied);

        return e;
    }

    private List<String> toList(String s) {
        String[] array = s.trim().split(",",-1);
        return Arrays.stream(array).map(String::trim).toList();
    }

    @ParameterType("(GET|HEAD|POST|PUT|PATCH|DELETE|OPTIONS|TRACE)")
    public HttpMethod httpMethod(String val) {
        return HttpMethod.valueOf(val);
    }

    @ParameterType(".*")
    public HttpStatus httpStatus(String val) {
        return HttpStatus.valueOf(val);
    }

}
