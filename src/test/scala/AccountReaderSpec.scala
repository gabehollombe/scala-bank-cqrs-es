import com.bank._

import java.util.UUID

import org.scalatest.{Matchers, FlatSpec}

import scala.collection.mutable._


class AccountReaderSpec extends FlatSpec with Matchers {

  implicit val timeService = new TimeService


  def reader(id: UUID, eventService: EventService = new EventService()) =
    new AccountReader(id, eventService)

  "Getting balance" should "sum up all of the deposits made, and subtract all withdrawals" in {
    val accountId = UUID.randomUUID()
    val otherAccountId = UUID.randomUUID()

    val eventService = new EventService()

    eventService.events = MutableList(
      Deposited(accountId, 1),
      Deposited(accountId, 2),
      Withdrawed(accountId, 0.5),
      Withdrawed(accountId, 0.2),

      Deposited(otherAccountId, 100),
      Withdrawed(otherAccountId, 50))

    val result = reader(accountId, eventService).getBalance
    result should be(2.3)

    //TODO also consider transfers
  }

  it should "allow limiting the events given a timestamp" in {
    val accountId = UUID.randomUUID()

    val eventService = new EventService()

    eventService.events = MutableList(
      Deposited(accountId, 100, 1),
      Deposited(accountId, 200, 2),
      Withdrawed(accountId, 100, 3),
      Withdrawed(accountId, 200, 4))

    val account = new AccountReader(accountId, eventService, 4)

    account.getBalance should be(200)
  }

}
