package net.martinprobson.demo

import ToDo.*
import cats.effect.IO
import org.http4s.{DecodeResult, EntityDecoder, MediaType, UrlForm}

import java.nio.charset.Charset.defaultCharset

case class ToDo(id: ID, description: String, complete: Boolean)

object ToDo {

  /**
   * Make a ToDo from the posted UrlForm
   */
  given EntityDecoder[IO, ToDo] = EntityDecoder[IO, UrlForm]
    .map{ u => u.getFirstOrElse("new-todo", throw new IllegalStateException("No content!"))}
    .map{ desc => ToDo(desc, false)}

  def apply(id: ID, description: String, complete: Boolean): ToDo =
    new ToDo(id, description, complete)
  def apply(description: String, complete: Boolean): ToDo =
    new ToDo(UNASSIGNED_ID, description, complete)

  type ID = Long
  val UNASSIGNED_ID = 0L
}
