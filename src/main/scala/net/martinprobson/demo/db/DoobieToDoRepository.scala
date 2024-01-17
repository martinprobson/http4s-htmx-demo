package net.martinprobson.demo.db

import cats.effect.IO
import cats.free.Free
import doobie.*
import doobie.free.connection
import doobie.free.connection.ConnectionOp
import doobie.implicits.*
import fs2.Stream
import net.martinprobson.demo.Logging
import net.martinprobson.demo.ToDo
import net.martinprobson.demo.ToDo.ID

class DoobieToDoRepository(xa: Transactor[IO]) extends ToDoRepository with Logging {

  override def toggle(id: ID): IO[Unit] = ???

  override def delete(id: ID): IO[Int] = for {
    r <- deleteToDo(id)
    _ <- log.debug(s"Deleted todo with id = $id response = $r")
  } yield r

  override def add(todo: ToDo): IO[ToDo] = for {
    _ <- log.info(s"About to create : $todo")
    todo <- insertToDo(todo)
    _ <- log.info(s"Created user: $todo")
  } yield todo

  override def get(): IO[List[ToDo]] = selectAll.compile.toList

  override def count(): IO[Long] = selectCount.transact(xa)

  private def deleteToDo(id: ID): IO[Int] =
    sql"DELETE FROM todo WHERE id = $id".update.run.transact(xa)

  private def insertToDo(todo: ToDo): IO[ToDo] = (for {
    _ <-
      sql"INSERT INTO todo (description, complete) VALUES (${todo.description},${todo.complete})"
        .update.run
    id <- sql"SELECT last_insert_id()".query[Long].unique
    todo <- Free.pure[ConnectionOp, ToDo](ToDo(id, todo.description, todo.complete))
  } yield todo).transact(xa)

  private def selectAll: Stream[IO, ToDo] =
    sql"SELECT id, description, complete FROM todo".query[ToDo].stream.transact(xa)

  private def selectCount: ConnectionIO[Long] =
    sql"SELECT COUNT(*) FROM todo".query[Long].unique

  def createTable: IO[Int] =
    sql"""
         |create table if not exists todo
         |(
         |    id   int auto_increment
         |        primary key,
         |    description  varchar(100) null,
         |    complete tinyint null
         |         );
         |""".stripMargin.update.run.transact(xa)
}
object DoobieToDoRepository {

  def apply(xa: Transactor[IO]): IO[DoobieToDoRepository] = for {
    toDoRepository <- IO(new DoobieToDoRepository(xa))
    _ <- toDoRepository.createTable
  } yield toDoRepository

}
