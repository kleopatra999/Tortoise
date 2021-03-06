// (C) Uri Wilensky. https://github.com/NetLogo/Tortoise

package org.nlogo.tortoise

import
  org.scalatest.{ Tag, BeforeAndAfterAll, exceptions },
    exceptions.TestPendingException

import
  org.nlogo.headless.test.{ AbstractFixture, Command, Finder, LanguageTest, NormalMode, CommandTests, TestMode, ReporterTests }

import
  org.nlogo.tortoise.tags.{ SlowTest => TortoiseSlowTag, LanguageTest => TortoiseLanguageTag}

private[tortoise] trait TortoiseFinder extends Finder with BeforeAndAfterAll with BrowserReporter with TestLogger {

  protected def freebies: Map[String, String]

  override def shouldRun(t: LanguageTest, mode: TestMode) =
    mode == NormalMode && super.shouldRun(t, mode)

  def genFixture(name: String): AbstractFixture = new TortoiseFixture(name, nashorn, notImplemented) {
    override def checkResult(mode: TestMode, reporter: String, expectedResult: String, actualResult: AnyRef): Unit = {
      annotatePrevious(s"""NetLogo reporter for: $reporter
                          |expected result: $expectedResult
                          |actualResult: $actualResult""".stripMargin)
      super.checkResult(mode, reporter, expectedResult, actualResult)
    }

    override def runCommand(command: Command, mode: TestMode): Unit = {
      annotate(s"NetLogo generated code for: ${command.command}")
      super.runCommand(command, mode)
    }
  }

  override def runTest(t: LanguageTest, mode: TestMode): Unit =
    loggingFailures(t.suiteName, t.testName, { super.runTest(t, mode) })

  override def test(name: String, otherTags: Tag*)(testFun: => Unit) =
    super.test(name, (Seq(TortoiseLanguageTag) ++ otherTags):_*)(testFun)

  override def withFixture[T](name: String)(body: (AbstractFixture) => T): T =
    freebies.get(name.stripSuffix(" (NormalMode)")) match {
      case None =>
        body(genFixture(name))
      case Some(excuse) =>
        try body(genFixture(name))
        catch {
          case _: TestPendingException => // ignore; we'll hit the fail() below
          case ex: Exception =>
            val message =
              if (excuse.contains("ASSUMES OPTIMIZATION"))
                excuse
              else
                s"$ex: LAME EXCUSE: $excuse"
            notImplemented(message)
        }
        fail(s"LAME EXCUSE WASN'T NEEDED: $excuse")
    }

  protected def notImplemented(s: String): Nothing = {
    info(s)
    throw new TestPendingException
  }

}

class TestReporters extends ReporterTests with TortoiseFinder {
  import Freebies._
  override val freebies = Map(
    "Misc::Version" -> "Assumes JVM NetLogo version numbers"
  ) ++ evalNotSupportedReporters ++ incErrorDetectReporters
}

class TestCommands extends CommandTests with TortoiseFinder {
  import Freebies._
  override val freebies = Map[String, String](
    // requires handling of non-local exit (see in JVM NetLogo: `NonLocalExit`, `_report`, `_foreach`, `_run`)
    "Every::EveryLosesScope"  -> "NetLogo Web does not support distinct jobs"
  ) ++ incErrorDetectCommands ++ emptyInitBlockCommands ++
       evalNotSupportedCommands ++ cmdTaskRepMismatchCommands ++
       lameCommands
}

