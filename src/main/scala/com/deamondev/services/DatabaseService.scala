package com.deamondev.services

import cats.effect.kernel.Resource
import doobie.util.transactor._
import cats.effect.kernel.Async
import cats.implicits._

trait DatabaseService[F[_]] {
  def getTransactorResource: F[Resource[F, Transactor[F]]]
}

object DatabaseService {
  private def prepareTransactor[F[_]: Async]: Transactor[F] =
    Transactor.fromDriverManager[F](
      "org.postgresql.Driver",
      "jdbc:postgresql:manifolds_atlas",
      "postgres",
      ""
    )

  def make[F[_]: Async]: F[DatabaseService[F]] =
    new DatabaseService[F] {
      override def getTransactorResource: F[Resource[F, Transactor[F]]] =
        Async[F].pure(
          Resource.make[F, Transactor[F]](prepareTransactor.pure[F])(t =>
            Async[F].pure(())
          )
        )
    }.pure[F]
}
