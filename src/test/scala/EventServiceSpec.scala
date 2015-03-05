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

  "getting events" should "find by type" in {
    val s = service
    val created = AccountCreated(makeUUID, "gabe")
    val deposited = Deposited(makeUUID, 100)
    s.events = List(created, deposited)

    val events = s.all[AccountCreated]
    events should be (List(created))
    events(0).name should be("gabe")
  }

  "getting events" should "find by type and predicate" in {
    val s = service
    val deposited1 = Deposited(makeUUID, 100)
    val deposited2 = Deposited(makeUUID, 200)
    s.events = List(deposited1, deposited2)

    val predicate = { e:Deposited => e.amount < 150 }
    val events = s.get[Deposited](predicate)
    events should be (List(deposited1))
  }
}
