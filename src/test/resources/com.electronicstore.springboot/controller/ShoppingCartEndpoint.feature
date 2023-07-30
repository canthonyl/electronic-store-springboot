Feature: Products can be added and removed to shopping cart

  Background:
    Given the following product categories
      | Id | Name                |
      | 1  | Desktop computer    |
      | 2  | Laptop computer     |
      | 3  | Computer peripheral |
    And the following products in the electronic store
      | Id | Name                | Description                | Price    | Category Id |
      | 1  | Macbook pro         | Apple Macbook pro          | 11000.00 | 2           |
      | 2  | Dell Desktop        | Dell Desktop i5            | 5000.00  | 1           |
      | 3  | Mechanical Keyboard | Mechanical Keyboard Clicky | 678.00   | 3           |

  Scenario: Products are added to shopping cart and latest discount and total amount evaluated
    When a POST request is sent to "/shoppingCarts"
    Then http status CREATED is received
    And "Location" in Http Header contains the following values
      | http://localhost:8090/shoppingCarts/1 |
    When a POST request is sent to "/shoppingCarts/1/items" with body
      """json
        {
          "shoppingCartItems" : [
            {
              "shoppingCartId" : 1,
              "product" : {"id" : 1},
              "quantity" : 1
            },
            {
              "shoppingCartId" : 1,
              "product" : {"id" : 2},
              "quantity" : 1
            },
            {
              "shoppingCartId" : 1,
              "product" : {"id" : 3},
              "quantity" : 1
            }
          ]
        }
      """
    Then http status ACCEPTED is received


