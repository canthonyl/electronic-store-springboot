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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
    private ProductService productService;

    @Autowired
    private DealService dealService;


    public DealMatchResponse matchDeals(DealMatchRequest request) {

        Set<Long> allCategories = request.getCharacteristic().get(DiscountRuleSetting.Group.category).keySet();
        Set<Long> allProducts = request.getCharacteristic().get(DiscountRuleSetting.Group.product).keySet();
        CategoryValueMap<Long, ThresholdType> productQtyAndAmount = new CategoryValueMap<>(request.getCharacteristic().get(DiscountRuleSetting.Group.product));
        Map<Long, Map<Long, Map<ThresholdType, Double>>> itemDetailsByProductId = request.getMapToCartItemId();

        DealMatchContext dmc = new DealMatchContext();
        dmc.setProducts(productService.getProducts(allProducts));
        dmc.setAllRuleSettings(dealService.lookupRuleByCategoryOrProduct(allCategories, allProducts));
        dmc.setAllRule(dealService.lookupRuleByGroupId(dmc.ruleGroupIds));

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
            List<CategoryValueMap<Long, ThresholdType>> ruleResultDiscountDetails = Collections.emptyList();
            CategoryValueMap<Long, ThresholdType> ruleResultChangesToCart = null;

            Map<ThresholdType, List<DiscountRule>> rules = ruleIds.stream()
                    .map(dmc.allRules::get)
                    .filter(r -> applicable(r, dmc, thresholdProducts, productQtyAndAmount))
                    .collect(groupingBy(DiscountRule::getThresholdUnitType, mapping(Function.identity(), toList())));

            if (rules.size() > 0) {
                double savingAmountCandidate = 0.0;
                List<Long> bestDeals = new LinkedList<>();
                List<CategoryValueMap<Long, ThresholdType>> bestDealsTargetProducts = new LinkedList<>();
                CategoryValueMap<Long, ThresholdType> bestDealsChangesToCart = new CategoryValueMap<>();
                if (rules.containsKey(ThresholdType.Amount)) {
                    savingAmountCandidate = resolveBestDealsByAmount(rules.get(ThresholdType.Amount), dmc, productQtyAndAmount, thresholdProducts, bestDeals, bestDealsTargetProducts, bestDealsChangesToCart);
                    if (bestDeals.size() > 0) {
                        if (savingAmountCandidate > savingAmount) {
                            savingAmount = savingAmountCandidate;
                            ruleResult = bestDeals;
                            ruleResultDiscountDetails = bestDealsTargetProducts;
                            ruleResultChangesToCart = bestDealsChangesToCart;
                        }
                    }
                }

                bestDeals = new LinkedList<>();
                bestDealsTargetProducts = new LinkedList<>();
                if (rules.containsKey(ThresholdType.Qty)) {
                    savingAmountCandidate = resolveBestDealsByQty(rules.get(ThresholdType.Qty), dmc, productQtyAndAmount, thresholdProducts, bestDeals, bestDealsTargetProducts, bestDealsChangesToCart);
                    if (bestDeals.size() > 0) {
                        if (savingAmountCandidate > savingAmount) {
                            ruleResult = bestDeals;
                            ruleResultDiscountDetails = bestDealsTargetProducts;
                            ruleResultChangesToCart = bestDealsChangesToCart;
                        }
                    }
                }

                productQtyAndAmount.subtract(ruleResultChangesToCart);

                //assign deals to items
                for (int i = 0; i < ruleResult.size(); i++) {
                    Long ruleId = ruleResult.get(i);
                    DiscountRule rule = dmc.allRules.get(ruleId);
                    CategoryValueMap<Long, ThresholdType> discountDetailsByProduct = ruleResultDiscountDetails.get(i);
                    Set<Long> discountProductIds = discountDetailsByProduct.entitiesKeySet();

                    for (Long productId : discountProductIds) {
                        //Map<ThresholdType, Double> discountDetail = discountDetailsByProduct.get(productId);
                        Double discountAmount = discountDetailsByProduct.get(productId, ThresholdType.Amount).doubleValue();
                        Long totalQuantityEligibleForDiscount = discountDetailsByProduct.get(productId, ThresholdType.Qty).longValue();
                        Double discountPerItem = discountAmount / totalQuantityEligibleForDiscount;

                        Map<Long, Map<ThresholdType, Double>> relatedItems = itemDetailsByProductId.get(productId);
                        Iterator<Long> itemIdIterator = relatedItems.keySet().iterator();
                        while (totalQuantityEligibleForDiscount > 0L) {
                            Long itemId = itemIdIterator.next();
                            Map<ThresholdType, Double> relatedItemDetail = relatedItems.get(itemId);
                            double qtyReduced = Math.min(relatedItemDetail.get(ThresholdType.Qty), totalQuantityEligibleForDiscount);
                            double amountReduced = qtyReduced * discountPerItem;
                            relatedItemDetail.merge(ThresholdType.Qty, qtyReduced * -1, Double::sum);
                            relatedItemDetail.merge(ThresholdType.Amount, amountReduced * -1, Double::sum);

                            totalQuantityEligibleForDiscount -= (long) qtyReduced;
                            response.getItemIdToDeals().computeIfAbsent(itemId, id -> new LinkedList<>()).add(rule);
                            response.getItemDiscountAmount().merge(itemId, amountReduced, Double::sum);
                        }
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

    private double resolveBestDealsByAmount(List<DiscountRule> allDeals, DealMatchContext dmc, CategoryValueMap<Long, ThresholdType> productQtyAndAmount, Set<Long> thresholdProducts, List<Long> bestDealRuleIds, List<CategoryValueMap<Long, ThresholdType>> discountDetailList, CategoryValueMap<Long, ThresholdType> qtyAndAmountAllocatedForDiscount) {
        long ruleId = 0L;
        double bestSavingDiscountAmount = 0.0;
        CategoryValueMap<Long, ThresholdType> discountDetails = new CategoryValueMap<>();
        CategoryValueMap<Long, ThresholdType> changesToCart = new CategoryValueMap<>();

        for (DiscountRule r : allDeals) {
            CategoryValueMap<Long, ThresholdType> discountDetail = new CategoryValueMap<>();
            CategoryValueMap<Long, ThresholdType> workingThresholdValues = new CategoryValueMap<>(productQtyAndAmount);

            double calculatedDiscountAmount = calculateDiscountAmount(r, dmc, workingThresholdValues, thresholdProducts, true, discountDetail);

            if (calculatedDiscountAmount > bestSavingDiscountAmount) {
                ruleId = r.getId();
                bestSavingDiscountAmount = calculatedDiscountAmount;
                discountDetails = discountDetail;
                changesToCart = new CategoryValueMap<>(productQtyAndAmount);
                changesToCart.subtract(workingThresholdValues);
            }
        }

        bestDealRuleIds.add(ruleId);
        discountDetailList.add(discountDetails);
        qtyAndAmountAllocatedForDiscount.add(changesToCart);

        return bestSavingDiscountAmount;
    }

    private double resolveBestDealsByQty(List<DiscountRule> qtyTriggerDeals, DealMatchContext dmc, CategoryValueMap<Long, ThresholdType> productQtyAndAmount, Set<Long> thresholdProducts, List<Long> result, List<CategoryValueMap<Long, ThresholdType>> discountDetails, CategoryValueMap<Long, ThresholdType> resultChangesToCart) {
        //assume amount based discount are exclusive to one another while quantity based are not
        double bestSavingAmount = 0.0;
        Set<Long> bestDealSelectedRules = Collections.emptySet();
        Map<Long, CategoryValueMap<Long, ThresholdType>> bestDealDiscountDetails = Collections.emptyMap();
        CategoryValueMap<Long, ThresholdType> bestDealChangeToCart = null;

        Comparator<DiscountRule> comparator = Comparator.comparingDouble(r -> calculateDiscountAmount(r, dmc, productQtyAndAmount, thresholdProducts, false, new CategoryValueMap<>()));

        List<DiscountRule> sorted = qtyTriggerDeals.stream()
                .sorted(comparator.reversed())
                .toList();

        for (int i=0; i<sorted.size(); i++){
            int j = i;

            double selectedRulesSavingAmount = 0.0;
            Set<Long> selectedRules = new HashSet<>();
            Map<Long, CategoryValueMap<Long, ThresholdType>> selectedRulesDiscountValues = new HashMap<>();

            CategoryValueMap<Long, ThresholdType> workingValues = new CategoryValueMap<>(productQtyAndAmount);

            while (j < sorted.size() && workingValues.sum(thresholdProducts, ThresholdType.Qty).doubleValue() > 0.0) {
                DiscountRule currentRule = sorted.get(j);
                if (applicable(currentRule, dmc, thresholdProducts, workingValues)) {
                    CategoryValueMap<Long, ThresholdType> discountValues = new CategoryValueMap<>();
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
                bestDealChangeToCart = new CategoryValueMap<>(productQtyAndAmount);
                bestDealChangeToCart.subtract(workingValues);
            }
        }

        for (Long ruleId : bestDealSelectedRules) {
            result.add(ruleId);
            discountDetails.add(bestDealDiscountDetails.get(ruleId));
        }
        resultChangesToCart.add(bestDealChangeToCart);
        return bestSavingAmount;
    }

    private double calculateDiscountAmount(DiscountRule r, DealMatchContext dmc, CategoryValueMap<Long, ThresholdType> thresholdValues, Set<Long> thresholdProducts, boolean updateQuantity, CategoryValueMap<Long, ThresholdType> discountValues) {
        Long applicableRuleGroupId = r.getApplicableProductType() == ApplicableProductType.Identity ? r.getRuleGroupId() : r.getApplicableRuleGroupId();
        Set<Long> applicableProducts = r.getApplicableProductType() == ApplicableProductType.Identity ? thresholdProducts :
                dmc.ruleGroupCoveredProducts.get(r.getApplicableRuleGroupId());

        CategoryValueMap<Long, ThresholdType> thresholdUnitQty = dmc.ruleGroupProductQuantity.get(r.getRuleGroupId()).scaleTo(r.getThresholdUnit());

        CategoryValueMap<Long, ThresholdType> qtyAndAmountForThreshold = thresholdValues.collect(thresholdProducts);
        CategoryValueMap<Long, ThresholdType> qtyAndDiscountAmount = thresholdValues.collect(applicableProducts);
        CategoryValueMap<Long, ThresholdType> qtyAndOriginalAmount = thresholdValues.collect(applicableProducts);

        long units = 0L;
        if (r.getThresholdUnitType() == ThresholdType.Qty) {
            CategoryValueMap<Long, ThresholdType> qtyUnitized = qtyAndAmountForThreshold.collect(ThresholdType.Qty);
            units = qtyUnitized.unitize(thresholdUnitQty);
            qtyAndAmountForThreshold.scale(qtyUnitized, ThresholdType.Qty);
        }

        if (r.getApplicableUnitType() == ApplicableType.Qty) {
            CategoryValueMap<Long, ThresholdType> applicableUnitQty = dmc.ruleGroupProductQuantity.get(applicableRuleGroupId).collect(applicableProducts);
            applicableUnitQty.scale(units * r.getApplicableUnit());
            qtyAndDiscountAmount = qtyAndDiscountAmount.scaleTo(applicableUnitQty, ThresholdType.Qty);
            qtyAndDiscountAmount.compute(ThresholdType.Amount, n -> n.doubleValue() * r.getApplicableDiscount());
            qtyAndOriginalAmount = qtyAndOriginalAmount.scaleTo(applicableUnitQty, ThresholdType.Qty);
        } else {
            if (Optional.ofNullable(r.getOverrideAmount()).orElse(0.0) > 0) {
                double totalAmount = qtyAndDiscountAmount.sum(ThresholdType.Amount).doubleValue();
                double totalAmountApplicable = r.getOverrideAmount();
                qtyAndDiscountAmount.compute(ThresholdType.Amount, amount -> amount.doubleValue() / totalAmount * totalAmountApplicable);
            } else {
                double totalAmount = qtyAndDiscountAmount.sum(ThresholdType.Amount).doubleValue();
                double totalAmountApplicable = Math.min(r.getApplicableUnit(), totalAmount) * r.getApplicableDiscount();
                qtyAndDiscountAmount.compute(ThresholdType.Amount, amount -> amount.doubleValue() / totalAmount * totalAmountApplicable);
            }
        }

        if (updateQuantity) {
            thresholdValues.subtract(qtyAndAmountForThreshold);
            if (!r.getRuleGroupId().equals(applicableRuleGroupId)) {
                thresholdValues.subtract(qtyAndOriginalAmount);
            }
        }

        discountValues.add(qtyAndDiscountAmount);

        return qtyAndDiscountAmount.sum(ThresholdType.Amount).doubleValue();
    }

    private boolean applicable(DiscountRule rule, DealMatchContext dmc, Set<Long> productIds, CategoryValueMap<Long, ThresholdType> productValues) {
        ThresholdType thresholdType = rule.getThresholdUnitType();
        boolean result = false;
        switch (thresholdType) {
            case Qty -> {
                CategoryValueMap<Long, ThresholdType> thresholdQuantities = dmc.ruleGroupProductQuantity.get(rule.getRuleGroupId()).collect(productIds).scaleTo(rule.getThresholdUnit());
                result = productValues.greaterThanEqualTo(thresholdQuantities, ThresholdType.Qty);
            }
            case Amount -> {
                result = productValues.sum(productIds, ThresholdType.Amount).doubleValue() >= rule.getThresholdUnit();
            }
        }
        return result;
    }

    class CategoryValueMap<Entity, Dimension> {

        private final Map<Entity, Map<Dimension, Number>> valueMap = new HashMap<>();
        private final Predicate<Entity> allEntities = entity -> true;
        private final Function<Dimension, Predicate<Dimension>> specificDimensions = dim -> (d -> d.equals(dim));

        public CategoryValueMap() {}

        public CategoryValueMap(CategoryValueMap<Entity, Dimension> o) {
            o.forEach(this::put);
        }

        public <T extends Number> CategoryValueMap(Map<Entity, Map<Dimension, T>> o){
            o.forEach((entity, dimensionValueMap) -> dimensionValueMap.forEach((dimension, value) -> this.put(entity, dimension, value)));
        }

        public Number get(Entity entity, Dimension dim) {
            return Optional.ofNullable(valueMap.get(entity)).map(m -> m.get(dim)).orElse(0.0);
        }

        public void put(Entity entity, Dimension dim, Number val) {
            get(entity).put(dim, val);
        }

        public void compute(Dimension dimension, Function<Number, Number> computeFunction) {
            forEachFilteredDimensionEntry(allEntities, specificDimensions.apply(dimension), (entity, dimensionEntry) -> dimensionEntry.setValue(computeFunction.apply(dimensionEntry.getValue())));
        }

        public void compute(Collection<Entity> collection, Dimension dimension, Function<Number, Number> computeFunction) {
            forEachFilteredDimensionEntry(filterIfPresent(collection), specificDimensions.apply(dimension), (entity, dimensionEntry) -> dimensionEntry.setValue(computeFunction.apply(dimensionEntry.getValue())));
        }

        private void merge(Entity entity, Dimension dimension, Number b, BiFunction<Number, Number, Number> mergeOperation) {
            Number a = get(entity).getOrDefault(dimension, 0.0);
            Number newValue = mergeOperation.apply(a, b);
            put(entity, dimension, newValue);
        }

        public void merge(CategoryValueMap<Entity, Dimension> operand, Dimension dimension, BiFunction<Number, Number, Number> mergeOperation) {
            //operand.forEachFiltered(allEntities, specificDimensions.apply(dimension), (e, d, v) -> merge(e, d, v, mergeOperation));
            operand.forEachFilteredDimensionEntry(allEntities, dim -> dim.equals(dimension), (entity, dimEntry) -> this.merge(entity, dimension, dimEntry.getValue(), mergeOperation));
        }

        private void merge(CategoryValueMap<Entity, Dimension> operand, BiFunction<Number, Number, Number> mergeOperation) {
            operand.forEach((e, d, v) -> merge(e, d, v, mergeOperation));
        }

        public void add(Entity entity, Dimension dim, Number val) {
            get(entity).merge(dim, val, (a,b) -> a.doubleValue() + b.doubleValue());
        }

        public void add(CategoryValueMap<Entity, Dimension> operand) {
            merge(operand, (a,b) -> a.doubleValue() + b.doubleValue());
        }

        public void add(CategoryValueMap<Entity, Dimension> operand, Dimension dimension) {
            merge(operand, dimension, (a,b) -> a.doubleValue() + b.doubleValue());
        }

        public void subtract(CategoryValueMap<Entity, Dimension> operand) {
            merge(operand, (a,b) -> a.doubleValue() - b.doubleValue());
        }

        public void subtract(CategoryValueMap<Entity, Dimension> operand, Dimension dimension) {
            merge(operand, dimension, (a,b) -> a.doubleValue() - b.doubleValue());
        }

        public CategoryValueMap<Entity, Dimension> scaleTo(Number operand) {
            CategoryValueMap<Entity, Dimension> result = new CategoryValueMap<>();
            forEach((entity, dimension, value) -> result.put(entity, dimension, value.doubleValue() * operand.doubleValue()));
            return result;
        }

        public CategoryValueMap<Entity, Dimension> scaleTo(CategoryValueMap<Entity, Dimension> scaleMap, Dimension scalingDimension) {
            CategoryValueMap<Entity, Dimension> result = new CategoryValueMap<>();
            forEach((entity, dimension, value) -> {
                double factor = scaleMap.get(entity, scalingDimension).doubleValue() / this.get(entity, scalingDimension).doubleValue();
                double newValue = value.doubleValue() * factor;
                result.put(entity, dimension, newValue);
            });
            return result;
        }

        public void scale(CategoryValueMap<Entity, Dimension> scaleMap, Dimension scalingDimension) {
            forEachDimensionEntry((entity, dimensionEntry) -> {
                double factor = scaleMap.get(entity, scalingDimension).doubleValue() / this.get(entity, scalingDimension).doubleValue();
                dimensionEntry.setValue(dimensionEntry.getValue().doubleValue() * factor);
            });
        }

        public void scale(Number operand) {
            forEachValue((entity, dimension, value) -> value.doubleValue() * operand.doubleValue());
        }

        //quantized values in all dimensions to multiples of unit definition, returning number of units
        public long unitize(CategoryValueMap<Entity, Dimension> unitDefinition, Dimension... dimensions) {
            long numUnits = zipStream(this, unitDefinition, (baseValue, joinValue) -> new ZipEntryValue(baseValue, joinValue))
                    .filter(zipEntry -> filterIfPresent(Set.of(dimensions)).test(zipEntry.dimension))
                    .map(ZipEntry::getValues)
                    .mapToLong(ZipEntryValue::divideAsLong)
                    .min()
                    .orElse(0L);

            forEachFilteredValue(allEntities, filterIfPresent(Set.of(dimensions)), (entity, dimension, value) -> numUnits * unitDefinition.get(entity, dimension).doubleValue());
            return numUnits;
        }

        public Set<Entity> entitiesKeySet() {
            return valueMap.keySet();
        }

        public CategoryValueMap<Entity, Dimension> collect(Collection<Entity> entities, Dimension... categories) {
            CategoryValueMap<Entity, Dimension> result = new CategoryValueMap<>();
            //forEachFiltered(entities::contains, filterIfPresent(Set.of(categories)), result::put);
            forEachFilteredDimensionEntry(entities::contains, filterIfPresent(Set.of(categories)), (entity, dimEntry) -> {
                result.put(entity, dimEntry.getKey(), dimEntry.getValue());
            });
            return result;
        }

        public CategoryValueMap<Entity, Dimension> collect(Dimension... categories) {
            CategoryValueMap<Entity, Dimension> result = new CategoryValueMap<>();
            //forEachFiltered(allEntities, filterIfPresent(Set.of(categories)), result::put);
            forEachFilteredDimensionEntry(allEntities, filterIfPresent(Set.of(categories)), (entity, dimEntry) -> {
                result.put(entity, dimEntry.getKey(), dimEntry.getValue());
            });
            return result;
        }

        public void forEach(ValueConsumer<Entity, Dimension> consumer) {
            valueMap.forEach((entity, dimMap) -> dimMap.forEach((dim, val) -> consumer.accept(entity, dim, val)));
        }

        public void forEachValue(ValueMapper<Entity, Dimension> mapper) {
            forEachDimensionEntry((entity, dimensionEntry) -> dimensionEntry.setValue(mapper.accept(entity, dimensionEntry.getKey(), dimensionEntry.getValue())));
        }

        public void forEachFilteredValue(Predicate<Dimension> dimensionPredicate, ValueMapper<Entity, Dimension> mapper) {
            forEachFilteredDimensionEntry(allEntities, dimensionPredicate, (entity, dimensionEntry) -> dimensionEntry.setValue(mapper.accept(entity, dimensionEntry.getKey(), dimensionEntry.getValue())));
        }

        public void forEachFilteredValue(Predicate<Entity> entityPredicate, Predicate<Dimension> dimensionPredicate, ValueMapper<Entity, Dimension> mapper) {
            forEachFilteredDimensionEntry(entityPredicate, dimensionPredicate, (entity, dimensionEntry) -> dimensionEntry.setValue(mapper.accept(entity, dimensionEntry.getKey(), dimensionEntry.getValue())));
        }

        public Number sum(Dimension dim) {
            return sum(allEntities, dim);
        }

        public Number sum(Predicate<Entity> predicate, Dimension dim) {
            return valueMap.entrySet().stream().filter(e -> predicate.test(e.getKey()))
                    .map(e -> e.getValue().get(dim))
                    .reduce(CategoryValueMap::addAsDouble)
                    .orElse(0.0);
        }

        public Number sum(Collection<Entity> filter, Dimension dim) {
            return filter.stream()
                    .map(e -> valueMap.get(e))
                    .filter(Objects::nonNull)
                    .map(m -> m.get(dim))
                    .reduce(CategoryValueMap::addAsDouble)
                    .orElse(0.0);
        }

        public boolean greaterThanEqualTo(CategoryValueMap<Entity, Dimension> other, Dimension dim) {
            return zipStream(other, this, (baseValue, joinValue) -> new ZipEntryValue(joinValue, baseValue))
                    .filter(zipEntry -> zipEntry.dimension.equals(dim))
                    .map(ZipEntry::getValues)
                    .allMatch(ZipEntryValue::greaterThanOrEqual);
        }

        private Map<Dimension, Number> get(Entity entity) {
            return valueMap.computeIfAbsent(entity, e -> new HashMap<>());
        }

        private void forEachDimensionEntry(DimensionEntryConsumer<Entity, Dimension> consumer) {
            valueMap.forEach((e, m) -> m.entrySet().forEach(entry -> consumer.accept(e, entry)));
        }

        private void forEachFilteredDimensionEntry(Predicate<Entity> entityPredicate, Predicate<Dimension> dimensionPredicate, DimensionEntryConsumer<Entity, Dimension> consumer) {
            valueMap.entrySet().stream()
                    .filter(entityEntry -> entityPredicate == allEntities || entityPredicate.test(entityEntry.getKey()))
                    .forEach(entityEntry ->
                            entityEntry.getValue().entrySet().stream()
                                    .filter(dimensionEntry -> dimensionPredicate.test(dimensionEntry.getKey()))
                                    .forEach(dimensionEntry -> consumer.accept(entityEntry.getKey(), dimensionEntry)));
        }

        /*private void forEachFiltered(Predicate<Entity> entityPredicate, Predicate<Dimension> dimensionPredicate, ValueConsumer<Entity, Dimension> consumer) {
            forEachFilteredDimensionEntry(entityPredicate, dimensionPredicate, (entity, dimensionEntry) -> consumer.accept(entity, dimensionEntry.getKey(), dimensionEntry.getValue()));
        }*/

        private <T> Predicate<T> filterIfPresent(Collection<T> c) {
            return t -> c.size() == 0 || c.contains(t);
        }

        private Stream<ZipEntry> zipStream(CategoryValueMap<Entity, Dimension> base, CategoryValueMap<Entity, Dimension> join, BiFunction<Number, Number, ZipEntryValue> valueOrder) {
            return base.valueMap.entrySet()
                    .stream()
                    .mapMulti((entityEntry, consumer) ->
                            entityEntry.getValue().entrySet()
                                    .stream()
                                    .map(dimensionEntry -> {
                                        Entity entity = entityEntry.getKey();
                                        Dimension dimension = dimensionEntry.getKey();
                                        Number baseStreamValue = dimensionEntry.getValue();
                                        Number joinStreamValue = join.get(entity, dimension);
                                        return new ZipEntry(entity, dimension, valueOrder.apply(baseStreamValue, joinStreamValue));})
                                    .forEach(consumer));
        }

        @FunctionalInterface
        interface ValueConsumer<Entity, Dimension> {
            void accept(Entity entity, Dimension dimension, Number value);
        }

        @FunctionalInterface
        interface ValueMapper<Entity, Dimension> {
            Number accept(Entity entity, Dimension dimension, Number value);
        }

        @FunctionalInterface
        private interface DimensionEntryConsumer<Entity, Dimension> {
            void accept(Entity entity, Map.Entry<Dimension, Number> dimensionEntry);
        }


        private class ZipEntry {
            final Entity entity;
            final Dimension dimension;
            final ZipEntryValue values;

            ZipEntry(Entity e, Dimension d, ZipEntryValue v) {
                entity = e;
                dimension = d;
                values = v;
            }

            ZipEntryValue getValues(){
                return values;
            }
        }


        private class ZipEntryValue {
            final Number val1;
            final Number val2;

            ZipEntryValue(Number v1, Number v2) {
                val1 = v1;
                val2 = v2;
            }

            boolean greaterThanOrEqual() {
                return val1.doubleValue() >= val2.doubleValue();
            }

            double sum(){
                return val1.doubleValue() + val2.doubleValue();
            }

            long divideAsLong() {
                return val1.longValue() / val2.longValue();
            }
        }

        private static Number addAsDouble(Number n1, Number n2) {
            return n1.doubleValue() + n2.doubleValue();
        }
    }

    class DealMatchContext {

        List<DiscountRuleSetting> allRuleSettings;
        Map<Long, Set<Long>> ruleGroupCoveredCategories;
        Map<Long, Set<Long>> ruleGroupCoveredProducts;
        Map<Long, CategoryValueMap<Long, ThresholdType>> ruleGroupProductQuantity;
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
                CategoryValueMap<Long, ThresholdType> ruleQuantities = ruleGroupProductQuantity.computeIfAbsent(ruleSetting.getRuleGroupId(), r -> new CategoryValueMap<>());
                if (ruleSetting.getProductId() != null && ruleSetting.getQuantity() != null) {
                    ruleQuantities.put(ruleSetting.getProductId(), ThresholdType.Qty, ruleSetting.getQuantity());
                }
                if (ruleSetting.getCategoryId() != null && ruleSetting.getQuantity() != null) {
                    productByCatId.get(ruleSetting.getCategoryId()).forEach(p -> {
                        ruleQuantities.put(p, ThresholdType.Qty, ruleSetting.getQuantity());
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

}
