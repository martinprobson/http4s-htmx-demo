package net.martinprobson.demo.db

import cats.effect.{IO, Ref}
import net.martinprobson.demo.Logging
import net.martinprobson.demo.ToDo
import net.martinprobson.demo.ToDo.ID

import scala.collection.immutable.SortedMap

class InMemoryToDoRepository(db: Ref[IO, SortedMap[ID, ToDo]], counter: Ref[IO, Long])
    extends ToDoRepository
    with Logging {

  override def toggle(id: ID): IO[Int] = for {
    _ <- log.debug(s"About to toggle todo: $id")
    _ <- db.update(todos => todos + (id -> ToDo(id, todos(id).description, !todos(id).complete)))
  } yield 1

  override def delete(id: ID): IO[Int] = for {
    _ <- log.debug(s"About to delete todo: $id")
    _ <- db.update(todos => todos.filterNot((key, _) => key == id))
  } yield 1

  override def add(todo: ToDo): IO[ToDo] = for {
    id <- counter.modify(x => (x + 1, x + 1))
    todo <- IO(ToDo(id, todo.description, todo.complete))
    _ <- db.update(todos => todos.updated(key = id, value = todo))
    _ <- log.debug(s"Created todo: $todo with id = $id")
  } yield todo

  override def get(): IO[List[ToDo]] = db.get.map { todos =>
    todos.map { case (id, todo) => ToDo(id, todo.description, todo.complete) }.toList
  }

  override def count(): IO[Long] = db.get.flatMap { todos => IO(todos.size.toLong) }
}
object InMemoryToDoRepository {

  def empty: IO[ToDoRepository] = for {
    db <- Ref[IO].of(SortedMap.empty[ID, ToDo])
    counter <- Ref[IO].of(0L)
  } yield new InMemoryToDoRepository(db, counter)
}
