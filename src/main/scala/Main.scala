import cats.effect.IO
import doobie._
import doobie.implicits._
import org.flywaydb.core.Flyway

import scala.concurrent.ExecutionContext

case class Person(id: Int, name: String)
case class PersonName(name: String)


object Main extends App {
  implicit val cs = IO.contextShift(ExecutionContext.global)
  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", "jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres"
  )

  // Create the Flyway instance and point it to the database
  val flyway: Flyway = Flyway.configure.dataSource("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres").load

  flyway.migrate()

  implicit val read: Read[PersonName] = Read[String].map(PersonName(_))


  val name = "Satan2"

  val dbStuff = for {
    listPersons <- sql"select id, name from person".query[Person].to[List]
    insertPerson <- sql"insert into person values (666, $name)".update.run
    listNames <- sql"select name from person".query[PersonName].to[List]
  } yield (listPersons, listNames)

  val main = for {
    personsAndNames <- dbStuff.transact(xa)
    (persons, names) = personsAndNames
    _ <- IO { println(s"$persons, $names") }
  } yield ()

  main.unsafeRunSync()
}
