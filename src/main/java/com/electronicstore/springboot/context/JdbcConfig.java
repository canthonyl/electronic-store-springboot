package com.electronicstore.springboot.context;

import com.electronicstore.springboot.dao.EntityDatastore;
import com.electronicstore.springboot.dao.jdbc.EntityJdbcRepository;
import com.electronicstore.springboot.dao.jdbc.JdbcTableMetadata;
import com.electronicstore.springboot.model.DiscountRule;
import com.electronicstore.springboot.model.DiscountRuleSetting;
import com.electronicstore.springboot.model.Product;
import com.electronicstore.springboot.model.ProductCategory;
import com.electronicstore.springboot.model.ShoppingCart;
import com.electronicstore.springboot.model.ShoppingCartItem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("data.jdbc")
@Configuration
public class JdbcConfig {

    @Bean
    public EntityJdbcRepository<Product> productJdbcRepo() {
        JdbcTableMetadata.Builder<Product> builder = new JdbcTableMetadata.Builder<>();

        JdbcTableMetadata<Product> tableMetaData = builder
                .tableName("product")
                .keyColumn("id", Product::setId, Product::getId, Long.class)
                .column("name", Product::setName, Product::getName, String.class)
                .column("description", Product::setDescription, Product::getDescription, String.class)
                .column("category_id", Product::setCategoryId, Product::getCategoryId, Long.class)
                .column("price", Product::setPrice, Product::getPrice, Double.class)
                .rowMapperInstance(Product::new)
                .build();

        return new EntityJdbcRepository<>(tableMetaData);
    }

    @Bean
    public EntityJdbcRepository<ProductCategory> productCategoryJdbcRepo() {
        JdbcTableMetadata.Builder<ProductCategory> builder = new JdbcTableMetadata.Builder<>();

        JdbcTableMetadata<ProductCategory> tableMetaData = builder
                .tableName("product_category")
                .keyColumn("id", ProductCategory::setId, ProductCategory::getId, Long.class)
                .column("name", ProductCategory::setName, ProductCategory::getName, String.class)
                .rowMapperInstance(ProductCategory::new)
                .build();

        return new EntityJdbcRepository<>(tableMetaData);
    }

    @Bean
    public EntityJdbcRepository<ShoppingCart> shoppingCartJdbcRepo() {
        JdbcTableMetadata.Builder<ShoppingCart> builder = new JdbcTableMetadata.Builder<>();

        JdbcTableMetadata<ShoppingCart> tableMetaData = builder
                .tableName("shopping_cart")
                .keyColumn("id", ShoppingCart::setId, ShoppingCart::getId, Long.class)
                .rowMapperInstance(ShoppingCart::new)
                .build();

        return new EntityJdbcRepository<>(tableMetaData);
    }

    @Bean
    public EntityJdbcRepository<ShoppingCartItem> shoppingCartItemJdbcRepo() {
        JdbcTableMetadata.Builder<ShoppingCartItem> builder = new JdbcTableMetadata.Builder<>();

        JdbcTableMetadata<ShoppingCartItem> tableMetaData = builder
                .tableName("shopping_cart_item")
                .keyColumn("id", ShoppingCartItem::setId, ShoppingCartItem::getId, Long.class)
                .column("shopping_cart_id", ShoppingCartItem::setShoppingCartId, ShoppingCartItem::getShoppingCartId, Long.class)
                .column("product_id", ShoppingCartItem::setProductId, ShoppingCartItem::getProductId, Long.class)
                .column("quantity", ShoppingCartItem::setQuantity, ShoppingCartItem::getQuantity, Long.class)
                .column("price", ShoppingCartItem::setPrice, ShoppingCartItem::getPrice, Double.class)
                .column("amount_before_discount", ShoppingCartItem::setAmountBeforeDiscount, ShoppingCartItem::getAmountBeforeDiscount, Double.class)
                .column("discount_amount", ShoppingCartItem::setDiscountAmount, ShoppingCartItem::getDiscountAmount, Double.class)
                .column("amount", ShoppingCartItem::setAmount, ShoppingCartItem::getAmount, Double.class)
                .rowMapperInstance(ShoppingCartItem::new)
                .build();

        return new EntityJdbcRepository<>(tableMetaData);
    }

