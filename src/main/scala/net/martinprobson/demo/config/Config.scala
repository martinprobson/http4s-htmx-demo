package net.martinprobson.demo.config

import cats.Id
import cats.effect.IO
import cats.implicits.*
import japgolly.clearconfig.*

final case class Config(
  threads: Int,
  driverClassName: String,
  url: String,
  user: String,
  password: String
)

object Config {

  private def configSources: ConfigSources[Id] =
    ConfigSource.environment[Id] >
      ConfigSource.propFileOnClasspath[Id]("/application.properties", optional = false) >
      ConfigSource.system[Id]

  private def cfg: ConfigDef[Config] = (
    ConfigDef.need[Int]("threads"),
    ConfigDef.need[String]("driverClassName"),
    ConfigDef.need[String]("url"),
    ConfigDef.need[String]("user"),
    ConfigDef.need[String]("password")
  ).mapN(apply)

  lazy val config: Config = cfg.run(configSources).getOrDie()

  val report: IO[String] = IO(
    cfg.withReport
      .run(configSources)
      .getOrDie()
      ._2
      .mapUnused(_.withoutSources(ConfigSourceName.system, ConfigSourceName.environment))
      .full
  )
}
