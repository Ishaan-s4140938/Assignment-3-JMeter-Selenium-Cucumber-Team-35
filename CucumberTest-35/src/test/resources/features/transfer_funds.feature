@transfer
Feature: Transfer funds between my accounts
  As a bank customer
  I want to move money between my accounts
  So that I can manage my available funds.

  Background:
    Given I am a newly registered customer with two funded accounts

  @positive
  Scenario Outline: Transfer a valid amount between distinct accounts
    When I transfer "<amount>" from my first account to my second account
    Then the transfer confirmation identifies the amount and both accounts
    And my first account is debited and my second account is credited by "<amount>"

    Examples:
      | amount |
      | 10.00  |
      | 0.01   |

  @negative
  Scenario Outline: Reject a transfer without a numeric amount
    When I transfer "<amount>" from my first account to my second account
    Then the transfer is rejected without a success confirmation
    And both account balances remain unchanged

    Examples:
      | amount |
      |        |
      | abc    |
