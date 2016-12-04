package com.zyx2.tztest

import java.io.File
import java.text.SimpleDateFormat
import scala.math.BigDecimal
import java.sql.Timestamp
import scala.util.{Try, Success, Failure}
import java.util.NoSuchElementException
import java.util.Calendar
import java.time._
import slick.driver.PostgresDriver.api._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global


/** Program entrypoint */
object Main extends App {
  val df = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss zzzz")
  val tzIdDefault = ZoneId.systemDefault
  val tzIdUsPacific = ZoneId.of("US/Pacific")
  val tzIdAmericaDenver = ZoneId.of("America/Denver")
  val tzIdAmericaNewYork = ZoneId.of("America/New_York")
  val tzIdAsiaSeoul = ZoneId.of("Asia/Seoul")

  // Grab two different rows using old and new.
  Seq(
    rowUsingCalendar,
    rowUsingJavaTime
  ) map {
    row => TzTestDAO.create(row)
  }

  // Fetch all rows and output to console.
  Await.result(TzTestDAO.getAllRows, Duration.Inf) map {
    row => {
      val id: String = f"${row.id.get}%03d"
      val timestamp =  ZonedDateTime.ofInstant(row.timestamp.toInstant, tzIdDefault)
      val timestamptz = ZonedDateTime.ofInstant(row.timestamptz.toInstant, tzIdDefault)
      val description: String = row.description

      print(f"$id%-10s")
      print(f"${timestamp.toString}%-60s")
      print(f"${timestamptz.toString}%-60s")
      println(description)
    }
  }

  /** Create row using java.util.Calendar */
  def rowUsingCalendar: TzTestRow = {
    val now = Calendar.getInstance().getTime
    val ts = new Timestamp(now.getTime())
    TzTestRow(ts, ts, s"${df.format(ts)} ($tzIdDefault)")
  }

  /** Create row using java.time */
  def rowUsingJavaTime: TzTestRow = {
    val now = Timestamp.from(ZonedDateTime.now(tzIdDefault).toInstant)
    TzTestRow(now, now, s"${df.format(now)} ($tzIdDefault)")
  }
}

/** TzTest row definition. `id' column is primary key and is optional so
 *  PostgreSQL will autoincrement and we don't need to specify it. */
case class TzTestRow(
  timestamp: Timestamp,
  timestamptz: Timestamp,
  description: String,
  id: Option[Int] = None
)

/** Defines tztest table. */
class TzTestTable(tag: Tag) extends Table[TzTestRow](tag, "tztest") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def timestamp = column[Timestamp]("timestamp")
  def timestamptz = column[Timestamp]("timestamptz")
  def description = column[String]("description")
  def * = (timestamp, timestamptz, description, id.?) <> (TzTestRow.tupled, TzTestRow.unapply)
}

/** DB query methods. */
object TzTestDAO  {
  val db = Database.forConfig("database")
  val TzTest = TableQuery[TzTestTable]

  /** Get all rows from `tztest' table.
   *
   *  @return Future[Seq[TzTestRow]] all rows.
   */
  def getAllRows: Future[Seq[TzTestRow]] = {
    val q = TzTest
    val action = q.result
    val result: Future[Seq[TzTestRow]] = db.run(action)
    result
  }

  /** Insert new row in `tztest` table.
   *
   *  @param TzTestRow New row case class.
   *  @return Int Row ID inserted.
   */
  def create(row: TzTestRow): Int = {
    Await.result(db.run(TzTest returning TzTest.map(_.id) += row), Duration.Inf)
  }
}
