package com.bank

import java.util.UUID

//NOTE: would rather just use util.UUID and mock it in tests, but we don't know how to pass UUID to our AccountService constructor
class UUIDService {
  def generate =
    java.util.UUID.randomUUID()
}

class AccountService(eventService: EventService, uuid: UUIDService)  {
  def createAccount(name: String) =
    eventService.add(new AccountCreated(uuid.generate, name))

  def deposit(accountId: UUID, amount: BigDecimal) =
    eventService.add(new Deposited(accountId, amount))
}