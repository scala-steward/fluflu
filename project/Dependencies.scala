import sbt._

object Dependencies {
  val Ver = new {
    val `scala2.12` = "2.12.19"
    val `scala2.13` = "2.13.14"
    val scala3      = "3.4.2"

    val organizeImports = "0.6.0"

    val msgpackJava  = "0.9.8"
    val mess         = "0.3.6"
    val scalaLogging = "3.9.5"
    val logback      = "1.2.3"

    val scalatest = "3.2.19"
    val scalatestplus = new {
      val scalacheck = "3.2.18.0"
      val mockito    = "3.2.18.0"
    }
  }

  def is2_13(v: String): Boolean = CrossVersion.partialVersion(v) match {
    case Some((2, 13)) => true
    case _             => false
  }

  lazy val OrganizeImports = "com.github.liancheng" %% "organize-imports" % Ver.organizeImports

  val Pkg = new {
    lazy val msgpackJava    = "org.msgpack"                 % "msgpack-core"    % Ver.msgpackJava
    lazy val mess           = "com.github.tkrs"            %% "mess-core"       % Ver.mess
    lazy val scalaLogging   = "com.typesafe.scala-logging" %% "scala-logging"   % Ver.scalaLogging
    lazy val logbackClassic = "ch.qos.logback"              % "logback-classic" % Ver.logback

    lazy val scalatest  = "org.scalatest"     %% "scalatest"       % Ver.scalatest
    lazy val scalacheck = "org.scalatestplus" %% "scalacheck-1-17" % Ver.scalatestplus.scalacheck
    lazy val mockito    = "org.scalatestplus" %% "mockito-5-10"    % Ver.scalatestplus.mockito

    lazy val forTest = Seq(scalatest, scalacheck, mockito)
  }
}
