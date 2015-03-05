import scala.reflect.runtime.universe._
import scala.collection.mutable.ListBuffer
import scala.reflect._

trait Thing
case class Foo(value: String) extends Thing
case class Bar(value: String) extends Thing

val things = List(
  Foo("1"),
  Bar("2"),
  Foo("3"))

def filterByClass[C: ClassTag](implicit p:(C => Boolean)=((_:C) => true)): List[C] = {
  val byClass:List[C] = things collect { case thing: C => thing }
  byClass.filter(p)
}
val pred = (t:Foo) => t.value == "1"
val filtered = filterByClass[Foo]()(pred)

val filtered2 = filterByClass[Foo]



