import com.bank._
import org.scalatest._
import org.scalamock.scalatest.MockFactory

class AccountServiceSpec extends FlatSpec
with Matchers
with MockFactory {

  def service(eventService:EventService = new EventService(),
              uuidService:UUIDService = new UUIDService()): AccountService = {
    new AccountService(eventService, uuidService)
  }

  def makeUUID =
    java.util.UUID.randomUUID()

  "Creating an account" should "add an AccountCreated event and return it" in {
    val uuid = makeUUID
    val eventServiceMock = mock[EventService]
    val uuidMock = stub[UUIDService]
    (uuidMock.generate _).when().returns(uuid)
    val event = AccountCreated(uuid, "gabe")
    (eventServiceMock.add[Event] _).expects(event).returning(event)

    val result = service(eventServiceMock, uuidMock).createAccount("gabe")
    result should be(event)
  }

  "Depositing money" should "create a Deposited event and return it" in {
    val accountId = makeUUID
    val eventServiceMock = mock[EventService]
    val event = Deposited(accountId, 100)
    (eventServiceMock.add[Event] _).expects(event).returning(event)

    val result = service(eventServiceMock).deposit(accountId, 100)
    result should be(event)
  }
}

