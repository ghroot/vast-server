Feature: Test feature
  Test description

  Scenario: Testing
    Given a world with size 50 x 50 and seed 1234
    When player "test" connects
    And player "test" has 1 of item "Axe"
    And designates the object of type "tree" closest to player "test" as "tree"
    And order player "test" to interact with "tree"
    Then object "tree" should not exist

  Scenario: Testing 2
    Given a world with size 300 x 300 and seed 1234
    When player "test" connects
    And player "test2" connects
    And designates the object of type "player" closest to player "test" as "test2"
    And order player "test" to follow "test2"
    And order player "test2" to move 10, 10 from current position
    And player "test" stops moving
    And player "test2" stops moving