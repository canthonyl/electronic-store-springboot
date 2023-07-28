package com.electronicstore.springboot.controller;

import com.electronicstore.springboot.context.CommonContext;
import com.electronicstore.springboot.dto.ShoppingCartRequest;
import com.electronicstore.springboot.dto.ShoppingCartResponse;
import com.electronicstore.springboot.model.ShoppingCart;
import com.electronicstore.springboot.model.ShoppingCartItem;
import com.electronicstore.springboot.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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
                .map(s -> ResponseEntity.ok(s))
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
    public ResponseEntity<ShoppingCartResponse> createShoppingCart(/*@RequestBody ShoppingCartRequest request*/) {
        //List<ShoppingCartItem> initialItems = Optional.ofNullable(request.getShoppingCartItems()).orElse(Collections.emptyList());
        ShoppingCart shoppingCart = shoppingCartService.createShoppingCart();
        URI uri = appContext.getBaseUriBuilder()
                .pathSegment("shoppingCarts", "{id}")
                .buildAndExpand(shoppingCart.getId())
                .toUri();
        return ResponseEntity.created(uri).body(new ShoppingCartResponse(shoppingCart));
    }

    @PostMapping("{cartId}/items")
    public ResponseEntity<ShoppingCartResponse> addShoppingCartItems(@PathVariable(name="cartId") Long cartId, @RequestBody ShoppingCartRequest request) {
        if (!shoppingCartService.shoppingCartExists(cartId)) {
            return ResponseEntity.notFound().build();
        }
        shoppingCartService.addShoppingCartItems(cartId, request.getShoppingCartItems());
        if (request.getResponseType() == ShoppingCart) {
            return ResponseEntity.accepted().body(new ShoppingCartResponse(shoppingCartService.getShoppingCart(cartId).get()));
        } else {
            return ResponseEntity.accepted().build();
        }
    }

    @PutMapping("{cartId}")
    public ResponseEntity<ShoppingCartResponse> replaceShoppingCart(@PathVariable Long cartId, @RequestBody ShoppingCartRequest request) {
        if (!shoppingCartService.shoppingCartExists(cartId)) {
            return ResponseEntity.notFound().build();
        }
        //shoppingCartService.replaceShoppingCart(cartId, request.getShoppingCart());
        if (request.getResponseType() == ShoppingCart) {
            return ResponseEntity.accepted().body(new ShoppingCartResponse(shoppingCartService.getShoppingCart(cartId).get()));
        } else {
            return ResponseEntity.accepted().build();
        }
    }

    /*@PatchMapping("{cartId}")
    public ResponseEntity<ShoppingCartResponse> updateShoppingCart(@PathVariable Long cartId, @RequestBody ShoppingCartRequest request) {
        if (!shoppingCartService.shoppingCartExists(cartId)) {
            return ResponseEntity.notFound().build();
        }
        return shoppingCartService.updateShoppingCart(cartId, request.getShoppingCart())
                .map(s -> ResponseEntity.ok().body(new ShoppingCartResponse(s)))
                .get();
    }*/

    /*@PutMapping("{cartId}/items/{itemId}")
    public ResponseEntity<ShoppingCartResponse> updateShoppingCartItem(@PathVariable(name="cartId") Long cartId, @PathVariable(name="itemId") Long itemId, @RequestBody ShoppingCartRequest request) {
        if (!shoppingCartService.shoppingCartItemExists(cartId, itemId)) {
            return ResponseEntity.notFound().build();
        }
        //shoppingCartService.updateShoppingCartItems(cartId, itemId, request.getSingleItemRequest());
        if (request.getResponseType() == ShoppingCart) {
            return ResponseEntity.accepted().body(new ShoppingCartResponse(shoppingCartService.getShoppingCart(cartId).get()));
        } else {
            return ResponseEntity.accepted().build();
        }
    }*/


}
