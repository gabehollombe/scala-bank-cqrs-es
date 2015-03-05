import com.bank._
import org.scalatest._
import org.scalamock.scalatest.MockFactory

import scala.reflect._
import scala.reflect.runtime.universe._

class EventServiceSpec extends FlatSpec
with Matchers
with MockFactory {

  def service: EventService = {
    new EventService()
  }

  it should "return all events of a given type" in {
    val s = service
    val created = AccountCreated(1, "gabe")
    val deposited = Deposited(1, 100)
    s.events = List(created, deposited)

    val events = s.eventsOfType(classOf[AccountCreated])
    events should be (List(created))
  }
}
