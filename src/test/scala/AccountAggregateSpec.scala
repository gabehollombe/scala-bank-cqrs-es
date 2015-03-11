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

  "Handling deposited events" should "increase the balance" in {
    val accountId = UUID.randomUUID()
    val eventServiceStub = stub[EventService]
    val accountEvents = List(
      (AccountCreated(accountId, 0), 1L),
      (Deposited(accountId, 1), 2L),
      (Deposited(accountId, 2), 3L)
    )
    (eventServiceStub.accountEvents(_: UUID)).when(accountId).returns(accountEvents)
    val accountAggregate = new AccountAggregate(accountId, 0, eventServiceStub)

    accountAggregate.balance should be(3)
  }

  "Depositing money" should "create a Deposited event" in {
    val accountId = UUID.randomUUID()
    val eventServiceStub = stub[EventService]
    (eventServiceStub.accountEvents _).when(accountId).returns(List())
    val event = Deposited(accountId, 100)

    account(accountId, eventServiceStub).deposit(100)
    (eventServiceStub.add[Event] _).verify(event)
  }

  it should "require a positive amount" in {
    account().deposit(-1) should be(AmountMustBePositiveError)
    account().deposit(0) should be(AmountMustBePositiveError)
  }

  "Withdrawing money" should "create a Withdrawed event" in {
    val accountId = UUID.randomUUID()
    val eventServiceStub = stub[EventService]
    (eventServiceStub.accountEvents _).when(accountId).returns(List())
    val event = Withdrawed(accountId, 100)
    val account = new AccountAggregate(accountId, 100, eventServiceStub)
    account.withdraw(100)
    (eventServiceStub.add[Event] _).verify(event)
  }

  it should "require a positive amount" in {
    account().withdraw(-1) should be(AmountMustBePositiveError)
    account().withdraw(0) should be(AmountMustBePositiveError)
  }

  it should "prevent overdraws that have passed the limit" in {
    val accountId = UUID.randomUUID()
    val eventServiceStub = stub[EventService]
    (eventServiceStub.accountEvents _).when(accountId).returns(List())
    val overdrawLimit = BigDecimal(100)
    val account = new AccountAggregate(accountId, overdrawLimit, eventServiceStub)
    var result = account.withdraw(100.01)
    result should be(OverdrawLimitExceededError)

    account.deposit(100)
    account.withdraw(200)
    result = account.withdraw(0.01)
    result should be(OverdrawLimitExceededError)
    (eventServiceStub.add[Event] _).verify(Withdrawed(accountId, 0.01)).never
  }



  //Trying to mock calling get, but with no predicate argument (worked back before we added predicate arg, so this
  // will likey work better for testing `all` now...
  // (eventServiceMock.get(_:ClassTag[Deposited])).expects(*).returning(depositedEvents)

}

