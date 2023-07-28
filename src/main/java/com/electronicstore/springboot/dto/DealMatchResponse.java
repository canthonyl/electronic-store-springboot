package com.electronicstore.springboot.dto;

import com.electronicstore.springboot.model.DiscountRule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DealMatchResponse {

    private Map<Long, List<DiscountRule>> itemIdToDeals;
    private Map<Long, Double> itemDiscountAmount;

    public DealMatchResponse() {
        this.itemIdToDeals = new HashMap<>();
        this.itemDiscountAmount = new HashMap<>();
    }

    public Map<Long, Double> getItemsDiscountAmount() {
        return itemDiscountAmount;
    }

    public Map<Long, List<DiscountRule>> getItemIdToDeals() {
        return itemIdToDeals;
    }

}
