import java.util.UUID

import com.bank._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, FlatSpec}

class AccountRepoSpec extends FlatSpec
with Matchers
with MockFactory {
  case class ExampleEvent(accountId: UUID) extends Event

  implicit val timeService = stub[TimeService]
  implicit val uuidService = stub[UUIDService]
  implicit val eventService= stub[EventService]

  val repo = new AccountRepo(eventService, uuidService)

  "Creating an account" should "should return it" in {
    val accountAgg : AccountAggregate = repo.createAccount(123)
    accountAgg.overdrawLimit.shouldBe(123)
  }

  it should "add an AccountCreated event" in {
    val uuid = UUID.randomUUID()
    (uuidService.generate _).when().returns(uuid)
    val accountAgg : AccountAggregate = repo.createAccount(100.00)
    (eventService.add[Event] _) verify AccountCreated(uuid, 100.00)
  }

  "Getting an account aggregate by its id" should "return the aggregate" in {
    val account = repo.createAccount(0)
    val result = repo.getAccount(account.id)
    result.get.shouldBe(account)
  }

  "Saving an aggregate" should "persist its unsaved events to the event service" in {
    val accountId = UUID.randomUUID()
    class NoArgsAccountAggregate extends AccountAggregate(accountId, 0, repo)

    val accountStub = stub[NoArgsAccountAggregate]
    val event = ExampleEvent(accountId)

    (accountStub.unsavedEvents _).when().returns(List(event, event))
    repo.saveAccount(accountStub)
    (eventService.add[Event] _) verify event twice
  }

  it should "clear its unsaved events" in {
    class NoArgsAccountAggregate extends AccountAggregate(UUID.randomUUID(), 0, repo)
    val accountStub = stub[NoArgsAccountAggregate]
    (accountStub.unsavedEvents _).when().returns(List())

    repo.saveAccount(accountStub)

    (accountStub.clearUnsavedEvents _).verify
  }
}
