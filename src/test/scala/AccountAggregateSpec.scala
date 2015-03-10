import java.util.UUID

import com.bank._
import org.scalamock.scalatest.MockFactory
import org.scalatest._

import scala.reflect.ClassTag

class AccountAggregateSpec extends FlatSpec
with Matchers
with MockFactory {

  implicit val timeService = stub[TimeService]

  def account( uuid:UUID = UUID.randomUUID(),
               eventService:EventService = new EventService()
               ): AccountAggregate = {
    new AccountAggregate(uuid, 0, eventService)
  }

  "Creating an account" should "add an AccountCreated event" in {
    val uuid = UUID.randomUUID()
    val eventServiceMock = mock[EventService]
    val uuidMock = stub[com.bank.UUIDService]
    (uuidMock.generate _).when().returns(uuid)
    val event = AccountCreated(uuid, 0)
    (eventServiceMock.add[Event] _).expects(event)
    AccountAggregate.create(0)(eventServiceMock, uuidMock)
  }

  // TODO: Look at this again.
//  it should "not initialize without an existing AccountCreated event with the appropriate id" in {
//    val eventService = new EventService()
//    val accountUUID = UUID.randomUUID()
//    eventService.events = List ( AccountCreated(accountUUID, 0) )
//    val nonExistantUUID = UUID.randomUUID()
//    a [InvalidAccountIdError] should be thrownBy new AccountAggregate(id = nonExistantUUID, events = eventService)
//  }

  "Depositing money" should "create a Deposited event" in {
    val accountId = UUID.randomUUID()
    val eventServiceMock = mock[EventService]
    val event = Deposited(accountId, 100)
    (eventServiceMock.add[Event] _).expects(event)

    account(accountId, eventServiceMock).deposit(100)
  }

  it should "require a positive amount" in {
    account().deposit(-1) should be(AmountMustBePositiveError)
    account().deposit(0) should be(AmountMustBePositiveError)
  }

  "Withdrawing money" should "create a Withdrawed event" in {
    val accountId = UUID.randomUUID()
    val eventServiceMock = mock[EventService]
    val event = Withdrawed(accountId, 100)
    (eventServiceMock.add[Event] _).expects(event)
    val account = new AccountAggregate(accountId, 100, eventServiceMock)
    account.withdraw(100)
  }

  it should "require a positive amount" in {
    account().withdraw(-1) should be(AmountMustBePositiveError)
    account().withdraw(0) should be(AmountMustBePositiveError)
  }

  it should "prevent overdraws that have passed the limit" in {
    val accountId = UUID.randomUUID()
    val fakeEventService = stub[EventService]
    val overdrawLimit = BigDecimal(100)
    val account = new AccountAggregate(accountId, overdrawLimit, fakeEventService)
    var result = account.withdraw(100.01)
    result should be(OverdrawLimitExceededError)

    account.deposit(100)
    account.withdraw(200)
    result = account.withdraw(0.01)
    result should be(OverdrawLimitExceededError)
    (fakeEventService.add[Event] _).verify(Withdrawed(accountId, 0.01)).never
  }


  //Trying to mock calling get, but with no predicate argument (worked back before we added predicate arg, so this
  // will likey work better for testing `all` now...
  // (eventServiceMock.get(_:ClassTag[Deposited])).expects(*).returning(depositedEvents)

}

