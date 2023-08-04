package com.electronicstore.springboot.model;

import java.util.HashSet;
import java.util.Set;

public class FreeProductRule {

    private Set<Long> triggerProducts;
    private Set<Long> freeProducts;

    public FreeProductRule(){
        triggerProducts = new HashSet<>();
        freeProducts = new HashSet<>();
    }

    public void setTriggerProducts(Set<Long> ids) {
        triggerProducts = ids;
    }

    public void setFreeProducts(Set<Long> ids) {
        freeProducts = ids;
    }

    public Set<Long> getTriggerProducts() {
        return triggerProducts;
    }

    public Set<Long> getFreeProducts() {
        return freeProducts;
    }
}