    @Bean
    public EntityJdbcRepository<DiscountRule> discountRuleJdbcRepository() {
        JdbcTableMetadata.Builder<DiscountRule> builder = new JdbcTableMetadata.Builder<>();

        JdbcTableMetadata<DiscountRule> tableMetaData = builder
                .tableName("discount_rule")
                .keyColumn("id", DiscountRule::setId, DiscountRule::getId, Long.class)
                .column("threshold_unit", DiscountRule::setThresholdUnit, DiscountRule::getThresholdUnit, Long.class)
                .column("threshold_unit_type", DiscountRule::setThresholdUnitType, DiscountRule::getThresholdUnitType, DiscountRule.ThresholdType.class)
                .column("applicable_unit", DiscountRule::setApplicableUnit, DiscountRule::getApplicableUnit, Long.class)
                .column("applicable_unit_type", DiscountRule::setApplicableUnitType, DiscountRule::getApplicableUnitType, DiscountRule.ApplicableType.class)
                .column("applicable_discount", DiscountRule::setApplicableDiscount, DiscountRule::getApplicableDiscount, Double.class)
                .column("override_amount", DiscountRule::setOverrideAmount, DiscountRule::getOverrideAmount, Double.class)
                .column("threshold_product_type", DiscountRule::setThresholdProductType, DiscountRule::getThresholdProductType, DiscountRule.ThresholdProductType.class)
                .column("rule_group_id", DiscountRule::setRuleGroupId, DiscountRule::getRuleGroupId, Long.class)
                .column("applicable_product_type", DiscountRule::setApplicableProductType, DiscountRule::getApplicableProductType, DiscountRule.ApplicableProductType.class)
                .column("applicable_rule_group_id", DiscountRule::setApplicableRuleGroupId, DiscountRule::getApplicableRuleGroupId, Long.class)
                .column("description", DiscountRule::setDescription, DiscountRule::getDescription, String.class)
                .rowMapperInstance(DiscountRule::new)
                .build();

        return new EntityJdbcRepository<>(tableMetaData);
    }

    @Bean
    public EntityJdbcRepository<DiscountRuleSetting> discountRuleSettingJdbcRepository() {
        JdbcTableMetadata.Builder<DiscountRuleSetting> builder = new JdbcTableMetadata.Builder<>();

        JdbcTableMetadata<DiscountRuleSetting> tableMetaData = builder
                .tableName("discount_rule_setting")
                .keyColumn("id", DiscountRuleSetting::setId, DiscountRuleSetting::getId, Long.class)
                .column("category_id", DiscountRuleSetting::setCategoryId, DiscountRuleSetting::getCategoryId, Long.class)
                .column("product_id", DiscountRuleSetting::setProductId, DiscountRuleSetting::getProductId, Long.class)
                .column("rule_group_id", DiscountRuleSetting::setRuleGroupId, DiscountRuleSetting::getRuleGroupId, Long.class)
                .column("quantity", DiscountRuleSetting::setQuantity, DiscountRuleSetting::getQuantity, Integer.class)
                .column("setting", DiscountRuleSetting::setSetting, DiscountRuleSetting::getSetting, String.class)
                .rowMapperInstance(DiscountRuleSetting::new)
                .build();

        return new EntityJdbcRepository<>(tableMetaData);
    }

    @Bean
    public EntityDatastore<Product> productDatastore() {
        return new EntityDatastore<>(productJdbcRepo());
    }

    @Bean
    public EntityDatastore<ProductCategory> productCategoryDatastore() {
        return new EntityDatastore<>(productCategoryJdbcRepo());
    }

    @Bean
    public EntityDatastore<ShoppingCart> shoppingCartDatastore() {
        return new EntityDatastore<>(shoppingCartJdbcRepo());
    }

    @Bean
    public EntityDatastore<ShoppingCartItem> shoppingCartItemDatastore() {
        return new EntityDatastore<>(shoppingCartItemJdbcRepo());
    }

    @Bean
    public EntityDatastore<DiscountRule> discountRuleDatastore() {
        return new EntityDatastore<>(discountRuleJdbcRepository());
    }

    @Bean
    public EntityDatastore<DiscountRuleSetting> discountRuleSettingDatastore() {
        return new EntityDatastore<>(discountRuleSettingJdbcRepository());
    }
}
