package com.bank

import java.util.UUID

object AccountRepo {
  implicit val timeService = new TimeService()
  implicit val eventService = new EventService()
  implicit val uuidService = new UUIDService()

  def createAccount(overdrawLimit: Int = 0) : AccountAggregate = {
    val account = AccountAggregate.create(overdrawLimit)
    accounts += (account.id -> account)
    account
  }

  def getAccount(id: UUID) = accounts.get(id)

  var accounts = Map[UUID, AccountAggregate]()

}
