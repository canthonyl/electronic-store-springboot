Feature: Free products offered in bundled deal incur zero cost

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
      | 4  | iPad Pro            | Apple iPad Pro             | 7000.00  | 3           |
      | 5  | Bluetooth Mouse     | Logitech bluetooth mouse   | 1598.00  | 3           |

  Scenario: A bundled deal offering free item result in that item's cost reduced to zero in shopping cart
    Given the following rule settings
      | Id | Category Id | Product Id | Rule Group Id | Quantity | Description                    |
      | 1  |             | 2          | 1             | 1        | Desktop Bundled                |
      | 2  |             | 5          | 2             | 1        | Mouse                          |
      | 3  |             | 1          | 3             | 1        | Macbook Pro + iPad Pro Bundled |
      | 4  |             | 4          | 3             | 1        | Macbook Pro + iPad Pro Bundled |
    And the following discount rules
      | Id | Description                                 | Threshold Unit Type | Threshold Unit | Applicable Unit Type | Applicable Unit | Applicable Discount | Override Amount | Threshold Product Type | Rule Group Id | Applicable Product Type | Applicable Rule Group Id |
      | 1  | Free Mouse for every Dell Desktop           | Qty                 | 1              | Qty                  | 1               | 1.0                 | 0.0             | All                    | 1             | All                     | 2                        |
      | 2  | Free Mouse for every Macbook Pro + iPad Pro | Qty                 | 1              | Qty                  | 1               | 1.0                 | 0.0             | All                    | 3             | All                     | 2                        |
    When an empty shopping cart with id 1 is created
    And the following items are added to the shopping cart id 1
      | Product Id | Quantity |
      | 1          | 1        |
      | 2          | 1        |
      | 4          | 1        |
      | 5          | 2        |
    Then shopping cart id 1 is refreshed with the following items
      | Id | Cart Id | Product Id | Quantity | Price | Amount Before Discount | Discount Amount | Amount | Discount Applied                                                                 |
      | 1  | 1       | 1          | 1        | 11000 | 11000                  | 0               | 11000  |                                                                                  |
      | 2  | 1       | 2          | 1        | 5000  | 5000                   | 0               | 5000   |                                                                                  |
      | 3  | 1       | 4          | 1        | 7000  | 7000                   | 0               | 7000   |                                                                                  |
      | 4  | 1       | 5          | 2        | 1598  | 3196                   | 3196            | 0      | [Free Mouse for every Dell Desktop, Free Mouse for every Macbook Pro + iPad Pro] |
    And shopping cart id 1 has the following total amounts
      | Field                        | Value |
      | Total Amount Before Discount | 26196 |
      | Total Discount Amount        | 3196  |
      | Total Amount                 | 23000 |

  Scenario: Only eligible quantities of items are offered for free in Bundled deal
    Given the following rule settings
      | Id | Category Id | Product Id | Rule Group Id | Quantity | Description                    |
      | 1  |             | 2          | 1             | 1        | Desktop Bundled                |
      | 2  |             | 5          | 2             | 1        | Mouse                          |
      | 3  |             | 1          | 3             | 1        | Macbook Pro + iPad Pro Bundled |
      | 4  |             | 4          | 3             | 1        | Macbook Pro + iPad Pro Bundled |
    And the following discount rules
      | Id | Description                                 | Threshold Unit Type | Threshold Unit | Applicable Unit Type | Applicable Unit | Applicable Discount | Override Amount | Threshold Product Type | Rule Group Id | Applicable Product Type | Applicable Rule Group Id |
      | 1  | Free Mouse for every Dell Desktop           | Qty                 | 1              | Qty                  | 1               | 1.0                 | 0.0             | All                    | 1             | All                     | 2                        |
      | 2  | Free Mouse for every Macbook Pro + iPad Pro | Qty                 | 1              | Qty                  | 1               | 1.0                 | 0.0             | All                    | 3             | All                     | 2                        |
    When an empty shopping cart with id 1 is created
    And the following items are added to the shopping cart id 1
      | Product Id | Quantity |
      | 1          | 2        |
      | 4          | 3        |
      | 5          | 4        |
    Then shopping cart id 1 is refreshed with the following items
      | Id | Cart Id | Product Id | Quantity | Price | Amount Before Discount | Discount Amount | Amount | Discount Applied                            |
      | 1  | 1       | 1          | 2        | 11000 | 22000                  | 0               | 22000  |                                             |
      | 2  | 1       | 4          | 3        | 7000  | 21000                  | 0               | 21000  |                                             |
      | 3  | 1       | 5          | 4        | 1598  | 6392                   | 3196            | 3196   | Free Mouse for every Macbook Pro + iPad Pro |
    And shopping cart id 1 has the following total amounts
      | Field                        | Value |
      | Total Amount Before Discount | 49392 |
      | Total Discount Amount        | 3196  |
      | Total Amount                 | 46196 |


  Scenario: A better deal requiring additional products is applied when the missing items are added to cart

