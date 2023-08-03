
#add products to store
curl -X POST -H "Content-Type:application/json" -d '@request/product_request.json' http://localhost:8080/products

#create shopping cart
curl -X POST -H "Content-Type:application/json" -d '{}' http://localhost:8080/shoppingCarts

#create shopping cart with initial items
curl -X POST -H "Content-Type:application/json" -d '@request/create_shopping_cart_with_initial_items.json' http://localhost:8080/shoppingCarts

#add items to shopping cart
curl -X POST -H "Content-Type:application/json" -d '@request/shopping_cart_add_product.json' http://localhost:8080/shoppingCarts/1/items

#get latest shopping cart
curl http://localhost:8080/shoppingCarts/1

#update item quantity with json
curl -X PUT -H "Content-Type:application/json" -d '{"productId":1,"quantity":2}' http://localhost:8080/shoppingCarts/1/items/1
curl -X PUT -H "Content-Type:application/json" -d '{"productId":3,"quantity":2}' http://localhost:8080/shoppingCarts/1/items/2
curl -X PUT -H "Content-Type:application/json" -d '{"productId":5,"quantity":2}' http://localhost:8080/shoppingCarts/1/items/3

