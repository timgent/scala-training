package exercises.modelling

object ModellingExercise {

  /**
   * What is bad about this badRunReport method and the BadReportRunner.runReport method it calls?
   * What could you do to fix the issues with this? Have a go and try to implement
   * @param arguments - a map of arguments that were passed from the command line. Possible keys and values are:
   *                  reportType - Either A, B, or C
   *                  startDate - A date in the format YYYY-MM-dd
   *                  endDate - A date in the format YYYY-MM-dd
   *                  db - A string representing a database
   *                  table - A string representing a table
   */
  def badRunReport(arguments: Map[String, String]): ReportOutput = {
    BadReportRunner.runReport(
      arguments("reportType"),
      arguments("endDate").toInt,
      arguments("db"),
      arguments("table")
    )
  }

  def runReport(arguments: Map[String, String]): ReportOutput = {
    ???
  }
}

object BadReportRunner {
  def runReport(reportType: String, startDate: Int, db: String, table: String): ReportOutput = ???
}

object ReportRunner {
  def runReport(): ReportOutput = ???
}

case class ReportOutput(data: List[ReportData], description: String)
case class ReportData(number: Int, string: String)