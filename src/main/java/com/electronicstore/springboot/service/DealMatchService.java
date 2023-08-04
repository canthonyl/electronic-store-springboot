package com.electronicstore.springboot.service;

import com.electronicstore.springboot.dao.EntityDatastore;
import com.electronicstore.springboot.dto.DealMatchRequest;
import com.electronicstore.springboot.dto.DealMatchResponse;
import com.electronicstore.springboot.model.DiscountRule;
import com.electronicstore.springboot.model.DiscountRuleSetting;
import com.electronicstore.springboot.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

@Service
public class DealMatchService {

    @Autowired
    private EntityDatastore<DiscountRuleSetting> discountRuleSettingDatastore;

    @Autowired
    private EntityDatastore<DiscountRule> discountRuleDatastore;

    @Autowired
    private ProductService productService;

    //private Map<Long, FreeProductRule> freeProductRules;

    public DealMatchService(){
        /*freeProductRules = new HashMap<>();
        FreeProductRule freeProductRule1 = new FreeProductRule();
        freeProductRule1.setTriggerProducts(Set.of(1L, 2L));
        freeProductRule1.setFreeProducts(Set.of(3L));
        freeProductRules.put(1L, freeProductRule1);*/
    }

    public List<DiscountRuleSetting> lookupRuleByCategoryOrProduct(Collection<Long> catId, Collection<Long> productId) {
        Map<String, Collection> criteria = new HashMap<>();
        if (catId.size()>0) {
            criteria.put("category_id", catId);
        }
        if (productId.size()>0) {
            criteria.put("product_id", productId);
        }
        if (criteria.size()>0) {
            return discountRuleSettingDatastore.findMatchingValuesIn(criteria);
        } else {
            return Collections.emptyList();
        }
    }

    public List<DiscountRule> lookupRuleByGroupId(Collection<Long> ruleGroupId) {
        if (ruleGroupId.size()>0) {
            return discountRuleDatastore.findMatchingValuesIn("rule_group_id", ruleGroupId);
        } else {
            return Collections.emptyList();
        }
    }

