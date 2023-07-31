Electronic Store
---------------

1) Start the service:

    a) Using jar:
        java -jar build/libs/electronic-store-springboot-0.0.1-SNAPSHOT.jar

    b) Using bootRun:
        ./gradlew bootRun

2) Example scenario:

    Run under src/main/resources/examples, refer to src/main/resources/examples/curl.sh:

```Bash
#create product
curl -X POST -H "Content-Type:application/json" -d '@request/product_request.json' http://localhost:8080/products
``` 
```Bash
# create new shopping cart (expect id=1)
curl -X POST -H "Content-Type:application/json" -d '{}' http://localhost:8080/shoppingCarts
```
```Bash
# add items to shopping cart 1
curl -X POST -H "Content-Type:application/json" -d '@request/shopping_cart_add_product.json' http://localhost:8080/shoppingCarts/1/items
```
```Bash
# get latest shopping cart 1
curl http://localhost:8080/shoppingCarts/1
```
```Bash
# create new shopping cart with 3 items (expect id=2)
curl -X POST -H "Content-Type:application/json" -d '@request/create_shopping_cart_with_initial_items.json' http://localhost:8080/shoppingCarts
```
```Bash
# get latest shopping cart 2
curl http://localhost:8080/shoppingCarts/2
```
    
    Note: 
      * database is currently not persisted to disk
      * database schema and product categories are created on startup (schema.sql, data.sql)
    