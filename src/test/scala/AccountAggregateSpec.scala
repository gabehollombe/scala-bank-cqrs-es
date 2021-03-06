import java.util.UUID

import com.bank
import com.bank._
import org.scalamock.scalatest.MockFactory
import org.scalatest._

class AccountAggregateSpec extends FlatSpec
with Matchers
with MockFactory {
  implicit val timeService = stub[TimeService]
  implicit val uuidService = stub[UUIDService]
  implicit val eventService = stub[EventService]
  val repo = new AccountRepo(eventService, uuidService)

  def account( uuid:UUID = UUID.randomUUID() ): AccountAggregate = {
    new AccountAggregate(uuid, 0, repo)
  }

  implicit val eventServiceMock = stub[EventService]

  "Creating an account" should "generate its id from the uuid service" in {
    val uuid = UUID.randomUUID()
    val uuidMock = stub[com.bank.UUIDService]
    (uuidMock.generate _).when().returns(uuid)
    val account = AccountAggregate.create(0)(uuidMock, repo)
    account.id shouldBe uuid
  }

  "Handling deposited events" should "increase the balance" in {
    val accountId = UUID.randomUUID()
    val events = List(
      Deposited(accountId, 1),
      Deposited(accountId, 2)
    )
    val accountAggregate = new AccountAggregate(accountId, 0, repo)
    accountAggregate.loadEvents(events)

    accountAggregate.balance should be(3)
  }

  "Handling withdrawed events" should "decrease the balance" in {
    val accountId = UUID.randomUUID()
    val events = List(
      Withdrawed(accountId, 1),
      Withdrawed(accountId, 2)
    )
    val accountAggregate = new AccountAggregate(accountId, 10, repo)
    accountAggregate.loadEvents(events)

    accountAggregate.balance should be(-3)
  }

  "Handling monthly fee events" should "decrease the balance" in {
    val accountId = UUID.randomUUID()
    val events = List(
      MonthlyOverdraftFeeCharged(accountId, 1, 1, 2015),
      MonthlyOverdraftFeeCharged(accountId, 2, 2, 2015)
    )
    val accountAggregate = new AccountAggregate(accountId, 0, repo)
    accountAggregate.loadEvents(events)

    accountAggregate.balance should be(-3)
  }

  "Handling yearly interest paid events" should "decrease the balance" in {
    val accountId = UUID.randomUUID()
    val events = List(
      YearlyInterestPaid(accountId, 1, 2015),
      YearlyInterestPaid(accountId, 2, 2016)
    )
    val accountAggregate = new AccountAggregate(accountId, 0, repo)
    accountAggregate.loadEvents(events)

    accountAggregate.balance should be(3)
  }

  "Depositing money" should "create an unsaved Deposited event" in {
    val accountId = UUID.randomUUID()
    val event = Deposited(accountId, 100)

    val acc = account(accountId)
    acc.deposit(100)
    acc.unsavedEvents should contain(event)
  }

  it should "require a positive amount" in {
    account().deposit(-1) should be(AmountMustBePositiveError)
    account().deposit(0) should be(AmountMustBePositiveError)
  }

  "Withdrawing money" should "create an unsaved Withdrawed event" in {
    val accountId = UUID.randomUUID()
    val event = Withdrawed(accountId, 100)
    val account = new AccountAggregate(accountId, 100, repo)
    account.withdraw(100)
    account.unsavedEvents should contain(event)
  }

  it should "require a positive amount" in {
    account().withdraw(-1) should be(AmountMustBePositiveError)
    account().withdraw(0) should be(AmountMustBePositiveError)
  }

  it should "prevent overdraws that have passed the limit" in {
    val accountId = UUID.randomUUID()
    val overdrawLimit = BigDecimal(100)
    val account = new AccountAggregate(accountId, overdrawLimit, repo)
    var result = account.withdraw(100.01)
    result should be(OverdrawLimitExceededError)

    account.deposit(100)
    account.withdraw(200)
    result = account.withdraw(0.01)
    result should be(OverdrawLimitExceededError)
    account.unsavedEvents should not contain(Withdrawed(accountId, 0.01))
  }

  def transferFixture = new {
    class NoArgsAccountRepo extends AccountRepo(eventService, uuidService)

    val repo = stub[NoArgsAccountRepo]

    val account1 = new AccountAggregate(UUID.randomUUID(), 0, repo)

    val account2Id = UUID.randomUUID()
    class NoArgsAccountAggregate extends AccountAggregate(account2Id, 0, repo)
    val account2 = stub[NoArgsAccountAggregate]
    (repo.getAccount _).when(account2.id).returns(Option(account2))

    val transferAmount = BigDecimal(10)
  }

  "Transfering money to another account" should "create an unsaved withdrawed event from this account and create an unsaved transfered event from this account" in {
    val f = transferFixture
    f.account1.deposit(f.transferAmount)
    f.account1.transfer(f.transferAmount, f.account2.id)
    f.account1.unsavedEvents should contain(Withdrawed(f.account1.id, f.transferAmount))
    f.account1.unsavedEvents should contain(Transferred(f.account1.id, f.transferAmount, f.account2.id))
  }

  it should "deposit into destination account" in {
    val f = transferFixture
    f.account1.deposit(f.transferAmount)
    f.account1.transfer(f.transferAmount, f.account2.id)
    (f.account2.deposit _).verify(f.transferAmount)
  }

  it should "require a positive amount" in {
    account().transfer(-1, UUID.randomUUID()) should be(AmountMustBePositiveError)
    account().transfer(0, UUID.randomUUID()) should be(AmountMustBePositiveError)
  }

  it should "throw an InvalidAccountIdError error if destination account id is not found" in {
    a [InvalidAccountIdError] should be thrownBy account().transfer(100.00, UUID.randomUUID())
  }

  "Clearing unsaved events" should "empty out the unsaved events list" in {
    val acc = account()
    acc.deposit(1)
    acc.clearUnsavedEvents
    acc.unsavedEvents.length.shouldBe(0)
  }
}

