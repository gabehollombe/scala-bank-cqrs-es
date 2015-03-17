package com.bank

import java.util.UUID

case class InvalidAccountIdError() extends Error
case class AmountMustBePositiveError() extends Error
case class OverdrawLimitExceededError() extends Error

class AccountAggregate(val id: UUID, val overdrawLimit: BigDecimal = 0, events: EventService) {

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
    else addAndApply(new Deposited(id, amount))

  def withdraw(amount: BigDecimal) =
    if (amount <= 0)
      AmountMustBePositiveError
    else if (balance - amount < -overdrawLimit)
      OverdrawLimitExceededError
    else addAndApply(new Withdrawed(id, amount))

  def transfer(amount: BigDecimal, destinationAccountId: UUID) = {
    if (amount <= 0)
      AmountMustBePositiveError
    else {
      addAndApply(new Withdrawed(id, amount))
      events.add(new Deposited(destinationAccountId, amount))
      events.add(new Transferred(this.id, amount, destinationAccountId))
    }
  }

  def addAndApply(event: Event) = {
    events.add(event)
    applyEvent(event)
  }
}

object AccountAggregate {
  def create(overdrawLimit: BigDecimal)(implicit events: EventService, uuid: UUIDService) = {
    val id = uuid.generate
    events.add(new AccountCreated(id, overdrawLimit))
    new AccountAggregate(id, overdrawLimit, events)
  }
}
