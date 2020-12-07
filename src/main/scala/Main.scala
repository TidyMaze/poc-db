import cats.effect.IO
import doobie._
import doobie.implicits._
import org.flywaydb.core.Flyway

import scala.concurrent.ExecutionContext

case class Person(id: Int, name: String)
case class PersonName(name: String)


object Main extends App {
  implicit val cs = IO.contextShift(ExecutionContext.global)

  private val dbUrl = "jdbc:postgresql://localhost:5432/postgres"
  private val dbUser = "postgres"
  private val dbPassword = "postgres"

  // Doobie connection to database
  val xa = Transactor.fromDriverManager[IO]("org.postgresql.Driver", dbUrl, dbUser, dbPassword)

  // Database migration using /src/main/resources/db/migration
  val flyway: Flyway = Flyway.configure.dataSource(dbUrl, dbUser, dbPassword).load
  flyway.migrate()

  // Custom read for some columns in Person table
  implicit val read: Read[PersonName] = Read[String].map(PersonName)

  // sql interpolation
  val name = "Satan2"

  // bunch of actions in same transaction
  val dbStuff = for {
    // read Person to case class using automatic derivation of the Person
    listPersons <- sql"select id, name from person".query[Person].to[List]

    // insert a person in same transaction
    insertPerson <- sql"insert into person values (666, $name)".update.run

    // list only name column using custom read
    listNames <- sql"select name from person".query[PersonName].to[List]
  } yield (listPersons, listNames)

  // merging database IO with other IOs (HTTP, GRPC ...)
  val main = for {
    personsAndNames <- dbStuff.transact(xa)
    (persons, names) = personsAndNames
    _ <- IO { println(s"$persons, $names") }
  } yield ()

  // run the entire IO
  main.unsafeRunSync()
}
