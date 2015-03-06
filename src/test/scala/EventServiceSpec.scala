import com.bank._
import org.scalatest._
import org.scalamock.scalatest.MockFactory
import scala.collection.mutable._

class EventServiceSpec extends FlatSpec
with Matchers
with MockFactory {

  case class ExampleEvent(id: Int, var timestamp: Long = 0) extends Event

  implicit val timeService = stub[TimeService]

  def service: EventService = {
    new EventService()
  }
  def makeUUID =
    java.util.UUID.randomUUID()

  "getting events" should "find by type" in {
    val s = service
    val created = AccountCreated(makeUUID, 0)
    val deposited = Deposited(makeUUID, 100)
    s.events = MutableList(created, deposited)

    val events = s.all[AccountCreated]
    events should be (MutableList(created))
    events(0) should be (created)
  }

  "getting events" should "find by type and predicate" in {
    val s = service
    val deposited1 = Deposited(makeUUID, 100)
    val deposited2 = Deposited(makeUUID, 200)
    s.events = MutableList(deposited1, deposited2)

    val predicate = { e:Deposited => e.amount < 150 }
    val events = s.get[Deposited](predicate)
    events should be (MutableList(deposited1))
  }

  "adding an event" should "put a timestamp on the event and return it" in {
    val s = service
    val now = 12345L
    (timeService.currentTimeMillis _).when().returns(now)

    val event = s.add(ExampleEvent(0))
    event.timestamp should be(now)
  }

  "adding an event" should "remember the event" in {
    val s = service
    s.events should be(MutableList())

    val event = ExampleEvent(0)
    s.add(event)
    s.events should be(MutableList(event))
  }
}
