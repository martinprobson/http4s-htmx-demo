package net.martinprobson.demo

import cats.effect.*
import com.comcast.ip4s.{ipv4, port}
import doobie.Transactor
import org.http4s.{HttpRoutes, Request, Response, StaticFile}
import org.http4s.dsl.io.{->, /, GET, Ok, Root}
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.staticcontent.webjarServiceBuilder
import org.http4s.server.Router
import org.http4s.twirl.htmlContentEncoder
import org.http4s.*
import net.martinprobson.demo.ToDo.ID
import net.martinprobson.demo.db.{DBTransactor, DoobieToDoRepository}
import net.martinprobson.demo.config.Config

import scala.concurrent.duration.*
import net.martinprobson.demo.db.{InMemoryToDoRepository, ToDoRepository}

object HtmxDemoServer extends IOApp.Simple with Logging {

  /** This is our main entry point where the code will actually get executed.
    *
    * We provide a transactor (as a Resource) which will be used by Doobie to execute the SQL statements. 
    */
  override def run: IO[Unit] = DBTransactor.transactor.use { xa =>
    program(xa).flatMap(_ => log.info("Program exit"))
  }

  /** Start an Ember server to run our Http App. 
    */
  private def program(xa: Transactor[IO]): IO[Unit] = for {
    _ <- log.info("Program starting....")
    cfg <- Config.report
    _ <- log.debug(s"Config: \n $cfg")
    // toDoRepository <- InMemoryToDoRepository.empty
    toDoRepository <- DoobieToDoRepository(xa)
    _ <- EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(loggerService(httpApp(toDoRepository)).orNotFound)
      .withShutdownTimeout(1.second) // Set to 1 second, so that sbt ~ reStart is quicker
      .withLogger(log)
      .build
      .onFinalize(log.info("Shutdown of EmberServer"))
      .use(_ => IO.never)
  } yield ()

  private def getToDos(toDoRepository: ToDoRepository): IO[List[ToDo]] = for {
    _ <- log.debug("In getToDos")
    todos <- toDoRepository.get()
    _ <- log.debug(s"Got $todos")
  } yield todos

  private def postToDo(request: Request[IO], toDoRepository: ToDoRepository): IO[List[ToDo]] = for {
    todo <- request.as[ToDo]
    _ <- log.debug(s"In postToDo: Got request $todo")
    _ <- toDoRepository.add(todo)
    todos <- toDoRepository.get()
  } yield todos

  private def deleteToDo(id: ID, toDoRepository: ToDoRepository): IO[List[ToDo]] = for {
    _ <- log.debug(s"In deleteToDo: $id")
    _ <- toDoRepository.delete(id)
    _ <- log.debug(s"Deleted: $id")
    todos <- toDoRepository.get()
  } yield todos

  private def toggleToDo(id: ID, toDoRepository: ToDoRepository): IO[List[ToDo]] = for {
    _ <- log.debug(s"In toggleToDo: $id")
    _ <- toDoRepository.toggle(id)
    _ <- log.debug(s"Toggle: $id")
    todos <- toDoRepository.get()
  } yield todos

  /**
   * All our http routes are defined here
   */
  private def toDoService(toDoRepository: ToDoRepository): HttpRoutes[IO] = HttpRoutes
    .of[IO] {
      case PATCH -> Root / "todo" / IntVar(id) =>
        toggleToDo(id, toDoRepository).flatMap { todos => Ok(html.todos(todos)) }
      case DELETE -> Root / "todo" / IntVar(id) =>
        deleteToDo(id, toDoRepository).flatMap { todos => Ok(html.todos(todos)) }
      case GET -> Root =>
        Ok(html.index(new java.util.Date().toInstant.toString))
      case req @ POST -> Root / "todo" / "create" =>
        postToDo(req, toDoRepository).flatMap { todos => Ok(html.todos(todos)) }
      case GET -> Root / "todos" =>
        getToDos(toDoRepository).flatMap(todos => Ok(html.todos(todos)))
      case GET -> Root / "http4s-favicon.svg" =>
        StaticFile.fromResource[IO]("assets/http4s-favicon.svg").getOrElseF(NotFound())
    }

  private val webjars: HttpRoutes[IO] =
    webjarServiceBuilder[IO].toRoutes

  private def httpApp(toDoRepository: ToDoRepository): HttpRoutes[IO] =
    Router.define("/" -> toDoService(toDoRepository))(default = webjars)

}
