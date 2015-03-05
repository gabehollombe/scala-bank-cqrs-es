package com.bank

import scala.reflect.ClassTag
import scala.reflect.api.TypeTags
import scala.reflect.runtime.universe._
import java.util.UUID

abstract class Event
case class AccountCreated(id: UUID, name: String) extends Event
case class Deposited(accountId: UUID, amount: BigDecimal) extends Event
case class Withdrawed(accountId: UUID, amount: BigDecimal) extends Event

class EventService {
  var events = List[Event]()

  def add[A <: Event](event: A): A =
    event

  def all[C:ClassTag] : List[C] = {
    events.collect { case event: C => event}
  }

  def get[C:ClassTag](predicate:(C => Boolean) = (_:C) => true) : List[C] = {
    all[C].filter(predicate)
  }
}