    public DealMatchResponse matchDeals(DealMatchRequest request) {

        Set<Long> catId = request.getCharacteristic().get(DiscountRuleSetting.Group.category).keySet();
        Set<Long> productId = request.getCharacteristic().get(DiscountRuleSetting.Group.product).keySet();
        Map<Long, Product> productById = productService.getProducts(productId).stream().collect(toMap(Product::getId, Function.identity()));
        Map<Long, Set<Long>> productByCatId = productById.values().stream().collect(groupingBy(Product::getCategoryId, mapping(Product::getId, toSet())));
        Map<Long, Map<DiscountRule.ThresholdType, Double>> characteristicsByProductId = request.getCharacteristic().get(DiscountRuleSetting.Group.product);

        List<DiscountRuleSetting> applicableRuleSettings = lookupRuleByCategoryOrProduct(catId, productId);

        Map<Long, DiscountRule> applicableRules = lookupRuleByGroupId(applicableRuleSettings.stream()
                .map(DiscountRuleSetting::getRuleGroupId).distinct().collect(toList()))
                .stream().collect(toMap(DiscountRule::getId, Function.identity()));

        Map<Long, List<DiscountRule>> applicableRulesByGroup = applicableRules.values().stream()
                .collect(groupingBy(DiscountRule::getRuleGroupId, mapping(Function.identity(), toList())));

        Map<Long, Set<Long>> productIdToRuleGroups = new HashMap<>();

        /*Map<Long, List<Long>> productIdToItems = request.getMapToCartItemId().get(DiscountRuleSetting.Group.product);
        Map<Long, List<Long>> workingProductIdToItems = new HashMap<>();
        productIdToItems.forEach((id, list) -> {
            workingProductIdToItems.put(id, new LinkedList<>(list));
        });

        Map<Long, List<Long>> freeItemIdListByProduct = new HashMap<>();
        for (FreeProductRule rule : freeProductRules.values()) {
            if (productId.containsAll(rule.getTriggerProducts())) {
                Set<Long> freeProducts = rule.getFreeProducts();
                for (Long freeProductId : freeProducts) {
                    List<Long> itemList = workingProductIdToItems.get(freeProductId);
                    freeItemIdListByProduct.computeIfAbsent(freeProductId, i->new LinkedList<>())
                            .add(itemList.remove(0));
                }
            }
        }*/



        for (DiscountRuleSetting setting : applicableRuleSettings) {
            Long ruleGroupId = setting.getRuleGroupId();
            Optional<Long> ruleCategoryId = Optional.ofNullable(setting.getCategoryId());
            Optional<Long> ruleProductId = Optional.ofNullable(setting.getProductId());

            if (ruleCategoryId.isPresent()) {
                productByCatId.getOrDefault(ruleCategoryId.get(), Collections.emptySet())
                    .forEach( id -> productIdToRuleGroups.computeIfAbsent(id, i -> new HashSet<>()).add(ruleGroupId));
            }
            if (ruleProductId.isPresent()) {
                productIdToRuleGroups.computeIfAbsent(ruleProductId.get(), i->new HashSet<>()).add(ruleGroupId);
            }
        }



        Map<Long, List<Long>> mapProductIdToCartItemId = request.getMapToCartItemId().get(DiscountRuleSetting.Group.product);

        DealMatchResponse response = new DealMatchResponse();

        /*Map<Long, Double> discountAmounts = response.getItemsDiscountAmount();
        for (Long freeProduct : freeItemIdListByProduct.keySet()) {
            List<Long> relatedProductIds = freeItemIdListByProduct.get(freeProduct);
            Double totalAmount = characteristicsByProductId.get(freeProduct).get(DiscountRule.ThresholdType.Amount);
            Double quantity = characteristicsByProductId.get(freeProduct).get(DiscountRule.ThresholdType.Qty);
            Double amountBeforeDiscount = totalAmount / quantity;
            for (Long itemId : relatedProductIds) {
                discountAmounts.put(itemId, totalAmount);
            }

        }*/


        for (Map.Entry<Long, Set<Long>> e : productIdToRuleGroups.entrySet()) {
            Product product = productById.get(e.getKey());
            Set<Long> ruleGroupIds = e.getValue();

            double savingAmount = 0;
            long maxSavingAmountRuleGroupId = 0L;
            List<Long> ruleResult = Collections.emptyList();
            DiscountRule.ApplicableType discountType = DiscountRule.ApplicableType.Qty;

            Map<DiscountRule.ThresholdType, Double> thresholdValues = characteristicsByProductId.get(product.getId());
            for (Long ruleGroupId : ruleGroupIds) {
                Map<DiscountRule.ThresholdType, List<DiscountRule>> rulesInGroup = applicableRulesByGroup.get(ruleGroupId)
                        .stream()
                        .filter(r -> applicable(r, thresholdValues))
                        .collect(groupingBy(DiscountRule::getThresholdUnitType, mapping(Function.identity(), toList())));

                double savingAmountCandidate = 0.0;
                List<Long> bestDeals = new LinkedList<>();
                if (rulesInGroup.containsKey(DiscountRule.ThresholdType.Amount)) {
                    savingAmountCandidate = resolveBestDealsByAmount(rulesInGroup.get(DiscountRule.ThresholdType.Amount), thresholdValues, bestDeals);
                    if (bestDeals.size() > 0) {
                        if (savingAmountCandidate > savingAmount) {
                            maxSavingAmountRuleGroupId = ruleGroupId;
                            savingAmount = savingAmountCandidate;
                            ruleResult = bestDeals;
                            discountType = DiscountRule.ApplicableType.Amount;
                        }
                    }
                }
                bestDeals = new LinkedList<>();
                if (rulesInGroup.containsKey(DiscountRule.ThresholdType.Qty)) {
                    savingAmountCandidate = resolveBestDealsByQty(rulesInGroup.get(DiscountRule.ThresholdType.Qty), thresholdValues, product, bestDeals);
                    if (bestDeals.size() > 0) {
                        if (savingAmountCandidate > savingAmount) {
                            maxSavingAmountRuleGroupId = ruleGroupId;
                            savingAmount = savingAmountCandidate;
                            ruleResult = bestDeals;
                            discountType = DiscountRule.ApplicableType.Qty;
                        }
                    }
                }
            }

            //assign deals to items
            List<Long> itemIdsForProduct = mapProductIdToCartItemId.get(product.getId());
            List<DiscountRule> ruleDetails = ruleResult.stream().map(applicableRules::get).toList();
            response.getItemIdToDeals().put(itemIdsForProduct.get(0), ruleDetails);
            response.getItemsDiscountAmount().put(itemIdsForProduct.get(0), savingAmount);
        }

        return response;
    }

    private double resolveBestDealsByAmount(List<DiscountRule> allDeals, Map<DiscountRule.ThresholdType, Double> thresholdValues, List<Long> result) {
        Map<Long, Double> amount = allDeals.stream()
                .collect(toMap(DiscountRule::getId, r -> applyAmountBasedDiscount(r, thresholdValues)));
        Map.Entry<Long, Double> bestEntry = amount.entrySet().stream().max(Comparator.comparingDouble(Map.Entry::getValue)).get();
        result.add(bestEntry.getKey());
        return bestEntry.getValue();
    }

