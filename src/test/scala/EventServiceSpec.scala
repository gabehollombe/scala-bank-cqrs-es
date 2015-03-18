import java.util.UUID

import com.bank._
import org.scalatest._
import org.scalamock.scalatest.MockFactory
import scala.collection.mutable._

class EventServiceSpec extends FlatSpec
with Matchers
with MockFactory {

  case class ExampleEvent(accountId: UUID) extends Event

  implicit val timeService = stub[TimeService]

  def service: EventService = {
    new EventService()
  }
  def makeUUID =
    java.util.UUID.randomUUID()

  "getting events" should "find by type" in {
    val s = service
    val accountId = makeUUID
    val created   = AccountCreated(accountId, 0)
    val deposited = Deposited(accountId, 100)
    val accountEvents: MutableList[(Event, Long)] = MutableList((created, 1L), (deposited, 2L))
    s.events += accountId -> accountEvents

    val results = s.all[AccountCreated]
    results should be (List( (created, 1L) ))
  }

  "getting events" should "find by account id" in {
    val s = service
    val accountId = makeUUID
    val created   = AccountCreated(accountId, 0)
    val deposited = Deposited(accountId, 100)
    val accountEvents: MutableList[(Event, Long)] = MutableList((created, 1L), (deposited, 2L))
    s.events += accountId -> accountEvents

    val results = s.accountEvents(accountId)
    results should be(accountEvents)
  }

  it should "find by type and account id" in {
    val s = service
    val accountId1 = makeUUID
    val accountId2 = makeUUID
    val deposited1 = Deposited(accountId1, 100)
    val deposited2 = Deposited(accountId2, 200)
    val account1Events: MutableList[(Event, Long)] = MutableList((deposited1, 1L))
    val account2Events: MutableList[(Event, Long)] = MutableList((deposited2, 2L))
    s.events += accountId1 -> account1Events
    s.events += accountId2 -> account2Events

    val results = s.accountEventsOfType[Deposited](accountId1)
    results should be (List( (deposited1, 1L) ))
  }

  it should "find by account id and cutoff date" in {
    val s = service
    val accountId1 = makeUUID
    val accountId2 = makeUUID
    val deposited1 = Deposited(accountId1, 100)
    val deposited2 = Deposited(accountId2, 200)
    val jan1 = TimeService.timestampFor(1, 2015)
    val feb1 = TimeService.timestampFor(2, 2015)
    val account1Events: MutableList[(Event, Long)] = MutableList((deposited1, jan1), (deposited1, feb1))
    val account2Events: MutableList[(Event, Long)] = MutableList((deposited2, jan1))
    s.events += accountId1 -> account1Events
    s.events += accountId2 -> account2Events

    val results = s.accountEvents(accountId1, feb1)
    results should be (List( (deposited1, jan1) ))
  }

  "adding an event" should "put a timestamp on the event and persist it as the 2nd element in a tuple" in {
    val s = service
    val now = 12345L
    (timeService.currentTimeMillis _).when().returns(now)

    val accountId = makeUUID
    s.add(ExampleEvent(accountId))
    val eventsAndTimestamps = s.accountEventsOfType[ExampleEvent](accountId)
    eventsAndTimestamps.length should be(1)
    eventsAndTimestamps(0)._2 should be(12345L)
  }
}
