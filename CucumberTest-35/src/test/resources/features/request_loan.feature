@loan
Feature: Request a loan
  As a bank customer
  I want my loan application assessed against my funds and down payment
  So that I receive an appropriate lending decision.

  Background:
    Given I am a newly registered customer with a funded account

  @positive
  Scenario: Approve an affordable loan with a sufficient down payment
    When I request a loan of "100.00" with a down payment of "20.00"
    Then the loan decision is "Approved"
    And a new loan account contains "100.00"
    And my source account is debited by "20.00"

  @negative
  Scenario: Deny a loan when the down payment exceeds available funds
    When I request a loan of "1000000.00" with a down payment greater than my available funds
    Then the loan decision is "Denied"
    And a reason is provided without creating a loan account
    And my source account balance remains unchanged
