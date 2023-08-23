package com.electronicstore.springboot.dto;

import com.electronicstore.springboot.model.Product;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.electronicstore.springboot.model.DiscountRule.ThresholdType;
import static com.electronicstore.springboot.model.DiscountRuleSetting.Group;
import static java.util.stream.Collectors.toMap;

public class DealMatchRequest {

    private Map<Group, Map<Long, Map<ThresholdType, Double>>> characteristic;
    private Map<Long, Map<Long, Map<ThresholdType, Double>>> mapToCartItemId;

    public DealMatchRequest(){
        characteristic = Arrays.stream(Group.values()).collect(toMap(Function.identity(), g ->
                switch (g) {
                    case all -> Collections.singletonMap(0L, initThresholdMap(0L));
                    default -> new HashMap<>();
        }));
        mapToCartItemId = new HashMap<>();
    }

    private Map<ThresholdType, Double> initThresholdMap(long id) {
        return Arrays.stream(ThresholdType.values()).collect(toMap(Function.identity(), t -> 0.0));
    }

    public void addCharacteristic(Product product, ThresholdType type, double value, Long itemId) {
        characteristic.get(Group.all).get(0L).merge(type, value, Double::sum);
        characteristic.get(Group.category).computeIfAbsent(product.getCategoryId(), i-> new HashMap<>()).merge(type, value, Double::sum);
        characteristic.get(Group.product).computeIfAbsent(product.getId(), i-> new HashMap<>()).merge(type, value, Double::sum);
        mapToCartItemId.computeIfAbsent(product.getId(), i->new HashMap<>())
                .computeIfAbsent(itemId, i->new HashMap<>()).put(type, value);
    }

    public Map<Group, Map<Long, Map<ThresholdType, Double>>> getCharacteristic() {
        return characteristic;
    }

    public Map<Long, Map<Long, Map<ThresholdType, Double>>> getMapToCartItemId() {
        return mapToCartItemId;
    }

}
