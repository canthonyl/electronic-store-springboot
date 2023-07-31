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
      | 3  | Mechanical Keyboard | Mechanical Keyboard Clicky | 1678.00   | 3           |
      | 4  | iPad Pro            | Apple iPad Pro             | 8000.00  | 3           |
      | 5  | Bluetooth Mouse     | Logitech bluetooth mouse   | 1598.00   | 3           |
    And the following discount rules
      | Id | Description                  | Threshold Unit Type | Threshold Unit | Applicable Unit Type | Applicable Unit | Applicable Discount | Override Amount | Rule Group Id |
      | 1  | Buy 1 get 50% off the second | Qty                 | 2              | Qty                  | 1               | 0.5                 | 0.0             | 1             |
      | 2  | Buy 3 get 60% off all 3      | Qty                 | 3              | Qty                  | 3               | 0.6                 | 0.0             | 1             |
      | 3  | Buy 5 get 70% off all 5      | Qty                 | 5              | Qty                  | 5               | 0.7                 | 0.0             | 1             |
    And the following rule settings
      | Id |  Category Id | Product Id | Rule Group Id |
      | 1  |  1           |            | 1             |
      | 2  |  2           |            | 1             |
      | 3  |              | 5          | 1             |
      | 4  |              | 4          | 1             |

  Scenario: Products are added to shopping cart and latest discount and total amount evaluated
    When a POST request is sent to "/shoppingCarts"
    Then http status CREATED is received
    And "Location" in Http Header contains the following values
      | http://localhost:8090/shoppingCarts/1 |
    When a POST request is sent to "/shoppingCarts/1/items" with body
      """json
        {
          "responseType": "ShoppingCart",
          "shoppingCartItems" : [
            {
              "productId" : 1,
              "quantity" : 2
            },
            {
              "productId" : 3,
              "quantity" : 2
            },
            {
              "productId" : 5,
              "quantity" : 2
            }
          ]
        }
      """
    Then http status ACCEPTED is received
    And the following response body
      """json
        {
          "shoppingCart": {
            "id": 1,
            "totalAmountBeforeDiscount": 28552.0,
            "totalDiscountAmount" : 6299.0,
            "totalAmount" : 22253.0,
            "items": [
              {
                "id": 1,
                "productId": 1,
                "quantity": 2,
                "price": 11000.0,
                "amountBeforeDiscount": 22000.0,
                "discountAmount": 5500.0,
                "amount": 16500.0,
                "discountApplied": ["Buy 1 get 50% off the second"]
              },
              {
                "id": 2,
                "productId": 3,
                "quantity": 2,
                "price": 1678.0,
                "amountBeforeDiscount": 3356.0,
                "discountAmount": 0.0,
                "amount": 3356.0,
                "discountApplied": null
              },
              {
                "id": 3,
                "productId": 5,
                "quantity": 2,
                "price": 1598.0,
                "amountBeforeDiscount": 3196.0,
                "discountAmount": 799.0,
                "amount": 2397.0,
                "discountApplied": ["Buy 1 get 50% off the second"]
              }
            ]
          }
        }
      """

