Feature: Products can be added and removed to shopping cart

  Background:
    Given the following products persisted
      | id | name                | description                | price |
      | 1  | Macbook pro         | Apple Macbook pro          | 11000 |
      | 2  | Dell Desktop        | Dell Desktop i5            | 5000  |
      | 3  | Mechanical Keyboard | Mechanical Keyboard Clicky | 116   |

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
              "product" : {"id" : 1},
              "quantity" : 1
            },
            {
              "product" : {"id" : 2},
              "quantity" : 1
            },
            {
              "product" : {"id" : 3},
              "quantity" : 1
            }
          ]
        }
      """
    Then http status ACCEPTED is received
    #And the following response body
    #  """json
    #    {
#
 #       }
  #    """


