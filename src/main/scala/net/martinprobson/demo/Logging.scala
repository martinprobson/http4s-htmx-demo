package net.martinprobson.demo

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.server.middleware.Logger
import org.typelevel.log4cats.{LoggerFactory, LoggerName, SelfAwareStructuredLogger}
import org.typelevel.log4cats.slf4j.Slf4jFactory

trait Logging {
  given loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

  def log: SelfAwareStructuredLogger[IO] =
    loggerFactory.getLogger(LoggerName(getClass.getName.stripSuffix("$")))

  val loggerService: HttpRoutes[IO] => HttpRoutes[IO] = Logger.httpRoutes[IO](
    logHeaders = false,
    logBody = false,
    redactHeadersWhen = _ => false,
    logAction = Some((msg: String) => log.debug(msg))
  )
}
