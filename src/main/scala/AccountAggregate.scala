package com.bank

import java.util.UUID

//NOTE: would rather just use util.UUID and mock it in tests, but we don't know how to pass UUID to our Account constructor

case class InvalidAccountIdError() extends Error
case class AmountMustBePositiveError() extends Error
case class OverdrawLimitExceededError() extends Error


class UUIDService {
  def generate =
    java.util.UUID.randomUUID()
}

class AccountAggregate(id: UUID, overdrawLimit: BigDecimal = 0, events: EventService) {

  var balance: BigDecimal  = 0
  for (tup <- events.accountEvents(this.id)) this.applyEvent(tup._1)

  def applyEvent(event: Event) = event match {
    case e: Deposited => balance += e.amount
    case e: YearlyInterestPaid => balance += e.amount
    case e: Withdrawed => balance -= e.amount
    case e: MonthlyOverdraftFeeCharged => balance -= e.amount
    case _ => // Ignore unknown events
  }

  def deposit(amount: BigDecimal) =
    if (amount <= 0)
      AmountMustBePositiveError
    else {
      balance += amount
      events.add(new Deposited(id, amount))
    }

  def withdraw(amount: BigDecimal) =
    if (amount <= 0)
      AmountMustBePositiveError
    else if (balance - amount < -overdrawLimit)
      OverdrawLimitExceededError
    else {
      balance -= amount
      events.add(new Withdrawed(id, amount))
    }
}

object AccountAggregate {
  def create(overdrawLimit: BigDecimal)(implicit events: EventService, uuid: UUIDService) =
    events.add(new AccountCreated(uuid.generate, overdrawLimit))
}
