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
    (eventServiceMock.accountEvents _).expects(uuid).returns(List())
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

  "Handling withdrawed events" should "decrease the balance" in {
    val accountId = UUID.randomUUID()
    val eventServiceStub = stub[EventService]
    val accountEvents = List(
      (AccountCreated(accountId, 0), 1L),
      (Withdrawed(accountId, 1), 2L),
      (Withdrawed(accountId, 2), 3L)
    )
    (eventServiceStub.accountEvents(_: UUID)).when(accountId).returns(accountEvents)
    val accountAggregate = new AccountAggregate(accountId, 0, eventServiceStub)

    accountAggregate.balance should be(-3)
  }

  "Handling monthly fee events" should "decrease the balance" in {
    val accountId = UUID.randomUUID()
    val eventServiceStub = stub[EventService]
    val accountEvents = List(
      (AccountCreated(accountId, 0), 1L),
      (MonthlyOverdraftFeeCharged(accountId, 1, 1, 2015), 2L),
      (MonthlyOverdraftFeeCharged(accountId, 2, 2, 2015), 3L)
    )
    (eventServiceStub.accountEvents(_: UUID)).when(accountId).returns(accountEvents)
    val accountAggregate = new AccountAggregate(accountId, 0, eventServiceStub)

    accountAggregate.balance should be(-3)
  }

  "Handling yearly interest paid events" should "decrease the balance" in {
    val accountId = UUID.randomUUID()
    val eventServiceStub = stub[EventService]
    val accountEvents = List(
      (AccountCreated(accountId, 0), 1L),
      (YearlyInterestPaid(accountId, 1, 2015), 2L),
      (YearlyInterestPaid(accountId, 2, 2016), 3L)
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

  "Transfering money to another account" should "withdraw from this account, deposit into destination account, and create a transfer event" in {
    val repo = AccountRepo
    val account1 = repo.createAccount()
    val account2 = repo.createAccount()
    val transferAmount = 10
    account1.deposit(11)
    account1.transfer(transferAmount, account2.id)

    // TODO: Feels a bit weird to talk to repo's eventService directly here instead of injecting this in to a
    // repo instance (but we want repo as a singleton (object) so good enough for now?
    // We'll just test the side effect in the system: that event service ends up with right events
    repo.eventService.accountEvents(account1.id).map(_._1) should contain (Withdrawed(account1.id, transferAmount))
    repo.eventService.accountEvents(account2.id).map(_._1) should contain (Deposited(account2.id, transferAmount))
    repo.eventService.all[Transferred].map(_._1) should contain (Transferred(account1.id, transferAmount, account2.id))
  }

  it should "require a positive amount" in {
    account().transfer(-1, UUID.randomUUID()) should be(AmountMustBePositiveError)
    account().transfer(0, UUID.randomUUID()) should be(AmountMustBePositiveError)
  }
}

