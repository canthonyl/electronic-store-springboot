package com.electronicstore.springboot.service;

import com.electronicstore.springboot.concurrent.LRUCache;
import com.electronicstore.springboot.dao.EntityDatastore;
import com.electronicstore.springboot.dto.DealMatchRequest;
import com.electronicstore.springboot.dto.DealMatchResponse;
import com.electronicstore.springboot.model.DiscountRule;
import com.electronicstore.springboot.model.Product;
import com.electronicstore.springboot.model.ShoppingCart;
import com.electronicstore.springboot.model.ShoppingCartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
public class ShoppingCartService {

    @Autowired
    private EntityDatastore<ShoppingCart> shoppingCartDatastore;

    @Autowired
    private EntityDatastore<ShoppingCartItem> shoppingCartItemDatastore;

    @Autowired
    private ProductService productService;

    @Autowired
    private DealMatchService dealMatchService;

    private final LRUCache shoppingCartCache;

    private final Queue<ShoppingCart> evictedQueue;

    public ShoppingCartService(){
        evictedQueue = new ConcurrentLinkedQueue<>();
        shoppingCartCache = new LRUCache(100, evictedQueue);
    }

    //TODO ensure latest price is used when check out
    //TODO ensure transaction is not committed if latest price is not used

    public boolean shoppingCartExists(Long id) {
        return shoppingCartDatastore.contains(id);
    }

    public boolean shoppingCartItemExists(Long cartId, Long itemId) {
        return getShoppingCartItem(cartId, itemId).isPresent();
    }

    public ShoppingCart createShoppingCart(ShoppingCart shoppingCart) {
        ShoppingCart resultCart = shoppingCartDatastore.persist(shoppingCart).get();
        shoppingCart.getItems().forEach(i -> i.setShoppingCartId(resultCart.getId()));
        List<ShoppingCartItem> resultItems = shoppingCartItemDatastore.persist(shoppingCart.getItems());
        resultCart.setItems(resultItems);
        return resultCart;
    }

    public Optional<ShoppingCart> getShoppingCart(Long id) {
        Optional<ShoppingCart> shoppingCart = shoppingCartDatastore.find(id);
        shoppingCart.ifPresent(s -> {
            List<ShoppingCartItem> items = shoppingCartItemDatastore.findMatching(ShoppingCartItem.ofShoppingCart(id));
            s.setItems(items);
        });
        return shoppingCart;
    }


    public Optional<ShoppingCartItem> getShoppingCartItem(Long cartId, Long itemId) {
        ShoppingCartItem item = new ShoppingCartItem();
        item.setShoppingCartId(cartId);
        item.setId(itemId);
        return shoppingCartItemDatastore.findMatching(item).stream().findFirst();

    }

    public Optional<ShoppingCart> replaceShoppingCart(Long cartId, ShoppingCart cart) {
        return Optional.empty();
    }

    public Optional<ShoppingCart> updateShoppingCart(Long cartId, ShoppingCart cart) {
        return Optional.empty();
    }

    private void updateFromRepoToCache(Long id) {
        shoppingCartDatastore.find(id).ifPresent(c -> {
                refreshShoppingCart(c);
                shoppingCartCache.put(id, c);
            });
    }

    public void addShoppingCartItems(Long cartId, List<ShoppingCartItem> items) {
        items.forEach(i -> i.setShoppingCartId(cartId));
        shoppingCartItemDatastore.persist(items);
    }

    public void updateShoppingCartItems(ShoppingCartItem item) {
        //refreshShoppingCartItem(item);
        shoppingCartItemDatastore.persist(item);
    }

    public LRUCache getShoppingCartCache() {
        return shoppingCartCache;
    }

    public Queue<ShoppingCart> getEvictedQueue() {
        return evictedQueue;
    }

    public void persistAll() {
        shoppingCartDatastore.persist(shoppingCartCache.values());
    }

    public Optional<ShoppingCart> removeShoppingCart(Long cartId) {
        Optional<ShoppingCart> result = shoppingCartDatastore.remove(cartId);
        result.ifPresent(r -> {
           shoppingCartItemDatastore.remove(r.getItems().stream().map(ShoppingCartItem::getId).toList());
        });
        return result;
    }

    public Optional<ShoppingCartItem> removeShoppingCartItem(Long cartId, Long itemId) {
        Optional<ShoppingCartItem> item = getShoppingCartItem(cartId, itemId);
        if (item.isPresent()) {
            shoppingCartItemDatastore.remove(itemId);
        }
        return item;
    }

    private void refreshShoppingCartItem(ShoppingCartItem item) {
        Double amountBeforeDiscount = item.getQuantity() * item.getPrice();
        Double amount = amountBeforeDiscount - item.getDiscountAmount();
        item.setAmountBeforeDiscount(amountBeforeDiscount);
        item.setAmount(amount);
    }

    private DealMatchResponse refreshDeals(Map<Long, Product> products, Map<Long, List<ShoppingCartItem>> itemsByProduct) {
        DealMatchRequest request = new DealMatchRequest();
        for (Map.Entry<Long, List<ShoppingCartItem>> e: itemsByProduct.entrySet()) {
            List<ShoppingCartItem> cartItems = e.getValue();
            Product product = products.get(e.getKey());
            for (ShoppingCartItem i : cartItems) {
                i.setPrice(product.getPrice());
                i.setAmountBeforeDiscount(i.getQuantity() * product.getPrice());
                i.setDiscountAmount(0.0);
                i.setAmount(i.getQuantity() * product.getPrice());
                request.addCharacteristic(product, DiscountRule.ThresholdType.Qty, i.getQuantity(), i.getId());
                request.addCharacteristic(product, DiscountRule.ThresholdType.Amount, i.getAmountBeforeDiscount(), i.getId());
            }
        }
        return dealMatchService.matchDeals(request);
    }

    public void refreshShoppingCart(ShoppingCart cart) {
        System.out.println("refreshShoppingCart...");
        Map<Long, Product> products = productService.getProducts(cart.getItems().stream().map(ShoppingCartItem::getProductId).toList())
                .stream().collect(toMap(Product::getId, Function.identity()));

        Map<Long, List<ShoppingCartItem>> itemsByProduct = cart.getItems().stream().collect(groupingBy(ShoppingCartItem::getProductId,
                toList()));

        DealMatchResponse response = refreshDeals(products, itemsByProduct);
        Map<Long, Double> itemDiscountAmount = response.getItemDiscountAmount();
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
            Optional<Product> product = productService.getProduct(e.getKey());
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

}
