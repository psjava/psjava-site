name := "psjava-site"

version := "1.2-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache
)     

libraryDependencies += "com.google.code.javaparser" % "javaparser" % "1.0.8"

libraryDependencies += "org.psjava" % "psjava" % "0.1.10"

libraryDependencies += "org.json" % "json" % "20140107"

play.Project.playJavaSettings

