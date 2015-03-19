import com.bank._

import scala.collection.mutable

class BankAggregate(val events: EventService) {
  def payInterests(year: Int) = {
    val timestamp = TimeService.timestampFor(1, year)

    val interestsPaid = new mutable.MutableList[Event]

    accountIds.foreach { id =>
      val accountReader = new AccountReader(id, events, timestamp)
      if (! accountReader.isOverdrawn() && accountReader.notYetPaidForYear(year)) {
        val interest: BigDecimal = accountReader.balance * 0.005
        val interestPaid = YearlyInterestPaid(id, interest, year)
        interestsPaid += interestPaid
        events.add(interestPaid)
      }
    }
    interestsPaid
  }


  def accountIds = {
    events.all[AccountCreated].map(_.accountId)
  }

  def chargeFees(month: Int, year: Int) = {
    val timestamp = TimeService.timestampFor(month, year)

    val feeChargeds = new mutable.MutableList[Event]

    accountIds.foreach { id =>
      val accountReader = new AccountReader(id, events, timestamp)
      if (accountReader.isOverdrawn() && accountReader.notYetChargedForMonth(month, year)) {
        val fee: BigDecimal = (accountReader.balance * 0.05).abs
        val feeCharged = MonthlyOverdraftFeeCharged(id, fee, month, year)
        feeChargeds += feeCharged
        events.add(feeCharged)
      }
    }
    feeChargeds
  }


}
