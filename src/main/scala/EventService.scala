package com.bank

import scala.reflect.api.TypeTags
import scala.reflect.runtime.universe._
import java.util.UUID

abstract class Event
case class AccountCreated(id: UUID, name: String) extends Event
case class Deposited(accountId: UUID, amount: BigDecimal) extends Event

class EventService {
  var events = List[Event]()

  def add[A <: Event](event: A): A =
    event

  def eventsOfType[C](c: C) : List[C] =
    events.filter(e => e.getClass() == c).map(e => e.asInstanceOf[C])
}
