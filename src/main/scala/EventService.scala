package com.bank

import scala.collection.mutable
import scala.collection.mutable._
import scala.reflect.ClassTag
import java.util.{Date, UUID}

trait Event {
  val accountId: UUID
}


case class AccountCreated(accountId: UUID, overdrawLimit: BigDecimal) extends Event
case class Deposited(accountId: UUID, amount: BigDecimal) extends Event
case class Withdrawed(accountId: UUID, amount: BigDecimal) extends Event
case class MonthlyOverdraftFeeCharged(accountId: UUID, amount: BigDecimal, month: Int, year: Int) extends Event
case class YearlyInterestPaid(accountId: UUID, amount: BigDecimal, year: Int) extends Event
case class Transferred(accountId: UUID, amount: BigDecimal, destinationAccountId: UUID) extends Event

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
  var events: Map[UUID, MutableList[(com.bank.Event, Long)]] = Map()

  def add[A <: Event](event: A): Unit = {
    val accountEvents = events.getOrElse(event.accountId, new MutableList[(Event, Long)])
    accountEvents += ((event, timeService.currentTimeMillis))
    events += (event.accountId -> accountEvents)
  }


  def all[C:ClassTag] : List[(C, Long)] = {
    events.values.toList flatMap
      ((accountEvents: MutableList[(Event, Long)]) =>
        accountEvents collect { case (event: C, timestamp: Long) => (event, timestamp)})
  }

  def accountEvents(accountId: UUID, until: Long = Long.MaxValue): List[(Event, Long)] = {
    events.getOrElse(accountId, MutableList()).toList.filter(_._2 < until)
  }

  def accountEventsOfType[C:ClassTag](accountId: UUID): List[(C, Long)] = {
    events.getOrElse(accountId, MutableList()).toList collect { case (event: C, timestamp: Long) => (event, timestamp) }
  }
}
