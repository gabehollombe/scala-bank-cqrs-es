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

  "A Bank" should "charge fees for accounts that are in overdraft at the end of the month" in {
    val eventService = new EventService

    val overdrawnAccountID = UUID.randomUUID()
    val accountID          = UUID.randomUUID()

    eventService.events = MutableList(
      AccountCreated(accountID, 100, jan1),
      AccountCreated(overdrawnAccountID, 100, jan1 + 1),
      Deposited(accountID, 100, jan1 + 2),
      Withdrawed(overdrawnAccountID, 50, jan1 + 3),
      Withdrawed(accountID, 150, feb1)
    )

    (timeService.currentTimeMillis _).when().returns(12345)

    val bank = new BankAggregate(eventService)
    val result = bank.chargeFees(2, 2015)
    val expectedFee = BigDecimal(0.05 * 50)
    val feeChargedEvent = MonthlyOverdraftFeeCharged(overdrawnAccountID, expectedFee, 2, 2015, 12345)

    result should be(MutableList(feeChargedEvent))
    eventService.events.count(_ == feeChargedEvent) should be(1)
  }
  
  it should "not charge fees if an account has already been charged for a given month" in {
    val eventService = new EventService
    val accountID = UUID.randomUUID()
    (timeService.currentTimeMillis _).when().returns(12345)

    val feeChargedEvent = MonthlyOverdraftFeeCharged(accountID, 10.00, 2, 2015, 12345)
    eventService.events = MutableList(
      AccountCreated(accountID, 100, jan1),
      feeChargedEvent
    )

    val bank = new BankAggregate(eventService)
    var result = bank.chargeFees(2, 2015)

    result should be(MutableList())
    eventService.events.count(_ == feeChargedEvent) should be(1)
  }

}
