package com.bank

import java.util.UUID

//NOTE: would rather just use util.UUID and mock it in tests, but we don't know how to pass UUID to our Account constructor
class UUIDService {
  def generate =
    java.util.UUID.randomUUID()
}

class Account(id: UUID, events: EventService) {
  def getBalance = {
    val sumOfDeposits = events.get[Deposited](_.accountId == id).foldLeft(BigDecimal(0))((acc, deposit) => acc + deposit.amount)
    val sumOfWithdrawals = events.get[Withdrawed](_.accountId == id).foldLeft(BigDecimal(0))((acc, withdrawal) => acc + withdrawal.amount)
    sumOfDeposits - sumOfWithdrawals
  }

  def deposit(amount: BigDecimal) =
    events.add(new Deposited(id, amount))

  def withdraw(amount: BigDecimal) =
    events.add(new Withdrawed(id, amount))
}

object Account {
  def create(name: String)(implicit events: EventService, uuid: UUIDService) =
    events.add(new AccountCreated(uuid.generate, name))
}
