package com.bank

import java.util.UUID

class AccountRepo(val eventService: EventService, uuidService : UUIDService) {
  implicit val repo = this
  implicit val uuid = uuidService

  def saveAccount(accountAggregate: AccountAggregate): Unit = {
    for (event <- accountAggregate.unsavedEvents) eventService.add(event)
    accountAggregate.clearUnsavedEvents
  }

  def createAccount(overdrawLimit: BigDecimal = 0) : AccountAggregate = {
    val account = AccountAggregate.create(overdrawLimit)
    eventService.add(new AccountCreated(account.id, overdrawLimit))
    accounts += (account.id -> account)
    account
  }

  def getAccount(id: UUID) = accounts.get(id)

  var accounts = Map[UUID, AccountAggregate]()
}
