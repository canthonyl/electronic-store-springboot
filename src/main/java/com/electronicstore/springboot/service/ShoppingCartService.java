package com.electronicstore.springboot.service;

import com.electronicstore.springboot.dao.ProductRepository;
import com.electronicstore.springboot.dao.ShoppingCartItemRepository;
import com.electronicstore.springboot.dao.ShoppingCartRepository;
import com.electronicstore.springboot.dto.DealMatchRequest;
import com.electronicstore.springboot.dto.DealMatchResponse;
import com.electronicstore.springboot.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

@Service
public class ShoppingCartService {

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private ShoppingCartItemRepository shoppingCartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private DealService dealService;

    //TODO ensure latest price is used when check out
    //TODO ensure transaction is not committed if latest price is not used

    public boolean shoppingCartExists(Long id) {
        return shoppingCartRepository.existsById(id);
    }

    private DealMatchResponse refreshDeals(Map<Long, Product> products, Map<Long, List<ShoppingCartItem>> itemsByProduct) {
        DealMatchRequest request = new DealMatchRequest();
        for (Map.Entry<Long, List<ShoppingCartItem>> e: itemsByProduct.entrySet()) {
            List<ShoppingCartItem> cartItems = e.getValue();
            Product product = products.get(e.getKey());
            for (ShoppingCartItem i : cartItems) {
                i.setPrice(product.getPrice());
                i.setAmountBeforeDiscount(i.getQuantity() * product.getPrice());
                i.setAmount(i.getQuantity() * product.getPrice());
                request.addCharacteristic(product, DiscountRule.ThresholdType.Qty, i.getQuantity());
                request.addCharacteristic(product, DiscountRule.ThresholdType.Amount, i.getAmountBeforeDiscount());
            }
            request.addMapping(product, cartItems.stream().map(ShoppingCartItem::getId).toList());
        }
        return dealService.matchDeals(request);
    }

    public void refreshShoppingCart(ShoppingCart cart) {
        Map<Long, Product> products = productRepository.findAllById(cart.getItems().stream().map(ShoppingCartItem::getProductId).toList())
                .stream().collect(toMap(Product::getId, Function.identity()));

        Map<Long, List<ShoppingCartItem>> itemsByProduct = cart.getItems().stream().collect(groupingBy(ShoppingCartItem::getProductId,
                toList()));

        System.out.println("shoppingCartItems = "+cart.getItems());
        System.out.println("shoppingCartItemsByProduct = "+itemsByProduct);

        DealMatchResponse response = refreshDeals(products, itemsByProduct);
        Map<Long, Double> itemDiscountAmount = response.getItemsDiscountAmount();
        Map<Long, List<DiscountRule>> itemDiscountRuleApplied = response.getItemIdToDeals();
        Map<Long, ShoppingCartItem> cartItems = cart.getItems().stream().collect(toMap(ShoppingCartItem::getId, Function.identity()));

        for (Long itemId : itemDiscountAmount.keySet()) {
            ShoppingCartItem cartItem = cartItems.get(itemId);
            cartItem.setDiscountAmount(itemDiscountAmount.get(itemId));
            cartItem.setDiscountApplied(itemDiscountRuleApplied.get(itemId)
                    .stream().map(DiscountRule::getDescription).toList());
        }

        double totalAmount = 0;
        double totalDiscount = 0;
        double totalAmountBeforeDiscount = 0;

        List<ShoppingCartItem> removedItems = new LinkedList<>();

        for (Map.Entry<Long, List<ShoppingCartItem>> e : itemsByProduct.entrySet()) {
            Optional<Product> product = productRepository.findById(e.getKey());
            if (product.isPresent()){
                Product p = product.get();
                List<ShoppingCartItem> itemList = e.getValue();
                for (ShoppingCartItem i : itemList) {
                   i.setPrice(p.getPrice());

                   double amountBeforeDiscount = i.getQuantity() * i.getPrice();
                   double discountAmount = i.getDiscountAmount();
                   double amount = amountBeforeDiscount - discountAmount;
                   i.setAmountBeforeDiscount(amountBeforeDiscount);
                   i.setDiscountAmount(discountAmount);
                   i.setAmount(amount);

                   totalAmountBeforeDiscount += amountBeforeDiscount;
                   totalDiscount += discountAmount;
                   totalAmount += amount;
                }
            } else {
                removedItems.addAll(e.getValue());
            }
        }
        cart.setTotalAmountBeforeDiscount(totalAmountBeforeDiscount);
        cart.setTotalDiscountAmount(totalDiscount);
        cart.setTotalAmount(totalAmount);
    }

    public boolean shoppingCartItemExists(Long cartId, Long itemId) {
        return getShoppingCartItem(cartId, itemId).isPresent();
    }

    public Optional<ShoppingCart> getShoppingCart(Long id) {
        Optional<ShoppingCart> shoppingCart = shoppingCartRepository.findById(id);
        if (shoppingCart.isPresent()) {
            refreshShoppingCart(shoppingCart.get());
        }
        return shoppingCart;
    }

    public ShoppingCart createShoppingCart(ShoppingCart shoppingCart) {
        ShoppingCart cart = shoppingCartRepository.save(shoppingCart);
        return cart;
    }

    public Optional<ShoppingCartItem> getShoppingCartItem(Long cartId, Long itemId) {
        return getShoppingCart(cartId)
                .flatMap(s -> s.getItems().stream().filter(i -> i.getId().equals(itemId)).findFirst());
    }

    public Optional<ShoppingCart> replaceShoppingCart(Long cartId, ShoppingCart cart) {
        return Optional.empty();
    }

    public Optional<ShoppingCart> updateShoppingCart(Long cartId, ShoppingCart cart) {
        return Optional.empty();
    }

    public void addShoppingCartItems(Long cartId, List<ShoppingCartItem> items) {
        ShoppingCart cart = new ShoppingCart(cartId);
        items.forEach(i -> i.setShoppingCart(cart));
        shoppingCartItemRepository.saveAll(items);
    }

    public void updateShoppingCartItems(Long cartId, Long itemId, ShoppingCartItem ShoppingCartItem) {

    }
}
