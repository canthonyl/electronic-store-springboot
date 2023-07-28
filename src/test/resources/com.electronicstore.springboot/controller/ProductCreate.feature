Feature: Create and view products via products endpoint

  Background:
    Given the following product categories
      | id | name                |
      | 1  | Desktop computer    |
      | 2  | Laptop computer     |
      | 3  | Computer peripheral |

  Scenario: Add new products by post method to /products endpoint
    Given the following products
      | Name                | Description                | Price | Category Id |
      | Macbook pro         | Apple Macbook pro          | 11000 | 2           |
      | Dell Desktop        | Dell Desktop i5            | 5000  | 1           |
      | Mechanical Keyboard | Mechanical Keyboard Clicky | 116   | 3           |
    When a POST request containing products is sent to "/products"
    Then http status ACCEPTED is received
    And "Location" in Http Header contains the following values
      | http://localhost:8090/products/1 |
      | http://localhost:8090/products/2 |
      | http://localhost:8090/products/3 |
    And the following response body
      """json
        {
          "products": [
            { "id":1, "categoryId": 2, "name":"Macbook pro", "description":"Apple Macbook pro", "price": 11000.0},
            { "id":2, "categoryId": 1, "name":"Dell Desktop", "description":"Dell Desktop i5", "price": 5000.0},
            { "id":3, "categoryId": 3, "name":"Mechanical Keyboard", "description":"Mechanical Keyboard Clicky", "price": 116.0}
          ]
        }
      """
    When a GET request is sent to "/products/1"
    Then http status OK is received
    And the following response body
      """json
      { "id":1, "categoryId":2, "name":"Macbook pro", "description":"Apple Macbook pro","price": 11000.0}
      """
    When a GET request is sent to "/products/2"
    Then http status OK is received
    And the following response body
      """json
      { "id":2, "categoryId":1, "name":"Dell Desktop", "description":"Dell Desktop i5", "price": 5000.0}
      """
    When a GET request is sent to "/products/3"
    Then http status OK is received
    And the following response body
      """json
      { "id":3, "categoryId":3, "name":"Mechanical Keyboard", "description":"Mechanical Keyboard Clicky", "price": 116.0}
      """


