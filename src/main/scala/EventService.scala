package com.bank

abstract class Event
class AccountCreated(val accountId: Int) extends Event
class Deposited(val accountId: Int, val amount: BigDecimal) extends Event

trait EventServiceTrait {
//  val events = List[Event]()

  def add[A <: Event](event: A): A =
    event
}
class EventService extends EventServiceTrait
