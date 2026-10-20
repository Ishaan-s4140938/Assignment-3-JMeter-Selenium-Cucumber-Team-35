@billpay
Feature: Pay a bill
  As a bank customer
  I want to pay a named payee from my account
  So that I can settle my bills accurately.

  Background:
    Given I am a newly registered customer with a funded account

  @positive
  Scenario: Pay a bill using matching payee account details
    When I pay "25.50" to "Sydney Utilities" with payee account "12345" confirmed as "12345"
    Then the payment confirmation identifies the payee, amount and source account
    And my source account is debited by "25.50"

  @negative
  Scenario: Reject mismatched payee account numbers
    When I pay "25.50" to "Sydney Utilities" with payee account "12345" confirmed as "54321"
    Then payment is blocked with "The account numbers do not match."
    And my source account balance remains unchanged

  @negative
  Scenario Outline: Reject a missing or invalid payment amount
    When I pay "<amount>" to "Sydney Utilities" with payee account "12345" confirmed as "12345"
    Then payment is blocked with "<message>"
    And my source account balance remains unchanged

    Examples:
      | amount | message                     |
      |        | The amount cannot be empty. |
      | abc    | Please enter a valid amount.|

  @negative
  Scenario: Require a payee name
    When I pay "25.50" to "" with payee account "12345" confirmed as "12345"
    Then payment is blocked with "Payee name is required."
    And my source account balance remains unchanged
