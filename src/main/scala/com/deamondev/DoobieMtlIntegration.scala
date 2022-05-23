package com.deamondev

import cats.effect.IOApp
import cats.effect.IO
import com.deamondev.services.DatabaseService
import doobie._
import doobie.implicits._
import cats._
import cats.effect._
import cats.implicits._
import com.deamondev.services.ManifoldsAtlas
import com.deamondev.services.Errors
import com.deamondev.services.Manifold
import com.deamondev.services.ManifoldsAtlasMTL
import cats.mtl._
import cats.mtl.implicits._
import cats.data.EitherT
import Errors._

object DoobieMtlIntegration extends IOApp.Simple {

  val program: IO[Unit] =
    for {
      dbService <- DatabaseService.make[IO]
      transactorR <- dbService.getTransactorResource

      mfldE <- ManifoldsAtlas[IO](transactorR).create(
        "double point",
        "x^2=0",
        0
      )
      _ <- mfldE match {
        case Left(e) =>
          e match {
            case ManifoldAlreadyExists(name) =>
              IO.println(s"sorry, manifold $name already exist...")
            case ThrowableManifoldError =>
              IO.println("throwable manifold error :)")
          }
        case Right(mfld) =>
          IO.println(s"Hurray! We have new mfld ${mfld.name}")
      }
    } yield ()

  type F[A] = EitherT[IO, ManifoldError, A]

  val programMTL: F[Unit] =
    for {
      dbService <- DatabaseService.make[F]
      transactorR <- dbService.getTransactorResource
      mfld <- ManifoldsAtlasMTL[F](transactorR)
        .create("triple point", "x^3=0", 0)
        .handleWith[ManifoldError] { case e @ ManifoldAlreadyExists(name) =>
          Sync[F].delay(println(s"Manifold $name already exists.")) *> Manifold(
            0,
            "x",
            "x",
            0
          ).pure[F]
        }
        .recoverWith { case e: Throwable =>
          Sync[F].delay(println("Something went terribly wrong!")) *> Manifold(
            0,
            "x",
            "x",
            0
          ).pure[F]
        }
    } yield ()

  override def run: IO[Unit] = programMTL.value.flatMap {
    case Left(err)     => IO(println(err))
    case Right(result) => IO(println(result))
  }
}
