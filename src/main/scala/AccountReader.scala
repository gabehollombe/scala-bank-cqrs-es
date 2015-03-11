package com.bank

import java.util.UUID

class AccountReader(id: UUID, events: EventService, until: Long = Long.MaxValue) {
  var balance: BigDecimal  = 0
  for (tup <- events.accountEvents(this.id).filter(_._2 < until)) this.applyEvent(tup._1)

  def applyEvent(event: Event) = event match {
    case e: Deposited => balance += e.amount
    case e: YearlyInterestPaid => balance += e.amount
    case e: Withdrawed => balance -= e.amount
    case e: MonthlyOverdraftFeeCharged => balance -= e.amount
    case _ => // Ignore unknown events
  }

  def notYetPaidForYear(year: Int): Boolean =
    events.
      accountEventsOfType[YearlyInterestPaid](id).
      // TODO: SAD TUPLE No Destructuring of anonymous param list
      count( (tup: (YearlyInterestPaid, Long)) => tup._2 < until && tup._1.year == year) == 0

  def notYetChargedForMonth(month: Int, year: Int): Boolean =
    events.
      accountEventsOfType[MonthlyOverdraftFeeCharged](id).
      count( (tup: (MonthlyOverdraftFeeCharged, Long)) => tup._2 < until && tup._1.month == month && tup._1.year == year) == 0


  def isOverdrawn(): Boolean = {
    balance < 0
  }
}
