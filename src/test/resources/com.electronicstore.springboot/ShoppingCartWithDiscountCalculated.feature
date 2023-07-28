Feature: Shopping cart with items have applicable discount applied to total

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
      | Id | Description                    | Threshold Unit Type | Threshold Unit | Applicable Unit Type | Applicable Unit | Applicable Discount | Override Amount | Rule Group Id |
      | 1  | "Buy 1 get 50% off the second" | Qty                 | 2              | Qty                  | 1               | 0.5                 | 0.0             | 1             |
      | 2  | "Buy 3 get 60% off all three"  | Qty                 | 3              | Qty                  | 3               | 0.6                 | 0.0             | 1             |
    And the following rule settings
      | Id | Rule Id | Category Id | Product Id | Rule Group Id |
      | 1  | 1       | 1           |            | 1             |
      | 2  | 1       | 2           |            | 1             |
      | 3  | 1       |             | 5          | 1             |
      | 4  | 2       |             | 4          | 1             |

  Scenario: User added items with quantity discount to shopping cart
    Given an empty shopping cart with id 1 is created
    When the following items are added to the shopping cart id 1
      | Product Id | Quantity |
      | 1          | 2        |
      | 3          | 2        |
      | 5          | 2        |
    Then shopping cart id 1 is refreshed with the following items
      | Id | Cart Item Id | Product Id | Quantity | Price | Amount Before Discount | Discount Amount | Amount |
      | 1  | 1            | 1          | 2        | 11000 | 22000                  | 5500            | 16500  |
      | 2  | 1            | 3          | 2        | 678   | 1356                   | 0               | 1356   |
      | 3  | 1            | 5          | 2        | 567   | 1134                   | 283.5           | 850.5  |
    And shopping cart id 1 has the following total amounts
      | Field                        | Value   |
      | Total Amount Before Discount | 24490   |
      | Total Discount Amount        | 5783.5  |
      | Total Amount                 | 18706.5 |

  Scenario: A single discount rule is replaced with multiple if it leads to lower cost
    Given a shopping cart with id 1 is created with the following items
      | Product Id | Quantity |
      | 1          | 2        |
      | 3          | 2        |
      | 5          | 2        |
    When shopping cart id 1 is refreshed
    Then shopping cart id 1 contains the following items
      | Id | Cart Item Id | Product Id | Quantity | Price | Amount Before Discount | Discount Amount | Amount |
      | 1  | 1            | 1          | 2        | 11000 | 22000                  | 5500            | 16500  |
      | 2  | 1            | 3          | 2        | 678   | 1356                   | 0               | 1356   |
      | 3  | 1            | 5          | 2        | 567   | 1134                   | 283.5           | 850.5  |
    And shopping cart id 1 has the following total amounts
      | Field                        | Value   |
      | Total Amount Before Discount | 24490   |
      | Total Discount Amount        | 5783.5  |
      | Total Amount                 | 18706.5 |
