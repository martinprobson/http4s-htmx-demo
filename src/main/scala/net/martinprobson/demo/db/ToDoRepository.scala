package net.martinprobson.demo.db

import cats.effect.{IO, Ref}
import net.martinprobson.demo.ToDo
import net.martinprobson.demo.ToDo.*

trait ToDoRepository {
  def toggle(id: ID): IO[Unit]
  def delete(id: ID): IO[Int]
  def add(todo: ToDo): IO[ToDo]
  def get(): IO[List[ToDo]]
  def count(): IO[Long]
}
