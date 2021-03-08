Feature: Movement
  Making sure movement cancellation when stuck etc works.

  Scenario: Slides around objects to reach target position
    Given an empty world
    And a player "player" at position 0, 2
    And a "rock" at 0.2, 0
    When player "player" is ordered to move to 0, -2
    And waiting until player "player" has no order
    Then player "player" should have reached position 0, -2

  Scenario: Move order is cancelled when player can't reach the target position
    Given an empty world
    And a player "player" at position 0, 2
    And a "rock" at 0.2, 0
    And a "rock" at -0.2, 0
    When player "player" is ordered to move to 0, -2
    And waiting until player "player" has no order
    Then player "player" should not have reached position 0, -2

  Scenario: Interact order is cancelled when player can't reach the entity
    Given an empty world
    And a player "player" at position 0, 2
    And player "player" has 1 of item "Axe"
    And a "rock" at 0.2, 0
    And a "rock" at -0.2, 0
    And a "tree" at 0, -2 called "tree"
    When player "player" is ordered to interact with "tree"
    And waiting until player "player" has no order
    Then player "player" should not be close to position 0, -2
    And "tree" should exist