import java.util.UUID


import com.bank._

import scala.collection.mutable

implicit val ts = new TimeService()
val es = new EventService()

case class ExampleEvent(accountId: UUID) extends Event

val id = UUID.randomUUID()
es.add(AccountCreated(id, 100))

//es.events(id)

var foo: Map[Int, mutable.MutableList[String]] = Map()

def addFoo(i: Int, s: String) = {
  val newI = foo.getOrElse(i, new mutable.MutableList[String])
  newI += s
  foo += (i -> newI)
}

addFoo(1, "one")
addFoo(1, "agai")

foo.get(1)

