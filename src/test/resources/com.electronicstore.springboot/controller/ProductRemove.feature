Feature: Product can be removed using /products endpoint

  Background:
    Given the following product categories
      | id | name                |
      | 1  | Desktop computer    |
      | 2  | Laptop computer     |
      | 3  | Computer peripheral |
    And the following products persisted
      | id | name                | description                | price | Category Id|
      | 1  | Macbook pro         | Apple Macbook pro          | 11000 | 2          |
      | 2  | Dell Desktop        | Dell Desktop i5            | 5000  | 1          |
      | 3  | Mechanical Keyboard | Mechanical Keyboard Clicky | 116   | 3          |

  Scenario: Product can be removed by DELETE request to /products/id
    When a DELETE request is sent to "/products/1"
    Then http status ACCEPTED is received
    When a DELETE request is sent to "/products/2"
    Then http status ACCEPTED is received
    When a DELETE request is sent to "/products/3"
    Then http status ACCEPTED is received
    When a GET request is sent to "/products/1"
    Then http status NOT_FOUND is received
    When a GET request is sent to "/products/2"
    Then http status NOT_FOUND is received
    When a GET request is sent to "/products/3"
    Then http status NOT_FOUND is received

  Scenario: Return not found status when non existent product is requested for deletion
    When a DELETE request is sent to "/products/1"
    Then http status ACCEPTED is received
    When a GET request is sent to "/products/1"
    Then http status NOT_FOUND is received
    When a DELETE request is sent to "/products/1"
    Then http status NOT_FOUND is received
