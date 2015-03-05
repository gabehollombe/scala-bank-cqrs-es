import java.util.UUID

import com.bank._
import org.scalamock.scalatest.MockFactory
import org.scalatest._

import scala.reflect.ClassTag

class AccountSpec extends FlatSpec
with Matchers
with MockFactory {

  def account( uuid:UUID = UUID.randomUUID(),
               eventService:EventService = new EventService()
               ): Account = {
    new Account(uuid, eventService)
  }

  "Creating an account" should "add an AccountCreated event and return it" in {
    val uuid = UUID.randomUUID()
    val eventServiceMock = mock[EventService]
    val uuidMock = stub[com.bank.UUIDService]
    (uuidMock.generate _).when().returns(uuid)
    val event = AccountCreated(uuid, "gabe")
    (eventServiceMock.add[Event] _).expects(event).returning(event)

    val result = Account.create("gabe")(eventServiceMock, uuidMock)
    result should be(event)
  }

  "Depositing money" should "create a Deposited event and return it" in {
    val accountId = UUID.randomUUID()
    val eventServiceMock = mock[EventService]
    val event = Deposited(accountId, 100)
    (eventServiceMock.add[Event] _).expects(event).returning(event)

    val result = account(accountId, eventServiceMock).deposit(100)
    result should be(event)
  }

  "Withdrawing money" should "create a Withdrawed event and return it" in {
    val accountId = UUID.randomUUID()
    val eventServiceMock = mock[EventService]
    val event = Withdrawed(accountId, 100)
    (eventServiceMock.add[Event] _).expects(event).returning(event)

    val result = account(accountId, eventServiceMock).withdraw(100)
    result should be(event)
  }

  //Trying to mock calling get, but with no predicate argument (worked back before we added predicate arg, so this
  // will likey work better for testing `all` now...
  // (eventServiceMock.get(_:ClassTag[Deposited])).expects(*).returning(depositedEvents)

  "Getting balance" should "sum up all of the deposits made, and subtract all withdrawals" in {
    val accountId = UUID.randomUUID()
    val otherAccountId = UUID.randomUUID()

    val eventService= new EventService()

    eventService.events = List(
      Deposited(accountId, 1),
      Deposited(accountId, 2),
      Withdrawed(accountId, 0.5),
      Withdrawed(accountId, 0.2),

      Deposited(otherAccountId, 100),
      Withdrawed(otherAccountId, 50))

    val result = account(accountId, eventService).getBalance
    result should be(2.3)

    //TODO also consider transfers
  }
}

