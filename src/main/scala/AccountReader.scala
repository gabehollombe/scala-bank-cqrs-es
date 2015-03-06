package com.bank

import java.util.UUID

class AccountReader(id: UUID, events: EventService, until: Long = Long.MaxValue) {
  def notYetChargedForMonth(month: Int, year: Int): Boolean =
    events.
      get[MonthlyOverdraftFeeCharged](_.accountId == id).
      count(e => e.timestamp < until && e.month == month && e.year == year) == 0



  def isOverdrawn(): Boolean = {
    getBalance < 0
  }

  def getBalance = {
    val sumOfDeposits = events.get[Deposited](_.accountId == id).filter(_.timestamp < until).foldLeft(BigDecimal(0))((acc, deposit) => acc + deposit.amount)
    val sumOfWithdrawals = events.get[Withdrawed](_.accountId == id).filter(_.timestamp < until).foldLeft(BigDecimal(0))((acc, withdrawal) => acc + withdrawal.amount)
    val sumOfOverdraftFees = events.get[MonthlyOverdraftFeeCharged](_.accountId == id).filter(_.timestamp < until).foldLeft(BigDecimal(0))((acc, withdrawal) => acc + withdrawal.amount)
    sumOfDeposits - sumOfWithdrawals - sumOfOverdraftFees
  }


}
