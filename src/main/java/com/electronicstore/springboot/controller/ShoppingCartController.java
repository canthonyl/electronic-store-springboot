package com.electronicstore.springboot.controller;

import com.electronicstore.springboot.context.CommonContext;
import com.electronicstore.springboot.dto.ShoppingCartRequest;
import com.electronicstore.springboot.dto.ShoppingCartResponse;
import com.electronicstore.springboot.model.ShoppingCart;
import com.electronicstore.springboot.model.ShoppingCartItem;
import com.electronicstore.springboot.service.ShoppingCartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Optional;

import static com.electronicstore.springboot.dto.ShoppingCartRequest.ResponseType.ShoppingCart;

@RestController
@RequestMapping("/shoppingCarts")
public class ShoppingCartController {

    @Autowired
    private CommonContext appContext;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @GetMapping("{cartId}")
    public ResponseEntity<ShoppingCart> getShoppingCart(@PathVariable Long cartId) {
        return shoppingCartService.getShoppingCart(cartId)
                .map(s -> {
                    shoppingCartService.refreshShoppingCart(s);
                    return ResponseEntity.ok(s);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("{cartId}/items/{itemId}")
    public ResponseEntity<ShoppingCartItem> getShoppingCartItem(@PathVariable(name="cartId") Long cartId, @PathVariable(name="itemId") Long itemId) {
        if (!shoppingCartService.shoppingCartExists(cartId)) {
            return ResponseEntity.notFound().build();
        }
        return shoppingCartService.getShoppingCartItem(cartId, itemId)
                .map(s -> ResponseEntity.ok(s))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ShoppingCartResponse> createShoppingCart(@RequestBody ShoppingCartRequest request) {
        ShoppingCart shoppingCart = shoppingCartService.createShoppingCart(new ShoppingCart(request.getShoppingCartItems()));

        Long id = shoppingCart.getId();

        URI uri = appContext.getBaseUriBuilder()
                .pathSegment("shoppingCarts", "{id}")
                .build(id);

        if (request.getResponseType() == ShoppingCart) {
            shoppingCartService.refreshShoppingCart(shoppingCart);
            return ResponseEntity.created(uri).body(new ShoppingCartResponse(shoppingCart));
        } else {
            return ResponseEntity.created(uri).build();
        }
    }

    @PostMapping("{cartId}/items")
    public ResponseEntity<ShoppingCartResponse> addShoppingCartItems(@PathVariable(name="cartId") Long cartId, @Valid @RequestBody ShoppingCartRequest request) {
        if (!shoppingCartService.shoppingCartExists(cartId)) {
            return ResponseEntity.notFound().build();
        }
        shoppingCartService.addShoppingCartItems(cartId, request.getShoppingCartItems());
        if (request.getResponseType() == ShoppingCart) {
            //replace with a refresh method
            ShoppingCart s = shoppingCartService.getShoppingCart(cartId).get();
            shoppingCartService.refreshShoppingCart(s);
            ShoppingCartResponse body = new ShoppingCartResponse(s);
            return ResponseEntity.ok().body(body);
        } else {
            return ResponseEntity.ok().build();
        }
    }

    @PutMapping("{cartId}/items/{itemId}")
    public ResponseEntity<Void> updateShoppingCartItem(@PathVariable(name="cartId") Long cartId, @PathVariable(name="itemId") Long itemId, @RequestParam(name="quantity") int quantity) {
        Optional<ShoppingCartItem> item = shoppingCartService.getShoppingCartItem(cartId, itemId);
        if (item.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ShoppingCartItem itemInstance = item.get();
        itemInstance.setQuantity(quantity);
        shoppingCartService.updateShoppingCartItems(itemInstance);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(path="{cartId}/items/{itemId}", consumes={MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> updateShoppingCartItemWithJson(@PathVariable(name="cartId") Long cartId, @PathVariable(name="itemId") Long itemId, @Valid @RequestBody ShoppingCartItem requestItem) {
        Optional<ShoppingCartItem> item = shoppingCartService.getShoppingCartItem(cartId, itemId);
        if (item.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ShoppingCartItem itemInstance = item.get();
        itemInstance.setQuantity(requestItem.getQuantity());
        shoppingCartService.updateShoppingCartItems(itemInstance);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("{cartId}")
    public ResponseEntity<Void> deleteShoppingCart(@PathVariable(name="cartId") Long cartId) {
        if (shoppingCartService.removeShoppingCart(cartId).isPresent()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("{cartId}/items/{itemId}")
    public ResponseEntity<Void> deleteShoppingCartItem(@PathVariable(name="cartId") Long cartId, @PathVariable(name="itemId") Long itemId) {
        if (shoppingCartService.removeShoppingCartItem(cartId, itemId).isPresent()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}

//TODO: each request should be associated with a valid session?
//TODO: apply header attribute at controller level?
//TODO: ensure non editable fields are ignored