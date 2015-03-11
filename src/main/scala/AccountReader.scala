package com.bank

import java.util.UUID

class AccountReader(id: UUID, events: EventService, until: Long = Long.MaxValue) {
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
    getBalance < 0
  }

  def getBalance = {
    // TODO: SAD TUPLE No Destructuring of anonymous param list in ALL of these =-(
    val sumOfDeposits = events.accountEventsOfType[Deposited](id).filter(_._2 < until).foldLeft(BigDecimal(0))((acc, tup) => acc + tup._1.amount)
    val sumOfWithdrawals = events.accountEventsOfType[Withdrawed](id).filter(_._2 < until).foldLeft(BigDecimal(0))((acc, tup) => acc + tup._1.amount)
    val sumOfOverdraftFees = events.accountEventsOfType[MonthlyOverdraftFeeCharged](id).filter(_._2 < until).foldLeft(BigDecimal(0))((acc, tup) => acc + tup._1.amount)
    sumOfDeposits - sumOfWithdrawals - sumOfOverdraftFees
  }
}
