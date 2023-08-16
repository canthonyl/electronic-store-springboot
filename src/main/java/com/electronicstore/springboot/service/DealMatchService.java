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
import static com.electronicstore.springboot.model.DiscountRule.ThresholdType;
import static com.electronicstore.springboot.model.DiscountRule.ThresholdProductType;
import static com.electronicstore.springboot.model.DiscountRule.ApplicableType;

@Service
public class DealMatchService {

    @Autowired
    private EntityDatastore<DiscountRuleSetting> discountRuleSettingDatastore;

    @Autowired
    private EntityDatastore<DiscountRule> discountRuleDatastore;

    @Autowired
    private ProductService productService;

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

    class DealMatchContext {

        List<DiscountRuleSetting> allRuleSettings;
        Map<Long, Set<Long>> ruleGroupCoveredCategories;
        Map<Long, Set<Long>> ruleGroupCoveredProducts;
        Set<Long> ruleGroupIds;

        Map<Long, DiscountRule> applicableRules;
        Map<Long, List<DiscountRule>> applicableRulesByGroup;

        Map<Long, Product> productById;
        Map<Long, Set<Long>> productByCatId;

        DealMatchContext() { }

        public void setAllRuleSettings(List<DiscountRuleSetting> ruleSettings) {
            allRuleSettings = ruleSettings;

            ruleGroupCoveredCategories = allRuleSettings.stream()
                    .filter(s -> s.getCategoryId() != null)
                    .collect(groupingBy(DiscountRuleSetting::getRuleGroupId, mapping(DiscountRuleSetting::getCategoryId, toSet())));

            ruleGroupCoveredProducts = allRuleSettings.stream()
                    .filter(s -> s.getProductId() != null)
                    .collect(groupingBy(DiscountRuleSetting::getRuleGroupId, mapping(DiscountRuleSetting::getProductId, toSet())));

            ruleGroupIds = allRuleSettings.stream()
                    .map(DiscountRuleSetting::getRuleGroupId).collect(toSet());
        }

        public void setAllRule(List<DiscountRule> rules) {
            applicableRules = rules.stream()
                    .filter(d ->
                            // applicableByProduct(d, catId, productId, ruleGroupCoveredCategories, ruleGroupCoveredProducts)
                            d.getThresholdProductType() == ThresholdProductType.Any ||
                                    productByCatId.keySet().containsAll(ruleGroupCoveredCategories.get(d.getRuleGroupId()))
                                            && productById.keySet().containsAll(ruleGroupCoveredProducts.get(d.getRuleGroupId()))
                    )
                    .collect(toMap(DiscountRule::getId, Function.identity()));

            applicableRulesByGroup = applicableRules.values().stream()
                    .collect(groupingBy(DiscountRule::getRuleGroupId, mapping(Function.identity(), toList())));
        }


        public void setProducts(List<Product> products) {
            productById = products.stream().collect(toMap(Product::getId, Function.identity()));
            productByCatId = productById.values().stream().collect(groupingBy(Product::getCategoryId, mapping(Product::getId, toSet())));
        }


    }

    public DealMatchResponse matchDeals(DealMatchRequest request) {

        Set<Long> catId = request.getCharacteristic().get(DiscountRuleSetting.Group.category).keySet();
        Set<Long> productId = request.getCharacteristic().get(DiscountRuleSetting.Group.product).keySet();
        Map<Long, Map<ThresholdType, Double>> characteristicsByProductId = request.getCharacteristic().get(DiscountRuleSetting.Group.product);
        Map<Long, List<Long>> mapProductIdToCartItemId = request.getMapToCartItemId().get(DiscountRuleSetting.Group.product);

        DealMatchContext dmc = new DealMatchContext();
        dmc.setProducts(productService.getProducts(productId));
        dmc.setAllRuleSettings(lookupRuleByCategoryOrProduct(catId, productId));
        dmc.setAllRule(lookupRuleByGroupId(dmc.ruleGroupIds));

        Map<Long, Set<Long>> productSelection = new HashMap<>();
        Map<Long, Set<Long>> selectionIdToRuleGroups = new HashMap<>();
        productIdToRuleGroups(dmc, productSelection, selectionIdToRuleGroups);

        DealMatchResponse response = new DealMatchResponse();

        for (Map.Entry<Long, Set<Long>> e : selectionIdToRuleGroups.entrySet()) {
            Long productSelectId = e.getKey();
            Product product = dmc.productById.get(productSelectId);
            //Set<Long> products
            Set<Long> ruleGroupIds = e.getValue();

            double savingAmount = 0;
            long maxSavingAmountRuleGroupId = 0L;
            List<Long> ruleResult = Collections.emptyList();
            ApplicableType discountType = ApplicableType.Qty;

            Map<ThresholdType, Double> thresholdValues = characteristicsByProductId.get(product.getId());
            for (Long ruleGroupId : ruleGroupIds) {
                Map<ThresholdType, List<DiscountRule>> rulesInGroup = dmc.applicableRulesByGroup.get(ruleGroupId)
                        .stream()
                        .filter(r -> applicable(r, thresholdValues))
                        .collect(groupingBy(DiscountRule::getThresholdUnitType, mapping(Function.identity(), toList())));

                double savingAmountCandidate = 0.0;
                List<Long> bestDeals = new LinkedList<>();
                if (rulesInGroup.containsKey(ThresholdType.Amount)) {
                    savingAmountCandidate = resolveBestDealsByAmount(rulesInGroup.get(ThresholdType.Amount), thresholdValues, bestDeals);
                    if (bestDeals.size() > 0) {
                        if (savingAmountCandidate > savingAmount) {
                            maxSavingAmountRuleGroupId = ruleGroupId;
                            savingAmount = savingAmountCandidate;
                            ruleResult = bestDeals;
                            discountType = ApplicableType.Amount;
                        }
                    }
                }
                bestDeals = new LinkedList<>();
                if (rulesInGroup.containsKey(ThresholdType.Qty)) {
                    savingAmountCandidate = resolveBestDealsByQty(rulesInGroup.get(ThresholdType.Qty), thresholdValues, product, bestDeals);
                    if (bestDeals.size() > 0) {
                        if (savingAmountCandidate > savingAmount) {
                            maxSavingAmountRuleGroupId = ruleGroupId;
                            savingAmount = savingAmountCandidate;
                            ruleResult = bestDeals;
                            discountType = ApplicableType.Qty;
                        }
                    }
                }
            }

            //assign deals to items
            List<Long> itemIdsForProduct = mapProductIdToCartItemId.get(product.getId());
            List<DiscountRule> ruleDetails = ruleResult.stream().map(dmc.applicableRules::get).toList();
            response.getItemIdToDeals().put(itemIdsForProduct.get(0), ruleDetails);
            response.getItemDiscountAmount().put(itemIdsForProduct.get(0), savingAmount);
        }

        return response;
    }

