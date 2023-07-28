package com.electronicstore.springboot.dto;

import com.electronicstore.springboot.model.Product;

import java.util.*;
import java.util.function.Function;

import static com.electronicstore.springboot.model.DiscountRule.ThresholdType;
import static com.electronicstore.springboot.model.DiscountRuleSetting.Group;
import static java.util.stream.Collectors.toMap;

public class DealMatchRequest {

    private Map<Group, Map<Long, Map<ThresholdType, Double>>> characteristic;
    private Map<Group, Map<Long, List<Long>>> mapToCartItemId;

    public DealMatchRequest(){
        characteristic = Arrays.stream(Group.values()).collect(toMap(Function.identity(), g ->
                switch (g) {
                    case all -> Collections.singletonMap(0L, initThresholdMap(0L));
                    default -> new HashMap<>();
        }));
        mapToCartItemId = Arrays.stream(Group.values()).filter(g -> g != Group.all)
                .collect(toMap(Function.identity(), g -> new HashMap<>()));
    }

    private Map<ThresholdType, Double> initThresholdMap(long id) {
        return Arrays.stream(ThresholdType.values()).collect(toMap(Function.identity(), t -> 0.0));
    }

    /*public void setCharacteristic(Group group, long id, ThresholdType type, double value, List<Long> relatedItemId) {
        characteristic.get(group).computeIfAbsent(id, this::initThresholdMap).put(type, value);
    }*/

    public void addCharacteristic(Product product, ThresholdType type, double value) {
        characteristic.get(Group.all).get(0L).merge(type, value, Double::sum);
        characteristic.get(Group.category).computeIfAbsent(product.getCategoryId(), i-> new HashMap<>()).merge(type, value, Double::sum);
        characteristic.get(Group.product).computeIfAbsent(product.getId(), i-> new HashMap<>()).merge(type, value, Double::sum);
    }

    public void addMapping(Product product, List<Long> itemIds) {
        mapToCartItemId.get(Group.product).merge(product.getId(), itemIds, (l1, l2) -> { l1.addAll(l2); return l1;});
    }

    public Map<Group, Map<Long, Map<ThresholdType, Double>>> getCharacteristic() {
        return characteristic;
    }

    public Map<Group, Map<Long, List<Long>>> getMapToCartItemId() {
        return mapToCartItemId;
    }

}
