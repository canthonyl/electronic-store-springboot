package com.electronicstore.springboot.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class FreeProductRule {

    private Set<Long> triggerProducts;
    private Set<Long> freeProducts;

    public FreeProductRule(){
        triggerProducts = new HashSet<>();
        freeProducts = new HashSet<>();
    }

}
