def Scala3 = "3.1.3-RC2"

run / fork := true

scalaVersion := Scala3

crossScalaVersions := Seq("2.13.8", Scala3)

scalacOptions ++= {
  if (scalaBinaryVersion.value == "3") {
    Nil
  } else {
    Seq("-Xsource:3")
  }
}

libraryDependencies += {
  if (scalaBinaryVersion.value == "3") {
    "org.scala-lang" %% "scala3-tasty-inspector" % scalaVersion.value
  } else {
    "org.scala-lang" % "scala-reflect" % scalaVersion.value
  }
}
