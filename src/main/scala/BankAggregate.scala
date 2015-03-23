import java.util.UUID

import com.bank._

import scala.collection.mutable._

class BankService(repo: AccountRepo) {
  def accountIds = repo.accountIds

  def payInterests(year: Int) = {
    val timestamp = TimeService.timestampFor(1, year)
    accountIds.foreach { id =>
      val account : AccountAggregate = repo.getAccount(id, timestamp)
      if (account.notOverdrawn && account.notYetPaidForYear(year)) {
        val interest: BigDecimal = account.balance * 0.005
        account.payYearlyInterest(interest, year)
        repo.saveAccount(account)
      }
    }
  }

  def chargeFees(month: Int, year: Int) = {
    val timestamp = TimeService.timestampFor(month, year)
    accountIds.foreach { id =>
      val account : AccountAggregate = repo.getAccount(id, timestamp)
      if (account.isOverdrawn && account.notYetChargedForMonth(month, year)) {
        val fee: BigDecimal = (account.balance * 0.05).abs
        account.chargeMonthlyFee(fee, month, year)
        repo.saveAccount(account)
      }
    }
  }

}
