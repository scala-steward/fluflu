import sbt._

object Deps {
  val Ver = new {
    val `scala2.11`   = "2.11.12"
    val `scala2.12`   = "2.12.6"
    val `scala2.13`   = "2.13.0-M4"
    val scalafmt      = "1.3.0"
    val shapeless     = "2.3.3"
    val circe         = "0.10.0-M2"
    val scalacheck    = "1.14.0"
    val scalatest     = "3.0.6-SNAP2"
    val monix         = "3.0.0-RC1"
    val scalaLogging  = "3.9.0"
    val logback       = "1.2.3"
    val msgpackJava   = "0.8.16"
    val mockito       = "2.19.1"
    val kindProjector = "0.9.7"
  }

  val Pkg = new {
    lazy val monixReactive  = "io.monix"                   %% "monix-reactive" % Ver.monix
    lazy val scalaLogging   = "com.typesafe.scala-logging" %% "scala-logging"  % Ver.scalaLogging
    lazy val circeCore      = "io.circe"                   %% "circe-core"     % Ver.circe
    lazy val circeGeneric   = "io.circe"                   %% "circe-generic"  % Ver.circe
    lazy val circeParser    = "io.circe"                   %% "circe-parser"   % Ver.circe
    lazy val scalatest      = "org.scalatest"              %% "scalatest"      % Ver.scalatest
    lazy val scalacheck     = "org.scalacheck"             %% "scalacheck"     % Ver.scalacheck
    lazy val msgpackJava    = "org.msgpack"                % "msgpack-core"    % Ver.msgpackJava
    lazy val logbackClassic = "ch.qos.logback"             % "logback-classic" % Ver.logback
    lazy val mockito        = "org.mockito"                % "mockito-core"    % Ver.mockito
    lazy val kindProjector  = "org.spire-math"             %% "kind-projector" % Ver.kindProjector

    lazy val forTest = Seq(scalatest, scalacheck, mockito).map(_ % "test")
  }
}
