package com.bank

import java.util.UUID

class AccountRepo(val eventService: EventService, uuidService : UUIDService) {
  implicit val repo = this
  implicit val uuid = uuidService
  var accounts = Map[UUID, AccountAggregate]()

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
  def getAccountReader(id: UUID, until: Long = Long.MaxValue) = {
    val accountReader = new AccountReader
    val events = eventService.accountEvents(id, until)
    accountReader.loadEvents(events)
    accountReader
  }

  def accountIds = accounts.keys

}
