package com.bank

import java.util.UUID

import scala.collection.mutable
import scala.collection.mutable._

case class InvalidAccountIdError() extends Error
case class AmountMustBePositiveError() extends Error
case class OverdrawLimitExceededError() extends Error

class AccountAggregate(val id: UUID, val overdrawLimit: BigDecimal = 0, repo: AccountRepo) {

  var balance: BigDecimal = 0
  var unsavedEventsList: MutableList[Event] = MutableList()

  def unsavedEvents : List[Event] = this.unsavedEventsList.toList

  def loadEvents(events: List[Event]) =
    for (event <- events) this.applyEvent(event)

  def applyEvent(event: Event) = event match {
    case e: Deposited => balance += e.amount
    case e: YearlyInterestPaid => balance += e.amount
    case e: Withdrawed => balance -= e.amount
    case e: MonthlyOverdraftFeeCharged => balance -= e.amount
    case _ => // Ignore unknown events
  }

  def deposit(amount: BigDecimal) =
    this.synchronized {
      if (amount <= 0)
        AmountMustBePositiveError
      else addAndApply(new Deposited(id, amount))
    }

  def withdraw(amount: BigDecimal) =
    this.synchronized {
      if (amount <= 0)
        AmountMustBePositiveError
      else if (balance - amount < -overdrawLimit)
        OverdrawLimitExceededError
      else addAndApply(new Withdrawed(id, amount))
    }

  def transfer(amount: BigDecimal, destinationAccountId: UUID) = {
    this.synchronized {
      if (amount <= 0)
        AmountMustBePositiveError
      else {
        repo.getAccount(destinationAccountId) match {
          case Some(destinationAccount) => {
            this.withdraw(amount)
            destinationAccount.deposit(amount)
            unsavedEventsList += new Transferred(this.id, amount, destinationAccountId)
          }
          case _ => throw new InvalidAccountIdError()
        }
      }
    }
  }

  def addAndApply(event: Event) = {
    unsavedEventsList += event
    applyEvent(event)
  }
}

object AccountAggregate {
  def create(overdrawLimit: BigDecimal)(implicit uuid: UUIDService, repo: AccountRepo) = {
    val id = uuid.generate
    new AccountAggregate(id, overdrawLimit, repo)
  }
}
