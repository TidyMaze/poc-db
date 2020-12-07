name := "poc-db"

version := "0.1"

scalaVersion := "2.13.4"

libraryDependencies ++= Seq(
  "org.flywaydb" % "flyway-core" % "7.3.1",
  "org.typelevel" %% "cats-effect" % "2.2.0",
  "org.tpolecat" %% "doobie-core" % "0.9.0",
  "org.tpolecat" %% "doobie-hikari" % "0.9.0", // HikariCP transactor.
  "org.tpolecat" %% "doobie-postgres" % "0.9.0", // Postgres driver 42.2.12 + type mappings.
  "org.tpolecat" %% "doobie-scalatest" % "0.9.0" % "test" // ScalaTest support for typechecking statements.
)
