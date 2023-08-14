package com.electronicstore.springboot.dto;

import com.electronicstore.springboot.model.DiscountRule;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class DealMatchResponse {

    private Map<Long, List<DiscountRule>> itemIdToDeals;
    private Map<Long, Double> itemDiscountAmount;

    public DealMatchResponse() {
        this.itemIdToDeals = new HashMap<>();
        this.itemDiscountAmount = new HashMap<>();
    }

}
