package com.zyx2.timestamp_demo

import java.text.SimpleDateFormat
import java.sql.Timestamp
import java.util.Calendar
import java.time._
import slick.driver.PostgresDriver.api._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

case class CommandLineOption(
  insert: Boolean = false
)

/** Program entrypoint */
object Main extends App {
  val logger = org.slf4j.LoggerFactory.getLogger(getClass)
  val df = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss zzzz")

  var insert: Boolean = false

  val parser = new scopt.OptionParser[CommandLineOption]("Main") {
    opt[Unit]('i', "insert") optional() action { (_, c) =>
      c.copy(insert = true) } text("Insert 2 new rows into tztest table.")
  }

  parser.parse(args, CommandLineOption()) match {
    case Some(opt) =>
      insert = opt.insert
    case None => ???
  }

  val tzIdDefault = ZoneId.systemDefault
  val tzIdUsPacific = ZoneId.of("US/Pacific")
  val tzIdAmericaDenver = ZoneId.of("America/Denver")
  val tzIdAmericaNewYork = ZoneId.of("America/New_York")
  val tzIdAsiaSeoul = ZoneId.of("Asia/Seoul")


  logger.info(s"""Current Time Zone is '$tzIdDefault'""")

  // Insert two different rows using old and new libs.
  if (insert)
    Seq(
      rowUsingCalendar,
      rowUsingJavaTime
    ) map {
      row => {
        logger.info(s"Inserting row: $row")
        TzTestDAO.create(row)
      }
    }

  // Fetch all rows and output to console.
  val buf = scala.collection.mutable.ListBuffer.empty[Seq[String]]
  val labels = scala.collection.mutable.HashMap.empty[String, String]
  val widths = scala.collection.mutable.HashMap.empty[String, Int]

  labels += ("id" -> "Id")
  labels += ("timestamp" -> "Timestamp Without Time Zone")
  labels += ("timestamptz" -> "Timestamp With Time Zone")
  labels += ("description" -> "Wall Clock Time On Insert")

  widths += ("id" -> labels("id").length)
  widths += ("timestamp" -> labels("timestamp").toString.length)
  widths += ("timestamptz" -> labels("timestamptz").toString.length)
  widths += ("description" -> labels("description").length)

  Await.result(TzTestDAO.getAllRows, Duration.Inf) map {
    row => {
      val id: String = s"${row.id.get}"
      val timestamp =  ZonedDateTime.ofInstant(row.timestamp.toInstant, tzIdDefault).toString
      val timestamptz = ZonedDateTime.ofInstant(row.timestamptz.toInstant, tzIdDefault).toString
      val description: String = row.description

      if (widths("id") < id.length)
        widths("id") = id.length

      if (widths("timestamp") < timestamp.length)
        widths("timestamp") = timestamp.length

      if (widths("timestamptz") < timestamptz.length)
        widths("timestamptz") = timestamptz.length

      if (widths("description") < description.length)
        widths("description") = description.length

      buf += Seq(id, timestamp, timestamptz, description)
    }
  }

  // Print headings.
  val headings = Seq("id", "timestamp", "timestamptz", "description")

  buf.length match {
    case 0 => println("*** There are no results to display.")
    case 1 => println("*** There is 1 result to display.")
    case n => println(s"*** There are $n results to display.")
  }

  if (buf.length > 0) {
    dividerOutput(headings, widths)
    print("|")
    headings map {label => print(s""" ${labels(label).padTo(widths(label) + 1, ' ')}|""")}
    println()
    dividerOutput(headings, widths)
  }

  // Print table.
  buf map {
    row => {
      print(s"| ${row(0).padTo(widths("id"), ' ')} |")
      print(s" ${row(1).toString.padTo(widths("timestamp"), ' ')} |")
      print(s" ${row(2).toString.padTo(widths("timestamptz"), ' ')} |")
      println(s" ${row(3).padTo(widths("description") + 1, ' ')}|")
    }
  }

  if (buf.length > 0)
    dividerOutput(headings, widths)

  /** Print heading divider to console.
   *
   *  @param Seq[String] Headings keys.
   *  @param mutable.HashMap[String, Int] Heading key/width map.
   *  @return Unit
   */
  def dividerOutput(headings: Seq[String],
    widths: scala.collection.mutable.HashMap[String,Int]): Unit = {
    print("+")
    headings map {label => print(s"""${"".padTo(widths(label) + 2, '-')}+""")}
    println()
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
    val q = TzTest.sortBy(_.id)
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
