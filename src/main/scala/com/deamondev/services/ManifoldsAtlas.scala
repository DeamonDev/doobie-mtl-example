package com.deamondev.services

import cats.effect.kernel.Async
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres._
import cats.effect.kernel.Resource

case class Manifold(id: Int, name: String, equation: String, eulerChar: Int)

object Errors {
  sealed abstract trait ManifoldError
  final case class ManifoldAlreadyExists(name: String) extends ManifoldError
  final case object ThrowableManifoldError extends ManifoldError
}

case class ManifoldsAtlas[F[+_]](
    transactorR: Resource[F, Transactor[F]]
)(implicit F: Async[F]) {
  import ManifoldsAtlasSql._

  def create(
      name: String,
      equation: String,
      eulerChar: Int
  ): F[Either[Errors.ManifoldError, Manifold]] =
    transactorR.use { transactor =>
      createQuery(name, equation, eulerChar)
        .attemptSomeSqlState[Errors.ManifoldError] { case sqlstate.class23.UNIQUE_VIOLATION =>
          Errors.ManifoldAlreadyExists(name)
        }
        .transact(transactor).recoverWith {
          case e: Throwable =>
            F.pure(Left(Errors.ThrowableManifoldError))
        }
    }
}

object ManifoldsAtlasSql {
  def createQuery(
      name: String,
      equation: String,
      eulerChar: Int
  ): ConnectionIO[Manifold] =
    sql"INSERT INTO algebraic_varieties (name, equation, euler_char) VALUES ($name, $equation, $eulerChar)".update
      .withUniqueGeneratedKeys("id", "name", "equation", "euler_char")
}
