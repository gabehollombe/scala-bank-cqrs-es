package com.bank

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

trait Event
case class AccountCreated(accountId: Int, name: String) extends Event
case class Deposited(accountId: Int, amount: BigDecimal) extends Event

class EventService {
  var events = List[Event]()

  def add[A <: Event](event: A): A =
    event

  def eventsOfType[T <: Event](t: Class[T]) : List[T] =
    //NOTE: this implementation feels dirty but it works
    events.filter(e => e.getClass() == t).map(e => e.asInstanceOf[T])
}
