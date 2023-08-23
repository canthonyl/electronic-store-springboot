Feature: Shopping cart with items eligible for multi-tiered bulk discount have appropriate tiers of discounts applied

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
      | 3  | Mechanical Keyboard | Mechanical Keyboard Clicky | 1678.00  | 3           |
      | 4  | iPad Pro            | Apple iPad Pro             | 8000.00  | 3           |
      | 5  | Bluetooth Mouse     | Logitech bluetooth mouse   | 1598.00  | 3           |
    And the following discount rules
      | Id | Description                  | Threshold Unit Type | Threshold Unit | Applicable Unit Type | Applicable Unit | Applicable Discount | Override Amount | Threshold Product Type | Rule Group Id | Applicable Product Type |
      | 1  | Buy 1 get 50% off the second | Qty                 | 2              | Qty                  | 1               | 0.5                 | 0.0             | Any                    | 1             | Identity                |
      | 2  | Buy 3 get 60% off all 3      | Qty                 | 3              | Qty                  | 3               | 0.6                 | 0.0             | Any                    | 1             | Identity                |
      | 3  | Buy 5 get 70% off all 5      | Qty                 | 5              | Qty                  | 5               | 0.7                 | 0.0             | Any                    | 1             | Identity                |

  Scenario: Bulk discount by quantities offering best saving given purchase quantity applied to shopping carts
    Given the following rule settings
      | Id | Category Id | Product Id | Rule Group Id | Quantity |
      | 1  | 1           |            | 1             | 1        |
      | 2  | 2           |            | 1             | 1        |
      | 3  |             | 5          | 1             | 1        |
      | 4  |             | 4          | 1             | 1        |
    And an empty shopping cart with id 1 is created
    When the following items are added to the shopping cart id 1
      | Product Id | Quantity |
      | 1          | 2        |
      | 3          | 2        |
      | 5          | 2        |
    Then shopping cart id 1 is refreshed with the following items
      | Id | Cart Id | Product Id | Quantity | Price | Amount Before Discount | Discount Amount | Amount | Discount Applied             |
      | 1  | 1       | 1          | 2        | 11000 | 22000                  | 5500            | 16500  | Buy 1 get 50% off the second |
      | 2  | 1       | 3          | 2        | 1678  | 3356                   | 0               | 3356   |                              |
      | 3  | 1       | 5          | 2        | 1598  | 3196                   | 799             | 2397   | Buy 1 get 50% off the second |
    And shopping cart id 1 has the following total amounts
      | Field                        | Value |
      | Total Amount Before Discount | 28552 |
      | Total Discount Amount        | 6299  |
      | Total Amount                 | 22253 |

  Scenario: Multiple lower quantity deals applied if it leads to bigger savings
    Given the following rule settings
      | Id | Category Id | Product Id | Rule Group Id | Quantity |
      | 1  | 1           |            | 1             | 1        |
      | 2  | 2           |            | 1             | 1        |
      | 3  |             | 5          | 1             | 1        |
      | 4  |             | 3          | 1             | 1        |
    And a shopping cart with id 1 is created with the following items
      | Product Id | Quantity |
      | 5          | 6        |
      | 3          | 5        |
    When shopping cart id 1 is refreshed
    Then shopping cart id 1 contains the following items
      | Id | Cart Id | Product Id | Quantity | Price | Amount Before Discount | Discount Amount | Amount | Discount Applied        |
      | 1  | 1       | 5          | 6        | 1598  | 9588                   | 5752.8          | 3835.2 | Buy 3 get 60% off all 3 |
      | 2  | 1       | 3          | 5        | 1678  | 8390                   | 5873            | 2517   | Buy 5 get 70% off all 5 |
    And shopping cart id 1 has the following total amounts
      | Field                        | Value   |
      | Total Amount Before Discount | 17978   |
      | Total Discount Amount        | 11625.8 |
      | Total Amount                 | 6352.2  |
