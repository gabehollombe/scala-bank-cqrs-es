//import com.bank._
//import org.scalatest._
//import org.mockito.Mockito._
//import org.scalatest.mock.MockitoSugar
//
//class AccountServiceSpec extends FlatSpec
//  with Matchers
//  with MockitoSugar {
//
//  def service(eventService:EventService = new EventService()): AccountService = {
//    new AccountService(eventService)
//  }
//
//  "Creating an account" should "add an AccountCreated event" in {
//    val eventServiceMock = mock[EventService]
//    service(eventServiceMock).createAccount("gabe")
//    verify(eventServiceMock).add(a [AccountCreated])
//  }
//
//  "Creating an account" should "return the AccountCreated event" in {
//    val event = service().createAccount("gabe")
//    event shouldBe a [AccountCreated]
//  }
//
//  it should "deposit money into an account" in {
//    val accountId = service().createAccount("gabe").accountId
//    val event = service().deposit(accountId, 1.0)
//    event shouldBe a [Deposited]
//  }
//}

import com.bank._
import org.scalatest._
import org.scalamock.scalatest.MockFactory

class AccountServiceSpec extends FlatSpec
with Matchers
with MockFactory {

  def service(eventService:EventServiceTrait = new EventService()): AccountService = {
    new AccountService(eventService)
  }

  "Creating an account" should "add an AccountCreated event" in {
    val eventServiceMock = mock[EventServiceTrait]
    (eventServiceMock.add _).expects(a [AccountCreated])
    service(eventServiceMock).createAccount("gabe")
  }

  "Creating an account" should "return the AccountCreated event" in {
    val event = service().createAccount("gabe")
    event shouldBe a [AccountCreated]
  }

  it should "deposit money into an account" in {
    val accountId = service().createAccount("gabe").accountId
    val event = service().deposit(accountId, 1.0)
    event shouldBe a [Deposited]
  }
}

