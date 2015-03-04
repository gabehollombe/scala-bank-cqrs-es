package com.bank

class AccountService(eventService: EventServiceTrait)  {
  def createAccount(name: String) = {
    eventService.add(new AccountCreated(1))
  }

  def deposit(accountId: Int, amount: BigDecimal) = {
    eventService.add(new Deposited(accountId, amount))
  }
}