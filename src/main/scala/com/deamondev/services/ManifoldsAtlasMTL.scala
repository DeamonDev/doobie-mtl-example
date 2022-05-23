package com.deamondev.services

import cats.effect.kernel.Resource
import doobie._
import doobie.implicits._
import cats.implicits._
import cats.effect.kernel.Async
import cats.MonadError
import cats.mtl.Raise
import doobie.postgres._
import cats.Monad
import cats.mtl.implicits._

case class ManifoldsAtlasMTL[F[_]: Monad](
    transactorR: Resource[F, Transactor[F]]
)(implicit F: Async[F]) {
  import ManifoldsAtlasSql._
  import Errors._

  def create(
      name: String,
      equation: String,
      eulerChar: Int
  )(implicit me: MonadError[F, Throwable], fr: Raise[F, ManifoldError]): F[Manifold] =
    transactorR.use { transactor =>
      createQuery(name, equation, eulerChar)
        .attemptSomeSqlState[ManifoldError] {
          case sqlstate.class23.UNIQUE_VIOLATION =>
            ManifoldAlreadyExists(name)
        }.transact(transactor).flatMap[Manifold] {
          case Left(e) =>
            fr.raise(e)
          case Right(mfld) =>
            F.pure(mfld)
        }.recoverWith {
          case e: Throwable =>
            me.raiseError(new RuntimeException("The DB cannot be reached"))
        }
    }
}
