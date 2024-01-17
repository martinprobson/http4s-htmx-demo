package net.martinprobson.demo.db

import cats.effect.{IO, Resource}
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import net.martinprobson.demo.config.Config
import net.martinprobson.demo.config.Config.config
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object DBTransactor {
  def log: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

  /** Setup a HikariTransactor connection pool.
    * @return
    *   A Resource containing a HikariTransactor.
    */
  val transactor: Resource[IO, HikariTransactor[IO]] =
    (for {
      _ <- Resource.eval[IO, Unit](log.info("Setting up transactor"))
      ce <- ExecutionContexts.fixedThreadPool[IO](config.threads)
      xa <- HikariTransactor
        .newHikariTransactor[IO](
          config.driverClassName,
          config.url,
          config.user,
          config.password,
          ce
        )
    } yield xa).onFinalize(log.info("Finalize of transactor"))

}
