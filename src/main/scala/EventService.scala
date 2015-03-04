package com.bank

abstract class Event
case class AccountCreated(accountId: Int, name: String) extends Event
case class Deposited(accountId: Int, amount: BigDecimal) extends Event

trait EventServiceTrait {
//  val events = List[Event]()

  def add[A <: Event](event: A): A =
    event

}
class EventService extends EventServiceTrait
