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

  "A Bank" should "charge fees for accounts that are in overdraft at the end of the month" in {

    val eventService = new EventService

    val overdrawnAccountID = UUID.randomUUID()
    val accountID          = UUID.randomUUID()

    eventService.events = MutableList(
      AccountCreated(accountID, 100, jan1),
      AccountCreated(overdrawnAccountID, 100, jan1 + 1),
      Deposited(accountID, 100, jan1 + 2),
      Withdrawed(overdrawnAccountID, 50, jan1 + 3)
    )

    (timeService.currentTimeMillis _).when().returns(12345)

    val bank = new BankAggregate(eventService)
    val events = bank.chargeFees(2, 2015)
    val expectedFee = BigDecimal(0.05 * 50)

    events should be(MutableList(FeeCharged(overdrawnAccountID, expectedFee, 12345)))
  }

}
