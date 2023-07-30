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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.electronicstore.springboot.dto.ShoppingCartRequest.ResponseType.ShoppingCart;

//TODO: each request should be associated with a valid session?
//TODO: apply header attribute at controller level?
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

    //TODO change request type to Shopping Cart Request
    @PostMapping
    public ResponseEntity<ShoppingCartResponse> createShoppingCart(@RequestBody ShoppingCartRequest request) {
        List<ShoppingCartItem> initialItems = Optional.ofNullable(request.getShoppingCartItems()).orElse(Collections.emptyList());
        ShoppingCart shoppingCart = new ShoppingCart(initialItems);
        shoppingCartService.createShoppingCart(shoppingCart);
        URI uri = appContext.getBaseUriBuilder()
                .pathSegment("shoppingCarts", "{id}")
                .buildAndExpand(shoppingCart.getId())
                .toUri();
        return ResponseEntity.created(uri)
                //.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new ShoppingCartResponse(shoppingCart));
    }

    //TODO: apply shopping cart ID to items in this context
    @PostMapping("{cartId}/items")
    public ResponseEntity<ShoppingCartResponse> addShoppingCartItems(@PathVariable(name="cartId") Long cartId, @RequestBody ShoppingCartRequest request) {
        if (!shoppingCartService.shoppingCartExists(cartId)) {
            return ResponseEntity.notFound().build();
        }
        shoppingCartService.addShoppingCartItems(cartId, request.getShoppingCartItems());
        if (request.getResponseType() == ShoppingCart) {
            //replace with a refresh method
            ShoppingCart s = shoppingCartService.getShoppingCart(cartId).get();
            ShoppingCartResponse body = new ShoppingCartResponse(s);
            return ResponseEntity.accepted().body(body);
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
