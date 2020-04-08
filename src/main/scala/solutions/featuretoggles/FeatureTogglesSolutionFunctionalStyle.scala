package solutions.featuretoggles

import java.time.LocalDate

import exercises.featuretoggles.{DateRange, DominantPatientTherapy, PatientTherapy}

object FeatureTogglesSolutionFunctionalStyle extends App {

  val useMostDaysDominantTherapy = args(4).toBoolean
  val endDate = LocalDate.parse(args(3))
  val startDate = LocalDate.parse(args(2))
  val outputTable = args(1)
  val inputTable = args(0)
  val dominantTherapyStrategy = if (useMostDaysDominantTherapy)
    DominantTherapyStrategy.MostTotalDays
  else
    DominantTherapyStrategy.LongestStreak

  runJob(inputTable, outputTable, DateRange(startDate, endDate), dominantTherapyStrategy)

  /**
   *
   * @param inputTable
   * @param outputTable
   */
  def runJob(inputTable: String, outputTable: String, periodOfInterest: DateRange,
             dominantTherapyStrategy: DominantTherapyStrategy): Unit = {
    val inputPatientTherapies = readPatientTherapies(inputTable)
    val dominantPatientTherapies = pickDominantTherapyPerPatient(inputPatientTherapies, periodOfInterest, dominantTherapyStrategy)
    writeTable(dominantPatientTherapies, outputTable)
  }

  /**
   * Picks the dominant therapy per patient by:
   * a) Gets the therapies that are in the period of interest
   * b) Updates the period and durations of those therapies if they extend beyond the period of interest
   * c) Finds the therapy which had the longest duration in a single entry
   *
   * @param patientTherapies
   * @param periodOfInterest
   * @return
   */
  def pickDominantTherapyPerPatient(patientTherapies: List[PatientTherapy], periodOfInterest: DateRange,
                                    dominantTherapyStrategy: DominantTherapyStrategy): List[DominantPatientTherapy] = {
    patientTherapies
      .filter(patientTherapy => periodOfInterest.contains(patientTherapy.therapyPeriod))
      .map(_.adjustDateRangeToNotExtendBeyondPeriod(periodOfInterest))
      .groupBy(_.patientId).values
      .map { patientTherapies => dominantTherapyStrategy.pickDominantTherapy(patientTherapies) }.toList
  }

  /**
   * A dummy implementation that just prints out the data instead of writing it out to a table
   *
   * @param data      - a List of any type
   * @param tableName - the table to write the data to
   */
  private def writeTable(data: List[_], tableName: String): Unit = {
    println(data)
  }

  /**
   * Dummy implementation here instead of reading a table
   *
   * @param tableName - name of the table to read
   * @return - a list of patient therapies
   */
  private def readPatientTherapies(tableName: String): List[PatientTherapy] = List(
    PatientTherapy(1, "therapyA", 5, DateRange(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 6))),
    PatientTherapy(1, "therapyB", 4, DateRange(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 5))),
    PatientTherapy(1, "therapyB", 3, DateRange(LocalDate.of(2019, 1, 9), LocalDate.of(2019, 1, 13))),
  )
}

sealed trait DominantTherapyStrategy {
  def pickDominantTherapy(therapiesForSinglePatient: List[PatientTherapy]): DominantPatientTherapy
}

object DominantTherapyStrategy {

  object LongestStreak extends DominantTherapyStrategy {
    override def pickDominantTherapy(therapiesForSinglePatient: List[PatientTherapy]): DominantPatientTherapy = {
      val dominantTherapy = therapiesForSinglePatient.maxBy(_.therapyDuration)
      DominantPatientTherapy(dominantTherapy.patientId, dominantTherapy.therapy)
    }
  }

  object MostTotalDays extends DominantTherapyStrategy {
    override def pickDominantTherapy(therapiesForSinglePatient: List[PatientTherapy]): DominantPatientTherapy = {
      val ((patientId, therapy), _) = therapiesForSinglePatient
        .groupBy(therapy => (therapy.patientId, therapy.therapy))
        .view.mapValues(_.map(_.therapyDuration).sum)
        .maxBy{ case ((_, _), summedDuration) => summedDuration}
      DominantPatientTherapy(patientId, therapy)
    }
  }

}