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
import static com.electronicstore.springboot.model.DiscountRule.ApplicableProductType;

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
        Map<Long, Map<Long, Integer>> ruleGroupProductQuantity;
        Set<Long> ruleGroupIds;

        Map<Long, DiscountRule> allRules;
        Map<Long, List<DiscountRule>> allRulesByRuleGroupId;

        Map<Long, Product> productById;
        Map<Long, Set<Long>> productByCatId;

        DealMatchContext() { }

        public void setProducts(List<Product> products) {
            productById = products.stream().collect(toMap(Product::getId, Function.identity()));
            productByCatId = productById.values().stream().collect(groupingBy(Product::getCategoryId, mapping(Product::getId, toSet())));
        }

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

            ruleGroupProductQuantity = new HashMap<>();
            for (DiscountRuleSetting ruleSetting : allRuleSettings) {
                Map<Long, Integer> ruleQuantities = ruleGroupProductQuantity.computeIfAbsent(ruleSetting.getRuleGroupId(), r -> new HashMap<>());
                if (ruleSetting.getProductId() != null && ruleSetting.getQuantity() != null) {
                    ruleQuantities.put(ruleSetting.getProductId(), ruleSetting.getQuantity());
                }
                if (ruleSetting.getCategoryId() != null && ruleSetting.getQuantity() != null) {
                    productByCatId.get(ruleSetting.getCategoryId()).forEach(p -> {
                        ruleQuantities.put(p, ruleSetting.getQuantity());
                    });
                }
            }

        }

        public void setAllRule(List<DiscountRule> rules) {
            allRules = rules.stream()
                    .filter(r -> r.getThresholdProductType() == ThresholdProductType.Any ||
                            Optional.ofNullable(ruleGroupCoveredCategories.get(r.getRuleGroupId())).map(productByCatId.keySet()::containsAll).orElse(true)
                            && Optional.ofNullable(ruleGroupCoveredProducts.get(r.getRuleGroupId())).map(productById.keySet()::containsAll).orElse(true))
                    .collect(toMap(DiscountRule::getId, Function.identity()));

            allRulesByRuleGroupId = allRules.values().stream()
                    .collect(groupingBy(DiscountRule::getRuleGroupId, mapping(Function.identity(), toList())));
        }




    }

    public DealMatchResponse matchDeals(DealMatchRequest request) {

        Set<Long> allCategories = request.getCharacteristic().get(DiscountRuleSetting.Group.category).keySet();
        Set<Long> allProducts = request.getCharacteristic().get(DiscountRuleSetting.Group.product).keySet();
        Map<Long, Map<ThresholdType, Double>> characteristicsByProductId = request.getCharacteristic().get(DiscountRuleSetting.Group.product);
        Map<Long, Map<Long, Map<ThresholdType, Double>>> itemDetailsByProductId = request.getMapToCartItemId();

        DealMatchContext dmc = new DealMatchContext();
        dmc.setProducts(productService.getProducts(allProducts));
        dmc.setAllRuleSettings(lookupRuleByCategoryOrProduct(allCategories, allProducts));
        dmc.setAllRule(lookupRuleByGroupId(dmc.ruleGroupIds));

        Map<Long, Set<Long>> productSelection = new HashMap<>();
        Map<Long, Set<Long>> selectionIdToDiscountRules = new HashMap<>();
        productApplicableDiscountRules(dmc, productSelection, selectionIdToDiscountRules);

        DealMatchResponse response = new DealMatchResponse();

        for (Map.Entry<Long, Set<Long>> e : selectionIdToDiscountRules.entrySet()) {
            Long productSelectId = e.getKey();
            Set<Long> thresholdProducts = productSelection.get(productSelectId);
            Set<Long> ruleIds = e.getValue();

            double savingAmount = 0;
            List<Long> ruleResult = Collections.emptyList();
            List<Map<Long, Map<ThresholdType, Double>>> ruleResultDiscountDetails = Collections.emptyList();

            Map<ThresholdType, List<DiscountRule>> rules = ruleIds.stream()
                    .map(dmc.allRules::get)
                    .filter(r -> applicable(r, dmc, thresholdProducts, characteristicsByProductId))
                    .collect(groupingBy(DiscountRule::getThresholdUnitType, mapping(Function.identity(), toList())));

            double savingAmountCandidate = 0.0;
            List<Long> bestDeals = new LinkedList<>();
            List<Map<Long, Map<ThresholdType, Double>>> bestDealsTargetProducts = new LinkedList<>();
            if (rules.containsKey(ThresholdType.Amount)) {
                savingAmountCandidate = resolveBestDealsByAmount(rules.get(ThresholdType.Amount), dmc, characteristicsByProductId, thresholdProducts, bestDeals, bestDealsTargetProducts);
                if (bestDeals.size() > 0) {
                    if (savingAmountCandidate > savingAmount) {
                        savingAmount = savingAmountCandidate;
                        ruleResult = bestDeals;
                        ruleResultDiscountDetails = bestDealsTargetProducts;
                    }
                }
            }

            bestDeals = new LinkedList<>();
            bestDealsTargetProducts = new LinkedList<>();
            if (rules.containsKey(ThresholdType.Qty)) {
                savingAmountCandidate = resolveBestDealsByQty(rules.get(ThresholdType.Qty), dmc, characteristicsByProductId, thresholdProducts, bestDeals, bestDealsTargetProducts);
                if (bestDeals.size() > 0) {
                    if (savingAmountCandidate > savingAmount) {
                        savingAmount = savingAmountCandidate;
                        ruleResult = bestDeals;
                        ruleResultDiscountDetails = bestDealsTargetProducts;
                    }
                }
            }

            //assign deals to items
            for (int i=0; i < ruleResult.size(); i++) {
                Long ruleId = ruleResult.get(i);
                DiscountRule rule = dmc.allRules.get(ruleId);
                Map<Long, Map<ThresholdType, Double>> discountDetailsByProduct = ruleResultDiscountDetails.get(i);
                Set<Long> discountProductIds = discountDetailsByProduct.keySet();
                ThresholdType fillType = rule.getApplicableUnitType() == ApplicableType.Qty ? ThresholdType.Qty : ThresholdType.Amount;

                for (Long productId : discountProductIds) {
                    Double discountAmount = discountDetailsByProduct.get(productId).get(ThresholdType.Amount);
                    Double fillValue = discountDetailsByProduct.get(productId).get(fillType);
                    Map<Long, Map<ThresholdType, Double>> relatedItems = itemDetailsByProductId.get(productId);
                    Optional<Long> itemId = relatedItems.entrySet().stream()
                            .filter(r -> r.getValue().get(fillType) >= fillValue)
                            .map(Map.Entry::getKey)
                            .findFirst();
                    if (itemId.isPresent()) {
                        if (fillType == ThresholdType.Amount) {
                            relatedItems.remove(itemId.get());
                        } else {
                            Map<ThresholdType, Double> relatedItemDetail = relatedItems.get(itemId.get());
                            double qtyReduced = fillValue;
                            double amountReduced = relatedItemDetail.get(ThresholdType.Amount) * (qtyReduced / relatedItemDetail.get(ThresholdType.Qty));
                            relatedItemDetail.merge(ThresholdType.Qty, qtyReduced * -1, Double::sum);
                            relatedItemDetail.merge(ThresholdType.Amount, amountReduced * -1, Double::sum);
                        }
                        response.getItemIdToDeals().computeIfAbsent(itemId.get(), id->new LinkedList<>()).add(rule);
                        response.getItemDiscountAmount().merge(itemId.get(), discountAmount, Double::sum);
                    }
                }
            }
        }

        return response;
    }

    private void productApplicableDiscountRules(DealMatchContext dmc, Map<Long, Set<Long>> productSelection, Map<Long, Set<Long>> selectionIdToDiscountRules) {

        dmc.allRules.values().forEach(r-> {
            Set<Long> productIds = dmc.ruleGroupCoveredProducts.getOrDefault(r.getRuleGroupId(), Collections.emptySet());
            Set<Long> filteredProductIds = new HashSet<>(productIds);
            filteredProductIds.retainAll(dmc.productById.keySet());
            dmc.ruleGroupCoveredCategories.getOrDefault(r.getRuleGroupId(), Collections.emptySet())
                    .stream().flatMap(c -> dmc.productByCatId.get(c).stream())
                    .forEach(filteredProductIds::add);
            r.setThresholdProductIds(filteredProductIds);
        });

        Map<ThresholdProductType, List<DiscountRule>> rulesByThresholdProductType = dmc.allRules.values().stream()
            .collect(groupingBy(DiscountRule::getThresholdProductType, mapping(Function.identity(), toList())));

        if (rulesByThresholdProductType.containsKey(ThresholdProductType.All)) {
            rulesByThresholdProductType.get(ThresholdProductType.All).stream()
                .collect(groupingBy(DiscountRule::getThresholdProductIds, mapping(Function.identity(), toList())))
                .forEach((s, rules) -> {
                    long selectionId = productSelection.size();
                    productSelection.put(selectionId, s);
                    selectionIdToDiscountRules.put(selectionId, rules.stream().map(DiscountRule::getId).collect(toSet()));
                });
        }

        if (rulesByThresholdProductType.containsKey(ThresholdProductType.Any)) {
            rulesByThresholdProductType.get(ThresholdProductType.Any).stream()
                .map(r -> r.getThresholdProductIds().stream().collect(groupingBy(i -> i, mapping(i -> r, toList()))))
                .reduce((m1, m2) -> {
                    m2.forEach((k, v) -> m1.computeIfAbsent(k, i -> new LinkedList<>()).addAll(v));
                    return m1;
                }).get()
                .forEach((k, v) -> {
                    long selectionId = productSelection.size();
                    productSelection.put(selectionId, Set.of(k));
                    selectionIdToDiscountRules.put(selectionId, v.stream().map(DiscountRule::getId).collect(toSet()));
                });
        }

    }

    private double resolveBestDealsByAmount(List<DiscountRule> allDeals, DealMatchContext dmc, Map<Long, Map<ThresholdType, Double>> thresholdValues, Set<Long> thresholdProducts, List<Long> result, List<Map<Long, Map<ThresholdType, Double>>> targetProducts) {
        Map<Long, Map<Long, Map<ThresholdType, Double>>> ruleDiscountDetails = new HashMap<>();
        Map<Long, Double> ruleDiscountAmount = new HashMap<>();
        for (DiscountRule r : allDeals) {
            Map<Long, Map<ThresholdType, Double>> discountDetail = new HashMap<>();
            double discountAmount = calculateDiscountAmount(r, dmc, thresholdValues, thresholdProducts, false, discountDetail);
            ruleDiscountDetails.put(r.getId(), discountDetail);
            ruleDiscountAmount.put(r.getId(), discountAmount);
        }
        Long bestDealRuleId = ruleDiscountAmount.entrySet().stream().max(Comparator.comparingDouble(Map.Entry::getValue)).get().getKey();
        result.add(bestDealRuleId);
        targetProducts.add(ruleDiscountDetails.get(bestDealRuleId));
        return ruleDiscountAmount.get(bestDealRuleId);
    }

    private double calculateDiscountAmount(DiscountRule r, DealMatchContext dmc, Map<Long, Map<ThresholdType, Double>> thresholdValues, Set<Long> thresholdProducts, boolean updateQuantity, Map<Long, Map<ThresholdType, Double>> discountValues) {
        Set<Long> applicableProducts = r.getApplicableProductType() == ApplicableProductType.Identity ?
                thresholdProducts : dmc.ruleGroupCoveredProducts.get(r.getApplicableRuleGroupId());
        if (r.getOverrideAmount() > 0) {
            if (r.getThresholdUnitType() == ThresholdType.Amount) {
                for (Long id : thresholdProducts) {
                    Map<ThresholdType, Double> discountValue = discountValues.computeIfAbsent(id, i -> new HashMap<>());
                    discountValue.merge(ThresholdType.Qty, thresholdValues.get(id).get(ThresholdType.Qty), Double::sum);
                    discountValue.merge(ThresholdType.Amount, r.getOverrideAmount(), Double::sum);

                    if (updateQuantity) {
                        thresholdValues.get(id).put(ThresholdType.Qty, 0.0);
                        thresholdValues.get(id).put(ThresholdType.Amount, 0.0);
                    }
                }
            } else {
                Map<Long, Integer> thresholdUnitDefinition = dmc.ruleGroupProductQuantity.get(r.getRuleGroupId());
                Map<Long, Double> quantities = thresholdProducts.stream().collect(toMap(Function.identity(), i -> thresholdValues.get(i).get(ThresholdType.Qty)));
                long numUnit = numberOfBasketUnit(r.getThresholdUnit(), thresholdUnitDefinition, quantities);
                if (numUnit > 0L && updateQuantity) {
                    Map<Long, Map<ThresholdType, Double>> reductionDetails = reductionDetails(numUnit * r.getThresholdUnit(), thresholdValues, thresholdUnitDefinition, thresholdProducts);
                    reductionDetails.forEach((productId, values) -> {
                        Map<ThresholdType, Double> currentValues = thresholdValues.get(productId);
                        currentValues.merge(ThresholdType.Qty, values.get(ThresholdType.Qty)*-1, Double::sum);
                        currentValues.merge(ThresholdType.Amount, values.get(ThresholdType.Amount)*-1, Double::sum);
                    });
                }
            }
            return r.getOverrideAmount();
        } else {
            Set<Long> targetProducts = r.getApplicableProductType() == ApplicableProductType.Identity ? thresholdProducts : applicableProducts;
            double applicableAmount = 0.0;

            if (r.getThresholdUnitType() == ThresholdType.Amount) {
                applicableAmount = targetProducts.stream().map(thresholdValues::get).mapToDouble(m -> m.get(ThresholdType.Amount)).sum();

                for (Long id : thresholdProducts) {
                    if (updateQuantity) {
                        thresholdValues.get(id).put(ThresholdType.Qty, 0.0);
                        thresholdValues.get(id).put(ThresholdType.Amount, 0.0);
                    }
                    Map<ThresholdType, Double> discountValue = discountValues.computeIfAbsent(id, i -> new HashMap<>());
                    discountValue.merge(ThresholdType.Qty, thresholdValues.get(id).get(ThresholdType.Qty), Double::sum);
                    discountValue.merge(ThresholdType.Amount, thresholdValues.get(id).get(ThresholdType.Amount) * r.getApplicableDiscount(), Double::sum);
                }

            } else {
                Map<Long, Integer> thresholdUnitDefinition = dmc.ruleGroupProductQuantity.get(r.getRuleGroupId());
                Map<Long, Double> quantities = thresholdProducts.stream().collect(toMap(Function.identity(), i -> thresholdValues.get(i).get(ThresholdType.Qty)));
                if (r.getApplicableUnitType() == ApplicableType.Qty) {
                    long unit = numberOfBasketUnit(r.getThresholdUnit(), thresholdUnitDefinition, quantities);
                    long actualApplyUnit = unit * r.getApplicableUnit();

                    if (actualApplyUnit > 0L) {
                        Map<Long, Integer> applyUnitDefinition = r.getApplicableProductType() == ApplicableProductType.Identity ? thresholdUnitDefinition : dmc.ruleGroupProductQuantity.get(r.getApplicableRuleGroupId());
                        applicableAmount = applicableAmountByQty(actualApplyUnit, dmc, targetProducts, r, thresholdValues, applyUnitDefinition);

                        Map<Long, Map<ThresholdType, Double>> targetDiscountDetails = reductionDetails(actualApplyUnit, thresholdValues, applyUnitDefinition, targetProducts);
                        for (Long id : targetDiscountDetails.keySet()) {
                            Map<ThresholdType, Double> targetDiscount = targetDiscountDetails.get(id);
                            Map<ThresholdType, Double> discountValue = discountValues.computeIfAbsent(id, i->new HashMap<>());
                            discountValue.merge(ThresholdType.Qty, targetDiscount.get(ThresholdType.Qty), Double::sum);
                            discountValue.merge(ThresholdType.Amount, targetDiscount.get(ThresholdType.Amount) * r.getApplicableDiscount(), Double::sum);
                        }

                        if (updateQuantity) {
                            Map<Long, Map<ThresholdType, Double>> reductionDetails = reductionDetails(unit * r.getThresholdUnit(), thresholdValues, thresholdUnitDefinition, thresholdProducts);
                            reductionDetails.forEach((productId, values) -> {
                                Map<ThresholdType, Double> currentValues = thresholdValues.get(productId);
                                currentValues.merge(ThresholdType.Qty, values.get(ThresholdType.Qty)*-1, Double::sum);
                                currentValues.merge(ThresholdType.Amount, values.get(ThresholdType.Amount)*-1, Double::sum);
                            });
                            if (r.getApplicableProductType() != ApplicableProductType.Identity) {
                                targetDiscountDetails.forEach((productId, values) -> {
                                    Map<ThresholdType, Double> currentValues = thresholdValues.get(productId);
                                    currentValues.merge(ThresholdType.Qty, values.get(ThresholdType.Qty)*-1, Double::sum);
                                    currentValues.merge(ThresholdType.Amount, values.get(ThresholdType.Amount)*-1, Double::sum);
                                });
                            }
                        }
                    }
                } else {
                    if (r.getApplicableProductType() == ApplicableProductType.Identity) {
                        long unit = numberOfBasketUnit(r.getThresholdUnit(), thresholdUnitDefinition, quantities);
                        if (unit > 0L) {
                            double actualApplicableAmount = 0.0;
                            for (Long id : quantities.keySet()) {
                                double productAmount = thresholdValues.get(id).get(ThresholdType.Amount);
                                actualApplicableAmount += productAmount * quantities.get(id) * unit / thresholdValues.get(id).get(ThresholdType.Qty);
                            }
                            applicableAmount = Math.min(r.getApplicableUnit(), actualApplicableAmount);
                            Map<Long, Map<ThresholdType, Double>> reductionDetails = reductionDetails(unit * r.getThresholdUnit(), thresholdValues, thresholdUnitDefinition, thresholdProducts);
                            for (Long id : reductionDetails.keySet()) {
                                Map<ThresholdType, Double> targetDiscount = reductionDetails.get(id);
                                Map<ThresholdType, Double> discountValue = discountValues.get(id);
                                discountValue.merge(ThresholdType.Qty, targetDiscount.get(ThresholdType.Qty), Double::sum);
                                discountValue.merge(ThresholdType.Amount, targetDiscount.get(ThresholdType.Amount), Double::sum);
                            }
                            if (updateQuantity) {
                                reductionDetails.forEach((productId, values) -> {
                                    Map<ThresholdType, Double> currentValues = thresholdValues.get(productId);
                                    currentValues.merge(ThresholdType.Qty, values.get(ThresholdType.Qty)*-1, Double::sum);
                                    currentValues.merge(ThresholdType.Amount, values.get(ThresholdType.Amount)*-1, Double::sum);
                                });
                            }
                        }
                    } else {
                        long unit = numberOfBasketUnit(r.getThresholdUnit(), thresholdUnitDefinition, quantities);
                        if (unit > 0L) {
                            double actualApplicableAmount = 0.0;
                            for (Long id : targetProducts) {
                                Map<ThresholdType, Double> targetValues = thresholdValues.get(id);

                                Map<ThresholdType, Double> discountValue = discountValues.computeIfAbsent(id, i -> new HashMap<>());
                                discountValue.merge(ThresholdType.Qty, targetValues.get(ThresholdType.Qty), Double::sum);
                                discountValue.merge(ThresholdType.Amount, targetValues.get(ThresholdType.Amount), Double::sum);

                                actualApplicableAmount += targetValues.get(ThresholdType.Amount);
                            }
                            applicableAmount = Math.min(r.getApplicableUnit(), actualApplicableAmount);

                            if (updateQuantity) {
                                Map<Long, Map<ThresholdType, Double>> reductionDetails = reductionDetails(unit * r.getThresholdUnit(), thresholdValues, thresholdUnitDefinition, thresholdProducts);
                                reductionDetails.forEach((productId, values) -> {
                                    Map<ThresholdType, Double> currentValues = thresholdValues.get(productId);
                                    currentValues.merge(ThresholdType.Qty, values.get(ThresholdType.Qty)*-1, Double::sum);
                                    currentValues.merge(ThresholdType.Amount, values.get(ThresholdType.Amount)*-1, Double::sum);
                                });
                            }
                        }
                    }
                }
            }

            return applicableAmount * r.getApplicableDiscount();
        }
    }

    private double applicableAmountByQty(long actualApplyUnit, DealMatchContext dmc, Set<Long> applyProductIds, DiscountRule rule, Map<Long, Map<ThresholdType, Double>> thresholdValues, Map<Long, Integer> applyUnitDefinition) {
        double totalApplicableAmount = 0.0;
        for (Long productId : applyProductIds) {
            long applicableUnit = applyUnitDefinition.get(productId) * actualApplyUnit;
            double totalUnit = thresholdValues.get(productId).get(ThresholdType.Qty);
            double ratio = applicableUnit / totalUnit;
            double productApplicableAmount = ratio * thresholdValues.get(productId).get(ThresholdType.Amount);
            totalApplicableAmount += productApplicableAmount;
        }
        return totalApplicableAmount;
    }


    private long numberOfBasketUnit(long basketMultiplier, Map<Long, Integer> basketQuantities, Map<Long, Double> actualQuantities) {
        long ratio = Long.MAX_VALUE;
        for (Long productId : actualQuantities.keySet()) {
            long basketQuantity = basketQuantities.get(productId) * basketMultiplier;
            long quantity = actualQuantities.get(productId).longValue();
            ratio = Math.min(quantity / basketQuantity, ratio);
            if (ratio == 0L) break;
        }
        return ratio;
    }


    private Map<Long, Map<ThresholdType, Double>> reductionDetails(long numberOfUnits, Map<Long, Map<ThresholdType,Double>> thresholdValues, Map<Long, Integer> thresholdUnitDefinition, Set<Long> productIds) {
        Map<Long, Map<ThresholdType, Double>> result = new HashMap<>();
        for (Long id : productIds) {
            Map<ThresholdType,Double> values = thresholdValues.get(id);
            double currentQty = values.get(ThresholdType.Qty);
            double currentAmount = values.get(ThresholdType.Amount);
            double quantityReduced = numberOfUnits * thresholdUnitDefinition.get(id);
            double amountReduced = currentAmount * (quantityReduced / currentQty);

            Map<ThresholdType, Double> discountValues = new HashMap<>();
            discountValues.put(ThresholdType.Qty, quantityReduced);
            discountValues.put(ThresholdType.Amount, amountReduced);
            result.put(id, discountValues);
        }
        return result;
    }

    private double resolveBestDealsByQty(List<DiscountRule> qtyTriggerDeals, DealMatchContext dmc, Map<Long, Map<ThresholdType, Double>> thresholdValues, Set<Long> thresholdProducts, List<Long> result, List<Map<Long, Map<ThresholdType, Double>>> discountDetails) {
        //assume amount based discount are exclusive to one another while quantity based are not
        double bestSavingAmount = 0.0;
        Set<Long> bestDealSelectedRules = Collections.emptySet();
        Map<Long, Map<Long, Map<ThresholdType, Double>>> bestDealDiscountDetails = Collections.emptyMap();

        Comparator<DiscountRule> comparator = Comparator.comparingDouble(r -> calculateDiscountAmount(r, dmc, thresholdValues, thresholdProducts, false, new HashMap<>()));

        List<DiscountRule> sorted = qtyTriggerDeals.stream()
                .sorted(comparator.reversed())
                .toList();

        for (int i=0; i<sorted.size(); i++){
            int j = i;

            double selectedRulesSavingAmount = 0.0;
            Set<Long> selectedRules = new HashSet<>();
            Map<Long, Map<Long, Map<ThresholdType, Double>>> selectedRulesDiscountValues = new HashMap<>();

            Map<Long, Map<ThresholdType, Double>> workingValues = new HashMap<>();
            for (Long id : thresholdValues.keySet()) {
                workingValues.put(id, new HashMap<>(thresholdValues.get(id)));
            }

            while (j < sorted.size()
                    && thresholdProducts.stream().map(workingValues::get).mapToDouble(m->m.get(ThresholdType.Qty)).sum() > 0.0
            ) {
                DiscountRule currentRule = sorted.get(j);
                if (applicable(currentRule, dmc, thresholdProducts, workingValues)) {
                    Map<Long, Map<ThresholdType, Double>> discountValues = new HashMap<>();
                    double savingAmount = calculateDiscountAmount(currentRule, dmc, workingValues, thresholdProducts, true, discountValues);
                    if (savingAmount > 0) {
                        selectedRulesSavingAmount += savingAmount;
                        selectedRules.add(currentRule.getId());
                        selectedRulesDiscountValues.put(currentRule.getId(), discountValues);
                    }
                }
                j++;
            }
            if (selectedRulesSavingAmount > bestSavingAmount) {
                bestSavingAmount = selectedRulesSavingAmount;
                bestDealSelectedRules = selectedRules;
                bestDealDiscountDetails = selectedRulesDiscountValues;
            }
        }

        for (Long ruleId : bestDealSelectedRules) {
            result.add(ruleId);
            discountDetails.add(bestDealDiscountDetails.get(ruleId));
        }
        return bestSavingAmount;
    }

    private boolean applicable(DiscountRule rule, DealMatchContext dmc, Set<Long> productIds, Map<Long, Map<ThresholdType, Double>> characteristicsByProductId) {
        double threshold = 0.0;
        double value = 0.0;

        ThresholdType thresholdType = rule.getThresholdUnitType();
        switch (thresholdType) {
            case Qty -> {
                Map<Long, Integer> ruleProductQuantity = dmc.ruleGroupProductQuantity.get(rule.getRuleGroupId());
                threshold = productIds.stream().mapToDouble(ruleProductQuantity::get).sum();
                threshold *= rule.getThresholdUnit();
            }
            case Amount -> threshold = rule.getThresholdUnit();
        }
        value = productIds.stream().map(characteristicsByProductId::get).mapToDouble(m -> m.get(thresholdType)).sum();
        return value >= threshold;
    }

}