    private double resolveBestDealsByQty(List<DiscountRule> allDeals, Map<DiscountRule.ThresholdType, Double> thresholdValues, Product product, List<Long> result) {
        //assume amount based discount are exclusive to one another while quantity based are not
        Map<Long, Double> dealSelectionSavingAmount = new HashMap<>();
        Map<Long, Set<Long>> dealSelections = new HashMap<>();

        Comparator<DiscountRule> comparator = (r1, r2) -> Double.compare(overallQuantityBasedDiscount(r1), overallQuantityBasedDiscount(r2));

        List<DiscountRule> sorted = allDeals.stream()
                .sorted(comparator)
                .toList();

        double minApplicableQty = sorted.stream().map(DiscountRule::getApplicableUnit).min(Long::compare).get().doubleValue();

        for (int i=0; i<sorted.size(); i++){
            int j = i;

            Set<Long> selectedRules = new HashSet<>();
            double savingAmount = 0.0;
            //double workingQty = thresholdValues.get(DiscountRule.ThresholdType.Qty);
            Map<DiscountRule.ThresholdType, Double> workingThresholdValues = new HashMap<>(thresholdValues);

            while (j < sorted.size() && workingThresholdValues.get(DiscountRule.ThresholdType.Qty) >= minApplicableQty) {
                DiscountRule currentRule = sorted.get(j);
                if (applicable(currentRule, workingThresholdValues)) {
                    savingAmount += applyAndUpdateQtyBasedDiscount(currentRule, workingThresholdValues, product);
                    selectedRules.add(currentRule.getId());
                }
                j++;
            }
            if (savingAmount > 0) {
                Long trackId = dealSelections.size()+1L;
                dealSelections.put(trackId, selectedRules);
                dealSelectionSavingAmount.put(trackId, savingAmount);
            }
        }

        Long bestSavingSelectionId = dealSelectionSavingAmount.entrySet().stream().max(Comparator.comparingDouble(Map.Entry::getValue)).get().getKey();
        result.addAll(dealSelections.get(bestSavingSelectionId).stream().toList());
        return dealSelectionSavingAmount.get(bestSavingSelectionId);
    }

    private double applyAndUpdateQtyBasedDiscount(DiscountRule r, Map<DiscountRule.ThresholdType, Double> thresholdValues, Product product) {
        Double qty = thresholdValues.get(DiscountRule.ThresholdType.Qty);
        Double amount = thresholdValues.get(DiscountRule.ThresholdType.Amount);
        long thresholdQty = r.getThresholdUnit();
        long numTimes = Double.valueOf(qty / thresholdQty).longValue();
        long totalApplicableQty = numTimes * r.getApplicableUnit();
        double discountAmount = totalApplicableQty * product.getPrice() * r.getApplicableDiscount();

        thresholdValues.put(DiscountRule.ThresholdType.Qty, qty - numTimes * thresholdQty);
        thresholdValues.put(DiscountRule.ThresholdType.Amount, amount - discountAmount);
        return discountAmount;
    }

    private double applyAmountBasedDiscount(DiscountRule r, Map<DiscountRule.ThresholdType, Double> thresholdValues) {
        //>0 applies up to max(applicable unit, actual)
        if (r.getOverrideAmount() > 0) {
            return r.getOverrideAmount();
        } else {
            if (r.getApplicableUnitType() == DiscountRule.ApplicableType.Amount) {
                return thresholdValues.get(DiscountRule.ThresholdType.Amount) * r.getApplicableDiscount();
            } else {
                double percentageApplied = r.getApplicableUnit() / thresholdValues.get(DiscountRule.ThresholdType.Qty);
                return thresholdValues.get(DiscountRule.ThresholdType.Amount) * percentageApplied * r.getApplicableDiscount();
            }
        }
    }

    private double overallQuantityBasedDiscount(DiscountRule rule) {
        return (rule.getApplicableUnit() * rule.getApplicableDiscount() + (rule.getThresholdUnit()-rule.getApplicableUnit()))
                / rule.getThresholdUnit();
    }

    private boolean applicable(DiscountRule rule, Map<DiscountRule.ThresholdType, Double> thresholdValues) {
        double ruleThreshold = rule.getThresholdUnit();
        double value = thresholdValues.get(rule.getThresholdUnitType());
        return value >= ruleThreshold;
    }


}
