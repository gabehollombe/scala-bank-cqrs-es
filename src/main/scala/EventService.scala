package com.bank

import scala.collection.mutable._
import scala.reflect.ClassTag
import java.util.{Date, UUID}

trait Event {
  var timestamp: Long
}


case class AccountCreated(id: UUID, overdrawLimit: BigDecimal, var timestamp: Long = 0) extends Event
case class Deposited(accountId: UUID, amount: BigDecimal, var timestamp: Long = 0) extends Event
case class Withdrawed(accountId: UUID, amount: BigDecimal, var timestamp: Long = 0) extends Event
case class MonthlyOverdraftFeeCharged(accountId: UUID, amount: BigDecimal, month: Int, year: Int, var timestamp: Long = 0) extends Event

class TimeService {
  def currentTimeMillis = System.currentTimeMillis()
}

object TimeService {

  def timestampFor(month: Int, year: Int) = {
    if (month <= 0 || month > 12) throw new IllegalArgumentException("Month must be between 1 and 12")
    if (year <= 0) throw new IllegalArgumentException("Year must be greater than 0")

    val date = new Date(year, month-1, 1)
    date.getTime
  }

}

class EventService(implicit timeService: TimeService) {
  var events = MutableList[Event]()

  def add[A <: Event](event: A): A = {
    event.timestamp = timeService.currentTimeMillis
    events += event
    event
  }

  def all[C:ClassTag] : MutableList[C] = {
    events.collect { case event: C => event}
  }

  def get[C:ClassTag](predicate:(C => Boolean) = (_:C) => true) : MutableList[C] = {
    all[C].filter(predicate)
  }
}
