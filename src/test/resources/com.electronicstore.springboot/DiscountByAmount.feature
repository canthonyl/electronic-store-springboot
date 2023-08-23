Feature: Bulk Discount applied to shopping cart based on amount

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

  Scenario: Discount rule closest to purchase amount applied
    Given the following discount rules
      | Id | Description       | Rule Group Id | Threshold Unit Type | Threshold Unit | Applicable Unit Type | Applicable Unit | Applicable Discount | Override Amount | Threshold Product Type | Applicable Product Type |
      | 1  | 10000 get 10% off | 1             | Amount              | 10000          | Amount               | 10999           | 0.1                 | 0.0             | Any                    | Identity                |
      | 2  | 11000 get 20% off | 1             | Amount              | 11000          | Amount               | 11999           | 0.2                 | 0.0             | Any                    | Identity                |
      | 3  | 12000 get 30% off | 1             | Amount              | 12000          | Amount               | 100000          | 0.3                 | 0.0             | Any                    | Identity                |
      | 4  | 5000 get 10% off  | 2             | Amount              | 5000           | Amount               | 100000          | 0.1                 | 0.0             | Any                    | Identity                |
    And the following rule settings
      | Id | Category Id | Product Id | Rule Group Id | Quantity |
      | 1  |             | 3          | 1             | 1        |
      | 2  |             | 4          | 1             | 1        |
      | 3  |             | 5          | 2             | 1        |
    When a shopping cart with id 1 is created with the following items
      | Product Id | Quantity |
      | 3          | 6        |
      | 4          | 2        |
      | 5          | 4        |
    Then shopping cart id 1 is refreshed with the following items
      | Id | Cart Id | Product Id | Quantity | Price | Amount Before Discount | Discount Amount | Amount | Discount Applied  |
      | 1  | 1       | 3          | 6        | 1678  | 10068                  | 1006.8          | 9061.2 | 10000 get 10% off |
      | 2  | 1       | 4          | 2        | 8000  | 16000                  | 4800            | 11200  | 12000 get 30% off |
      | 3  | 1       | 5          | 4        | 1598  | 6392                   | 639.2           | 5752.8 | 5000 get 10% off  |
    And shopping cart id 1 has the following total amounts
      | Field                        | Value |
      | Total Amount Before Discount | 32460 |
      | Total Discount Amount        | 6446  |
      | Total Amount                 | 26014 |


  Scenario: Discount rule closest to purchase amount applied (item splitted)
    Given the following discount rules
      | Id | Description       | Rule Group Id | Threshold Unit Type | Threshold Unit | Applicable Unit Type | Applicable Unit | Applicable Discount | Override Amount | Threshold Product Type | Applicable Product Type |
      | 1  | 10000 get 10% off | 1             | Amount              | 10000          | Amount               | 10999           | 0.1                 | 0.0             | Any                    | Identity                |
      | 2  | 11000 get 20% off | 1             | Amount              | 11000          | Amount               | 11999           | 0.2                 | 0.0             | Any                    | Identity                |
      | 3  | 12000 get 30% off | 1             | Amount              | 12000          | Amount               | 100000          | 0.3                 | 0.0             | Any                    | Identity                |
      | 4  | 5000 get 10% off  | 2             | Amount              | 5000           | Amount               | 100000          | 0.1                 | 0.0             | Any                    | Identity                |
    And the following rule settings
      | Id | Category Id | Product Id | Rule Group Id | Quantity |
      | 1  |             | 3          | 1             | 1        |
      | 2  |             | 4          | 1             | 1        |
      | 3  |             | 5          | 2             | 1        |
    When a shopping cart with id 1 is created with the following items
      | Product Id | Quantity |
      | 3          | 3        |
      | 3          | 3        |
      | 4          | 2        |
      | 5          | 4        |
    Then shopping cart id 1 is refreshed with the following items
      | Id | Cart Id | Product Id | Quantity | Price | Amount Before Discount | Discount Amount | Amount | Discount Applied  |
      | 1  | 1       | 3          | 3        | 1678  | 5034                   | 503.4           | 4530.6 | 10000 get 10% off |
      | 2  | 1       | 3          | 3        | 1678  | 5034                   | 503.4           | 4530.6 | 10000 get 10% off |
      | 3  | 1       | 4          | 2        | 8000  | 16000                  | 4800            | 11200  | 12000 get 30% off |
      | 4  | 1       | 5          | 4        | 1598  | 6392                   | 639.2           | 5752.8 | 5000 get 10% off  |
    And shopping cart id 1 has the following total amounts
      | Field                        | Value |
      | Total Amount Before Discount | 32460 |
      | Total Discount Amount        | 6446  |
      | Total Amount                 | 26014 |
