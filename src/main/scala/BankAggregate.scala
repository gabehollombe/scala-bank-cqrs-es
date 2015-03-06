import com.bank._

import scala.collection.mutable

class BankAggregate(val events: EventService) {

  def accountIds = {
    events.all[AccountCreated].map(_.id)
  }

  def chargeFees(month: Int, year: Int) = {
    val timestamp = TimeService.timestampFor(month, year)

    val feeChargeds = new mutable.MutableList[Event]

    accountIds.foreach { id =>
      val accountReader = new AccountReader(id, events, timestamp)
      if (accountReader.isOverdrawn() && accountReader.notYetChargedForMonth(month, year)) {
        val fee: BigDecimal = (accountReader.getBalance * 0.05).abs
        val feeCharged = MonthlyOverdraftFeeCharged(id, fee, month, year)
        feeChargeds += feeCharged
        events.add(feeCharged)
      }
    }
    feeChargeds
  }


}
