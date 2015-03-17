import java.util.UUID

import com.bank._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, FlatSpec}

class AccountRepoSpec extends FlatSpec
with Matchers
with MockFactory {
  val repo = AccountRepo
  implicit val timeService = stub[TimeService]

  "Creating an account" should "should return it" in {
    val accountAgg : AccountAggregate = repo.createAccount(123)
    accountAgg.overdrawLimit.shouldBe(123)
  }

  "Getting an account aggregate by its id" should "return the aggregate" in {
    val account = repo.createAccount(0)
    val result = repo.getAccount(account.id)
    result.get.shouldBe(account)
  }
}
