package com.bank

import java.util.UUID

class AccountReader(id: UUID, events: EventService, until: Long = Long.MaxValue) {
  def isOverdrawn(): Boolean = {
    getBalance < 0
  }

  def getBalance = {
    val sumOfDeposits = events.get[Deposited](_.accountId == id).foldLeft(BigDecimal(0))((acc, deposit) => acc + deposit.amount)
    val sumOfWithdrawals = events.get[Withdrawed](_.accountId == id).foldLeft(BigDecimal(0))((acc, withdrawal) => acc + withdrawal.amount)
    sumOfDeposits - sumOfWithdrawals
  }


}
