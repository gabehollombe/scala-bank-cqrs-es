package com.bank

import java.util.UUID

//NOTE: would rather just use util.UUID and mock it in tests, but we don't know how to pass UUID to our Account constructor
class UUIDService {
  def generate =
    java.util.UUID.randomUUID()
}

class Account(id: UUID, events: EventService) {
  def getBalance =
    events.get[Deposited](_.accountId == id).foldLeft(BigDecimal(0))((acc, deposit) => acc + deposit.amount )

  def deposit(amount: BigDecimal) =
    events.add(new Deposited(id, amount))
}

object Account {
  def create(name: String)(implicit events: EventService, uuid: UUIDService) =
    events.add(new AccountCreated(uuid.generate, name))
}
