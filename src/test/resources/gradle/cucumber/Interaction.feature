Feature: Interaction
  Making sure interactions work.

  Scenario: Continue interacting when pushed away
    Given an empty world
    And a player "player" at position 0, 0
    And player "player" has 1 of item "Axe"
    And a "tree" at 0, 2 called "tree"
    And a player "otherPlayer" at position -2, 1.5
    When player "player" is ordered to interact with "tree"
    And waiting 2 seconds
    And player "otherPlayer" is ordered to move 5, 0 from current position
    And waiting until player "player" has no order
    Then "tree" should not exist