private[tortoise] object Freebies {

  def incErrorDetectCommands     = asFreebieMap(incErrorDetectCommandNames,     incErrorDetectStr)
  def emptyInitBlockCommands     = asFreebieMap(emptyInitBlockCommandNames,     emptyInitBlockStr)
  def evalNotSupportedCommands   = asFreebieMap(evalNotSupportedCommandNames,   evalNotSupportedStr)
  def cmdTaskRepMismatchCommands = asFreebieMap(cmdTaskRepMismatchCommandNames, cmdTaskRepMismatchStr)
  def lameCommands               = asFreebieMap(lameCommandNames,               lameCommandStr)

  def incErrorDetectReporters    = asFreebieMap(incErrorDetectReporterNames,    incErrorDetectStr)
  def evalNotSupportedReporters  = asFreebieMap(evalNotSupportedReporterNames,  evalNotSupportedStr)

  private def asFreebieMap(names: Seq[String], msg: String) = names.map(_ -> msg).toMap

  // Significant: Requires the optimizer to be turned on
  private val emptyInitBlockStr = "ASSUMES OPTIMIZATION: empty init block"
  private val emptyInitBlockCommandNames = Seq(
    "Death::TurtleDiesWhileIteratingOverItsSet",
    "Interaction::Interaction3b1",
    "Interaction::Interaction3b2",
    "RandomOrderInitialization::TestRandomOrderInitializationCreateLinksFrom",
    "RandomOrderInitialization::TestRandomOrderInitializationCreateLinksTo",
    "RandomOrderInitialization::TestRandomOrderInitializationCreateLinksWith",
    "TurtlesHere::TurtlesHereCheckOrder1",
    "TurtlesHere::TurtlesHereCheckOrder2",
    "TurtlesHere::TurtlesHereCheckOrder3",
    "TurtlesHere::TurtlesHereCheckOrder4"
  )

  private val incErrorDetectStr = "Tortoise error detection and reporting not complete"
  private val incErrorDetectReporterNames = Seq(
    "Lists::Lput5",
    "Lists::ListFirst1",
    "Lists::ListReplaceIt2",
    "Lists::ReduceEmpty",
    "Lists::MapNotEnoughInputs",
    "Lists::ListItem1",
    "Lists::ListItem2",
    "Lists::ListLast1",
    "Lists::ListLength3",
    "Lists::ListRemoveItem4",
    "Lists::ListRemoveItem5",
    "Lists::ListRemoveItem6",
    "Lists::ListReplItem2",
    "Lists::ListReplItem3",
    "Lists::ListButFirst3",
    "Lists::ListButLast3",
    "Lists::ListSubList6",
    "Lists::ListSubList8",
    "Lists::ListSubList9",
    "Lists::ListSubList12",
    "Lists::SortBy5",
    "Numbers::Sqrt1",
    "Numbers::Sqrt4",
    "Numbers::Atan4",
    "Numbers::Exponentiation3",
    "Numbers::Log5",
    "Numbers::Log6",
    "Numbers::Max1",
    "Numbers::Min1",
    "Numbers::Mean1",
    "Numbers::Variance1",
    "Numbers::Variance2",
    "RunResult::RunResult4",
    "RunResult::RunResult5",
    "Strings::StrButFirst2",
    "Strings::StrButLast2",
    "Strings::StrRemoveItem4",
    "Strings::StrRemoveItem5",
    "Strings::StrRemoveItem6"
  )
  private val incErrorDetectCommandNames = Seq(
    "Agentsets::Agentsets2",
    "Agentsets::Agentsets3",
    "Agentsets::Agentsets4",
    "Agentsets::LinkAgentsetDeadLinks",
    "AgentsetBuilding::TurtleSet",
    "AgentsetBuilding::PatchSet2",
    "AgentsetBuilding::LinkSet",
    "AnyAll::All5",
    "Ask::AskAllTurtles",
    "Ask::AskAllPatches",
    "Breeds::SetBreedToNonBreed",
    "CommandTasks::*WrongTypeOfTask1",
    "CommandTasks::WrongTypeOfTask2",
    "CommandTasks::NotEnoughInputs",
    "CommandTasks::NotEnoughInputsForeach",
    "ComparingAgents::ComparingLinks",
    "Death::DeadTurtles1",
    "Death::DeadTurtles2",
    "Death::DeadTurtles5",
    "Death::DeadTurtles6",
    "Face::FaceAgentset",
    "Interaction::Interaction5",
    "Interaction::Interaction13",
    "Interaction::PatchTriesTurtleReporter",
    "Links::LinksNotAllowed",
    "Links::LinkNotAllowed",
    "Links::LinkCreationTypeChecking",
    "Lists::RemoveBug997FirstArgMustBeStringIfSecondArgIsString",
    "Lists::FilterTypeError",
    "Math::CatchNumbersOutsideDoubleRangeOfIntegers",
    "MoveTo::MoveTo",
    "Patch::SetVariableRuntime",
    "RGB::PatchesRGBColor",
    "RGB::TurtlesRGBColor",
    "RGB::LinksRGBColor",
    "Random::RandomOneOfWithLists",
    "Random::RandomNOfWithLists",
    "Random::OneOfWithAgentSets",
    "Random::RejectBadSeeds",
    "ReporterTasks::NotEnoughInputs",
    "Sort::SortingTypeErrors",
    "Sort::sort-by-catches-java-7-general-contract-violation-error",
    "Sort::sort-on-rejects-mixed-types",
    "Turtles::Turtles1a",
    "TurtlesOn::TurtlesOn1",
    "TypeChecking::AgentClassChecking1",
    "TypeChecking::AgentClassChecking3a",
    "TypeChecking::AgentClassChecking3b"
    )

  // perhaps never to be supported
  private val evalNotSupportedStr = "run/runresult on strings not supported"
  private val evalNotSupportedReporterNames = Seq(
    "RunResult::RunResult1",
    "RunResult::RunResult2",
    "RunResult::RunResult3"
  )
  private val evalNotSupportedCommandNames = Seq(
    "ControlStructures::Run1",
    "ControlStructures::Run2",
    "ControlStructures::Run3",
    "ControlStructures::Run4",
    "ControlStructures::Run5",
    "ControlStructures::Run6",
    "ControlStructures::Run7",
    "ControlStructures::Run8",
    "Run::LuisIzquierdoRun1",
    "Run::LuisIzquierdoRun2",
    "Run::LuisIzquierdoRunResult1",
    "Run::LuisIzquierdoRunResult2",
    "Run::run-evaluate-string-input-only-once"
  )

  // requires Tortoise compiler changes
  private val cmdTaskRepMismatchStr = "command task string representation doesn't match"
  private val cmdTaskRepMismatchCommandNames = Seq(
    "CommandTasks::*ToString3",
    "CommandTasks::*ToString4",
    "CommandTasks::*ToString5",
    "CommandTasks::*ToString6"
  )

  private val lameCommandStr = "This test is LAME!"
  private val lameCommandNames = Seq(
    "UserReporters::UserReporters"
  )

}
