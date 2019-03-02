package workshop.classTwo

import cats._
import cats.effect.Sync
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl

import scala.language.higherKinds

class UserResource[F[_] : Monad : Sync](R: UserRepository[F]) extends Http4sDsl[F] {

  implicit val userDecoder: EntityDecoder[F, User] = jsonOf[F, User]

  /**
    * Implement endpoint to register new users.
    * API should be like:
    * PUT /register
    * Content-Type: application/json
    *
    * { "login": "myLogin", "password": "myPassword" }
    *
    * Returns 201 Created if user was succesfully created and can be used for authentication.
    * Returns 409 Conflict if user with that login already exists.
    */
  val endpoints: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root / "dummy" => NotFound()
  }
}
