Feature: Production
  Making sure production works.

  Scenario: Factory produces item
    Given an empty world
    And a player "player" at position 0, 3
    And a "factory" at 0, 0 owned by player "player" called "factory"
    When player "player" is ordered to interact with "factory"
    And waiting until player "player" has no order
    And waiting 10 seconds
    And player "player" is ordered to interact with "factory"
    And waiting until player "player" has no order
    Then player "player" should have at least 1 of item "Axe"
