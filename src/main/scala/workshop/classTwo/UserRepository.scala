package workshop.classTwo

import scala.language.higherKinds

trait UserRepository[F[_]] {

  def save(user: User): F[User]

  def findByLoginAndSecret(login: String, secret: String): F[Option[User]]

  def findByLogin(login: String): F[Option[User]]

}

case class User(login: String, secret: String)
