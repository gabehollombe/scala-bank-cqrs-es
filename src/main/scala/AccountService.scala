package com.bank

class AccountService(eventService: EventService)  {
  def createAccount(name: String) = {
    eventService.add(new AccountCreated(nextId, name))
  }

  def nextId: Int =
    eventService.eventsOfType(classOf[AccountCreated]).length + 1

  def deposit(accountId: Int, amount: BigDecimal) = {
    eventService.add(new Deposited(accountId, amount))
  }
}