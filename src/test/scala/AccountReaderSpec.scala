
import java.util.UUID

import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, FlatSpec}

import scala.collection.mutable
import scala.collection.mutable._
import com.bank._


class AccountReaderSpec extends FlatSpec
with Matchers
with MockFactory
{

  implicit val timeService = new TimeService

  def reader(id: UUID, eventService: EventService = new EventService()) =
    new AccountReader(id, eventService)

  "Getting balance" should "sum up all of the deposits and interest paid, and subtract all withdrawals and monthly fees" in {
    val accountId = UUID.randomUUID()
    val otherAccountId = UUID.randomUUID()

    val eventService = stub[EventService]
    val accountEvents = List(
      Deposited(accountId, 1),
      Deposited(accountId, 2),
      Withdrawed(accountId, 0.5),
      Withdrawed(accountId, 0.2),
      YearlyInterestPaid(accountId, 0.1, 2015),
      MonthlyOverdraftFeeCharged(accountId, 0.1, 1, 2015))

    val otherAccountEvents = List(
      Deposited(otherAccountId, 100),
      Withdrawed(otherAccountId, 50))

    (eventService.accountEvents _).when(accountId, *).returns(accountEvents)
    (eventService.accountEvents _).when(otherAccountId, *).returns(otherAccountEvents)

    val result = reader(accountId, eventService).balance
    result should be(2.3)
  }

  it should "allow limiting the events given a timestamp" in {
    val accountId = UUID.randomUUID()

    val eventService = new EventService()
    val accountEvents: mutable.MutableList[(Event, Long)] =  MutableList(
      (Deposited(accountId, 100), 1L),
      (Deposited(accountId, 200), 2L),
      (Withdrawed(accountId, 100), 3L),
      (Withdrawed(accountId, 200), 4L))

    //TODO stub, don't touch private events on ES
    eventService.events += accountId -> accountEvents

    val account = new AccountReader(accountId, eventService, 4)

    account.balance should be(200)
  }

}