    //private Map<Long, Set<Long>> productIdToRuleGroups(List<DiscountRuleSetting> applicableRuleSettings, Map<Long, Set<Long>> productByCatId) {
    private void productIdToRuleGroups(DealMatchContext dmc, Map<Long, Set<Long>> productSelection, Map<Long, Set<Long>> selectionIdToRuleGroups) {
        for (DiscountRuleSetting setting : dmc.allRuleSettings) {
            Long ruleGroupId = setting.getRuleGroupId();
            Optional<Long> ruleCategoryId = Optional.ofNullable(setting.getCategoryId());
            Optional<Long> ruleProductId = Optional.ofNullable(setting.getProductId());

            if (ruleCategoryId.isPresent()) {
                dmc.productByCatId.getOrDefault(ruleCategoryId.get(), Collections.emptySet())
                        .forEach( id -> selectionIdToRuleGroups.computeIfAbsent(id, i -> new HashSet<>()).add(ruleGroupId));
            }
            if (ruleProductId.isPresent()) {
                selectionIdToRuleGroups.computeIfAbsent(ruleProductId.get(), i->new HashSet<>()).add(ruleGroupId);
            }
        }
    }

    private double resolveBestDealsByAmount(List<DiscountRule> allDeals, Map<ThresholdType, Double> thresholdValues, List<Long> result) {
        Map<Long, Double> amount = allDeals.stream()
                .collect(toMap(DiscountRule::getId, r -> applyAmountBasedDiscount(r, thresholdValues)));
        Map.Entry<Long, Double> bestEntry = amount.entrySet().stream().max(Comparator.comparingDouble(Map.Entry::getValue)).get();
        result.add(bestEntry.getKey());
        return bestEntry.getValue();
    }

    private double resolveBestDealsByQty(List<DiscountRule> allDeals, Map<ThresholdType, Double> thresholdValues, Product product, List<Long> result) {
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
            //double workingQty = thresholdValues.get(ThresholdType.Qty);
            Map<ThresholdType, Double> workingThresholdValues = new HashMap<>(thresholdValues);

            while (j < sorted.size() && workingThresholdValues.get(ThresholdType.Qty) >= minApplicableQty) {
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

    private double applyAndUpdateQtyBasedDiscount(DiscountRule r, Map<ThresholdType, Double> thresholdValues, Product product) {
        Double qty = thresholdValues.get(ThresholdType.Qty);
        Double amount = thresholdValues.get(ThresholdType.Amount);
        long thresholdQty = r.getThresholdUnit();
        long numTimes = Double.valueOf(qty / thresholdQty).longValue();
        long totalApplicableQty = numTimes * r.getApplicableUnit();
        double discountAmount = totalApplicableQty * product.getPrice() * r.getApplicableDiscount();

        thresholdValues.put(ThresholdType.Qty, qty - numTimes * thresholdQty);
        thresholdValues.put(ThresholdType.Amount, amount - discountAmount);
        return discountAmount;
    }

    private double applyAmountBasedDiscount(DiscountRule r, Map<ThresholdType, Double> thresholdValues) {
        //>0 applies up to max(applicable unit, actual)
        if (r.getOverrideAmount() > 0) {
            return r.getOverrideAmount();
        } else {
            if (r.getApplicableUnitType() == ApplicableType.Amount) {
                return thresholdValues.get(ThresholdType.Amount) * r.getApplicableDiscount();
            } else {
                double percentageApplied = r.getApplicableUnit() / thresholdValues.get(ThresholdType.Qty);
                return thresholdValues.get(ThresholdType.Amount) * percentageApplied * r.getApplicableDiscount();
            }
        }
    }

    private double overallQuantityBasedDiscount(DiscountRule rule) {
        return (rule.getApplicableUnit() * rule.getApplicableDiscount() + (rule.getThresholdUnit()-rule.getApplicableUnit()))
                / rule.getThresholdUnit();
    }

    private boolean applicable(DiscountRule rule, Map<ThresholdType, Double> thresholdValues) {
        double ruleThreshold = rule.getThresholdUnit();
        double value = thresholdValues.get(rule.getThresholdUnitType());
        return value >= ruleThreshold;
    }


}
