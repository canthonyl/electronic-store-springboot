
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



