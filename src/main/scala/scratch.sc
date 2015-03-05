import scala.reflect.runtime.universe._
import scala.collection.mutable.ListBuffer
import scala.reflect._

trait Thing
case class Foo(value: String) extends Thing
case class Bar(value: String) extends Thing

def filterByClass[C: ClassTag](things: List[Thing]): List[C] = {
  things collect { case thing: C => thing }
}
val things = List(
  Foo("1"),
  Bar("2"),
  Foo("3"))

val filtered = filterByClass[Foo](things)
