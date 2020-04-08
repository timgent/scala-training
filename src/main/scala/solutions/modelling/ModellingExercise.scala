package solutions.modelling

import java.time.LocalDate

import solutions.modelling.ReportRunError.{ErrorOr, InvalidArgumentError, MissingArgumentError}

import scala.util.Try

/**
 * This solution is more typesafe and functional compared to the original bad implementation. However it still has some
 * undesirable behaviour, can you spot it?
 * The issue is that if one of the provided arguments has an issue it will report only that error. A user might end up
 * running the application a number of times before they pick up the mistakes with all of the arguments! Wouldn't it
 * be better to use something that gives back all of the errors straight away? Cats has some handy utilities for
 * handling such a case, which you can check out here:
 * https://typelevel.org/cats/datatypes/validated.html
 */
object ModellingExercise {
  val reportTypeField = "reportType"
  val endDateField = "endDate"
  val dbField = "db"
  val tableField = "table"

  def runReport(arguments: Map[String, String]): ErrorOr[ReportOutput] = {
    def getArgument(argumentName: String): ErrorOr[String] =
      arguments.get(argumentName).toRight(MissingArgumentError(argumentName))

    for {
      reportTypeStr <- getArgument(reportTypeField)
      reportType <- ReportType.fromString(reportTypeStr)
      endDateStr <- getArgument(endDateField)
      endDate <- Try(LocalDate.parse(endDateStr)).toEither
        .left.map(e => InvalidArgumentError(endDateField, endDateStr, "Could not parse given date", Some(e)))
      dbStr <- getArgument(dbField)
      tableStr <- getArgument(tableField)
      tableName <- TableName[ReportData](dbStr, tableStr)
      reportResult <- ReportRunner.runReport(reportType, endDate, tableName)
    } yield reportResult
  }
}

case class TableName[T](db: String, table: String) {
  def readTable: List[T] = ???
}

object TableName {

  import ModellingExercise.{dbField, tableField}

  def apply[T](dbStr: String, tableStr: String): ErrorOr[TableName[T]] = for {
    db <- validateDb(dbStr)
    table <- validateTable(tableStr)
  } yield new TableName(db, table)

  private def validateDb(dbStr: String): ErrorOr[String] = if (dbStr.contains(" ")) {
    Left(InvalidArgumentError(dbField, dbStr, "Database name should not contain a space"))
  } else {
    Right(dbStr)
  }

  private def validateTable(tableStr: String): ErrorOr[String] = if (tableStr.contains(" ")) {
    Left(InvalidArgumentError(dbField, tableStr, "Table name should not contain a space"))
  } else {
    Right(tableStr)
  }
}

object ReportRunner {
  def runReport(reportType: ReportType, startDate: LocalDate,
                tableName: TableName[ReportData]): ErrorOr[ReportOutput] = ???
}

case class ReportOutput(data: List[ReportData], description: String)

case class ReportData(number: Int, string: String)


sealed trait ReportType {
  def str: String
}

object ReportType {
  def fromString(str: String): ErrorOr[ReportType] = str match {
    case ReportTypeA.str => Right(ReportTypeA)
    case ReportTypeB.str => Right(ReportTypeB)
    case ReportTypeC.str => Right(ReportTypeC)
    case invalidStr => Left(InvalidArgumentError(ModellingExercise.reportTypeField, invalidStr, s"No such report type $invalidStr"))
  }

  case object ReportTypeA extends ReportType {
    override val str: String = "A"
  }

  case object ReportTypeB extends ReportType {
    override val str: String = "B"
  }

  case object ReportTypeC extends ReportType {
    override val str: String = "C"
  }

}

sealed trait ReportRunError {
  def err: String
}

object ReportRunError {
  type ErrorOr[T] = Either[ReportRunError, T]

  case class MissingArgumentError(field: String) extends ReportRunError {
    override def err: String = s"argument $field was missing"
  }

  case class InvalidArgumentError(field: String, value: String, errorDescription: String,
                                  exception: Option[Throwable] = None) extends ReportRunError {
    override def err: String = s"Could not parse field $field with value $value. $errorDescription"
  }

  case class NoSuchTable(table: String, db: String) extends ReportRunError {
    override def err: String = s"Table $table was not found in database $db"
  }

}