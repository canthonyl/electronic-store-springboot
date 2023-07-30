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
      | 4  | iPad Pro            | Apple iPad Pro             | 8000.00  | 3           |
      | 5  | Bluetooth Mouse     | Logitech bluetooth mouse   | 567.00   | 3           |
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
              "product" : {"id" : 1},
              "quantity" : 2
            },
            {
              "product" : {"id" : 3},
              "quantity" : 2
            },
            {
              "product" : {"id" : 5},
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
            "totalAmountBeforeDiscount": 24490.0,
            "totalDiscountAmount" : 5783.5,
            "totalAmount" : 18706.5,
            "items": [
              {
                "id": 1,
                "product": {
                  "id": 1,
                  "name": null,
                  "description": null,
                  "price": null,
                  "categoryId": null
                },
                "quantity": 2,
                "price": 11000.0,
                "amountBeforeDiscount": 22000.0,
                "discountAmount": 5500.0,
                "amount": 16500.0,
                "discountApplied": ["Buy 1 get 50% off the second"]
              },
              {
                "id": 2,
                "product": {
                  "id": 3,
                  "name": null,
                  "description": null,
                  "price": null,
                  "categoryId": null
                },
                "quantity": 2,
                "price": 678.0,
                "amountBeforeDiscount": 1356.0,
                "discountAmount": 0.0,
                "amount": 1356.0,
                "discountApplied": null
              },
              {
                "id": 3,
                "product": {
                  "id": 5,
                  "name": null,
                  "description": null,
                  "price": null,
                  "categoryId": null
                },
                "quantity": 2,
                "price": 567.0,
                "amountBeforeDiscount": 1134.0,
                "discountAmount": 283.5,
                "amount": 850.5,
                "discountApplied": ["Buy 1 get 50% off the second"]
              }
            ]
          }
        }
      """

