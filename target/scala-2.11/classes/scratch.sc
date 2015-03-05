import scala.reflect.runtime.universe._

class Top
class Foo(val name: String) extends Top
class Bar(val name: String) extends Top
val f = new Foo("gabe")
def tt[T](thing: T)(implicit tag: TypeTag[T]) =
  tag
tt(new Foo("gabe"))

val events = List[Top](new Foo("f"), new Bar("b"))

def eventsOfType2[T : TypeTag](t: T) : List[Any] =
  events.filter((e: T)(implicit tag: TypeTag[T]) =>

eventsOfType2(typeOf[Bar])

