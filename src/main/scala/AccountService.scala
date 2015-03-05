package com.bank

class AccountService(eventService: EventService)  {
  def createAccount(name: String) = {
    eventService.add(new AccountCreated(1, name))
  }

//  def nextId: Int =
//    eventService.allOfType(AccountCreated).length + 1

  def deposit(accountId: Int, amount: BigDecimal) = {
    eventService.add(new Deposited(accountId, amount))
  }
}