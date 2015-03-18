
import java.util.UUID

import org.scalatest.{Matchers, FlatSpec}

import scala.collection.mutable
import scala.collection.mutable._
import com.bank._


class AccountReaderSpec extends FlatSpec with Matchers {

  implicit val timeService = new TimeService

  def reader(id: UUID, eventService: EventService = new EventService()) =
    new AccountReader(id, eventService)

  "Getting balance" should "sum up all of the deposits made, and subtract all withdrawals and monthly fees" in {
    val accountId = UUID.randomUUID()
    val otherAccountId = UUID.randomUUID()

    val eventService = new EventService()

    val accountEvents: mutable.MutableList[(Event, Long)] =  MutableList(
      (Deposited(accountId, 1), 1L),
      (Deposited(accountId, 2), 2L),
      (Withdrawed(accountId, 0.5), 3L),
      (Withdrawed(accountId, 0.2), 4L),
      (MonthlyOverdraftFeeCharged(accountId, 0.1, 1, 2015), 5L))

    val otherAccountEvents: mutable.MutableList[(Event, Long)] =  MutableList(
      (Deposited(otherAccountId, 100), 1L),
      (Withdrawed(otherAccountId, 50), 2L))

    //TODO stub, don't touch private events on ES
    eventService.events += accountId -> accountEvents
    eventService.events += otherAccountId -> otherAccountEvents

    val result = reader(accountId, eventService).balance
    result should be(2.2)

    //TODO also consider transfers
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
