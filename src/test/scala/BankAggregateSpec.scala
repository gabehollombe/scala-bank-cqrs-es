import java.util.UUID

import com.bank._
import org.scalamock.scalatest.MockFactory
import org.scalatest.Matchers
import org.scalatest.FlatSpec

import scala.collection.mutable._

class BankAggregateSpec extends FlatSpec
with Matchers
with MockFactory
{

  implicit val timeService = stub[TimeService]

  val jan1 = TimeService.timestampFor(1, 2015)
  val feb1 = TimeService.timestampFor(2, 2015)

  val fakeNow = 12345L
  (timeService.currentTimeMillis _).when().returns(fakeNow)

  "A Bank" should "charge fees for accounts that are in overdraft at the end of the month" in {
    val eventService = new EventService

    val accountID = UUID.randomUUID()

    val accountEvents: MutableList[(Event, Long)] =  MutableList(
      (AccountCreated(accountID, 100), jan1 + 1),
      (Withdrawed(accountID, 50), jan1 + 3))
    eventService.events += accountID -> accountEvents //TODO stub instead

    val bank = new BankAggregate(eventService)
    val result = bank.chargeFees(2, 2015)
    val expectedFee = BigDecimal(0.05 * 50)
    val feeChargedEvent = MonthlyOverdraftFeeCharged(accountID, expectedFee, 2, 2015)

    result should be(MutableList(feeChargedEvent))
    eventService.all[MonthlyOverdraftFeeCharged].length should be(1)
  }

  it should "ignore accounts that are in overdraft in other months into the future" in {
    val eventService = new EventService

    val accountID = UUID.randomUUID()

    val accountEvents: MutableList[(Event, Long)] =  MutableList(
      //Created in Jan
      (AccountCreated(accountID, 100), jan1),

      //No deposits in Jan, overdrafted in Feb
      (Deposited(accountID, 100), feb1 + 2),
      (Withdrawed(accountID, 150), feb1 + 3))

    eventService.events += accountID -> accountEvents //TODO stub instead

    val bank = new BankAggregate(eventService)
    val result = bank.chargeFees(2, 2015)

    result should be(MutableList())
    eventService.all[MonthlyOverdraftFeeCharged].length should be(0)
  }

  it should "not charge fees if an account has already been charged for a given month" in {
    val eventService = new EventService
    val accountID = UUID.randomUUID()

    val feeChargedEvent = MonthlyOverdraftFeeCharged(accountID, 10.00, 2, 2015)

    // make fee charged event already in history
    val accountEvents: MutableList[(Event, Long)] =  MutableList(
      (AccountCreated(accountID, 100), jan1),
      (feeChargedEvent, jan1 + 1))
    eventService.events += accountID -> accountEvents //TODO stub instead

    val bank = new BankAggregate(eventService)
    val result = bank.chargeFees(2, 2015)

    // charging for Feb 2015 should not create another charge, since one already exists
    result should be(MutableList())
    eventService.all[MonthlyOverdraftFeeCharged].length should be(1)
  }

  val jan1NextYear = TimeService.timestampFor(1, 2016)
  val feb1NextYear = TimeService.timestampFor(2, 2016)

  "Paying interest for a year" should "pay non-overdrawn accounts 0.5 percent for the balance at the end of the previous year" in {
    val eventService = new EventService

    val accountID = UUID.randomUUID()

    val accountEvents: MutableList[(Event, Long)] =  MutableList(
      (AccountCreated(accountID, 100), jan1),
      (Deposited(accountID, 50), jan1 + 1))
    eventService.events += accountID -> accountEvents //TODO stub instead

    val bank = new BankAggregate(eventService)
    val result = bank.payInterests(2016)
    val expectedInterest = BigDecimal(0.005 * 50)
    val interestPaidEvent = YearlyInterestPaid(accountID, expectedInterest, 2016)

    result should be(MutableList(interestPaidEvent))
    eventService.all[YearlyInterestPaid].length should be(1)
  }

  it should "not pay interest on overdrawn accounts" in {
    val eventService = new EventService

    val accountID = UUID.randomUUID()

    val accountEvents: MutableList[(Event, Long)] =  MutableList(
      (AccountCreated(accountID, 100), jan1),
      (Withdrawed(accountID, 50), jan1 + 1))
    eventService.events += accountID -> accountEvents //TODO stub instead

    val bank = new BankAggregate(eventService)
    val result = bank.payInterests(2016)

    result should be(MutableList())
    eventService.all[YearlyInterestPaid].length should be(0)
  }



}
