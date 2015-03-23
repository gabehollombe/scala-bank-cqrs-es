package com.bank

import java.util.UUID
import scala.collection.mutable._

case class InvalidAccountIdError() extends Error
case class AmountMustBePositiveError() extends Error
case class OverdrawLimitExceededError() extends Error

class AccountAggregate(val id: UUID, val overdrawLimit: BigDecimal = 0, repo: AccountRepo) {
  def notYetChargedForMonth(month: Int, year: Int): Boolean =
    ! feeChargedMonths.contains((month, year))


  var balance: BigDecimal = 0
  var interestPaidYears : MutableList[Int] = new MutableList()
  var feeChargedMonths : MutableList[(Int, Int)] = new MutableList()
  var unsavedEventsList: MutableList[Event] = MutableList()

  def notYetPaidForYear(year: Int): Boolean =
    ! interestPaidYears.contains(year)

  def notOverdrawn = balance >= 0
  def isOverdrawn = balance < 0

  def unsavedEvents : List[Event] = this.unsavedEventsList.toList

  def clearUnsavedEvents =
    unsavedEventsList.clear()

  def loadEvents(events: List[Event]) =
    for (event <- events) this.applyEvent(event)

  def applyEvent(event: Event) = event match {
    case e: Deposited => balance += e.amount
    case e: Withdrawed => balance -= e.amount
    case e: YearlyInterestPaid => onYearlyInterestPaid(e)
    case e: MonthlyOverdraftFeeCharged => onMonthlyOverdraftFeeCharged(e)
    case _ => // Ignore unknown events
  }

  def onMonthlyOverdraftFeeCharged(event: MonthlyOverdraftFeeCharged) = {
    balance -= event.amount
    feeChargedMonths += ((event.month, event.year))
  }

  def onYearlyInterestPaid(event: YearlyInterestPaid) = {
    balance += event.amount
    interestPaidYears += event.year
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

  def payYearlyInterest(amount: BigDecimal, year: Int) =
    this.synchronized {
      addAndApply(YearlyInterestPaid(amount, year))
    }

  def chargeMonthlyFee(amount: BigDecimal, month: Int, year: Int) =
    this.synchronized {
      addAndApply(MonthlyOverdraftFeeCharged(amount, month, year)
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
