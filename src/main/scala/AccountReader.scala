package com.bank

import java.util.UUID

import scala.collection.mutable._

class AccountReader(id: UUID, events: EventService, until: Long = Long.MaxValue) {
  var balance: BigDecimal  = 0
  var interestPaidYears : MutableList[Int] = new MutableList()
  var feeChargedMonths : MutableList[(Int, Int)] = new MutableList()
  for (tup <- events.accountEvents(this.id, until)) this.applyEvent(tup._1)

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

  def notYetPaidForYear(year: Int): Boolean =
    ! interestPaidYears.contains(year)

  def notYetChargedForMonth(month: Int, year: Int): Boolean =
    ! feeChargedMonths.contains((month, year))


  def isOverdrawn(): Boolean = {
    balance < 0
  }
}
