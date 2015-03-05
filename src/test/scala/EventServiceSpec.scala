import java.util.UUID

import com.bank._
import org.scalatest._
import org.scalamock.scalatest.MockFactory

class EventServiceSpec extends FlatSpec
with Matchers
with MockFactory {

  def service: EventService = {
    new EventService()
  }
  def makeUUID =
    java.util.UUID.randomUUID()

  it should "return all events of a given type" in {
    val s = service
    val created = AccountCreated(makeUUID, "gabe")
    val deposited = Deposited(makeUUID, 100)
    s.events = List(created, deposited)

    val events = s.eventsOfType(classOf[AccountCreated])
    events should be (List(created))
  }
}
