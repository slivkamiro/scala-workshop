package workshop.classTwo

import cats.data.{Ior, OptionT}
import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import org.http4s._
import org.http4s.implicits._
import org.http4s.server.blaze._
import org.http4s.server.middleware.Logger

import scala.language.higherKinds

object HttpServer extends IOApp {

  val xa = Transactor.fromDriverManager[IO](
    "org.h2.Driver",
    "jdbc:h2:~/live;INIT=RUNSCRIPT FROM './target/scala-2.12/classes/init.sql'",
    "sa",
    ""
  )

  val songRepository: SongRepository[IO] = new H2SongRepository(xa)
  val userRepository: UserRepository[IO] = new H2UserRepository(xa)

  val songService: HttpRoutes[IO] = new SongResource(new SongService(songRepository), userRepository).endpoints
  val userService: HttpRoutes[IO] = new UserResource(userRepository).endpoints

  /*val meteredEndpoints: IO[HttpRoutes[IO]] =
    Prometheus[IO](new CollectorRegistry(), "Ringito")
      .map(Metrics[IO](_)(songService))*/

  override def run(args: List[String]): IO[ExitCode] = {
    val routes = userService <+> songService

    BlazeServerBuilder[IO]
      .bindHttp(8111, "0.0.0.0")
      .withHttpApp(Logger.httpApp(true, true)(routes.orNotFound))
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}

class H2SongRepository(xa: Transactor.Aux[IO, Unit])
    extends SongRepository[IO] {

  // TODO return stream

  override def findAll(): IO[List[Song]] =
    sql"select artist, title, start, end, userName from songs"
      .query[Song]
      .to[List]
      .transact(xa)

  override def findByArtist(artist: String): IO[List[Song]] =
    sql"select artist, title, start, end, userName from songs where artist = $artist"
      .query[Song]
      .to[List]
      .transact(xa)

  override def findByTitle(title: String): IO[List[Song]] =
    sql"select artist, title, start, end, userName from songs where title = $title"
      .query[Song]
      .to[List]
      .transact(xa)

  override def findByArtistAndTitle(artist: String,
                                    title: String): IO[List[Song]] =
    sql"select artist, title, start, end, userName from songs where artist = $artist and title = $title"
      .query[Song]
      .to[List]
      .transact(xa)

  override def saveSong(song: Song): IO[Song] = saveImpl(song).transact(xa)

  private def saveImpl(song: Song): ConnectionIO[Song] =
    for {
      id <- sql"insert into songs(artist, title, start, end, userName) values (${song.artist}, ${song.title}, ${song.start}, ${song.end}, ${song.userName})".update
        .withUniqueGeneratedKeys[Long]("id")
      saved <- sql"select artist, title, start, end, userName from songs where id = $id"
        .query[Song]
        .unique
    } yield saved

  override def updateSong(userName: String,
                          artist: String,
                          title: String,
                          songUpdate: SongUpdate): IO[List[Song]] =
    updateImpl(songUpdate)((userName, artist, title)).value
      .transact(xa)
      .map(o => if (o.isEmpty) Nil else o.get)

  private def updateImpl(songUpdate: SongUpdate)
                        (implicit lat: (String, String, String)): OptionT[ConnectionIO, List[Song]] =
    for {
      at      <- OptionT.fromOption[ConnectionIO](Ior.fromOptions(songUpdate.artist, songUpdate.title))
      updated <- OptionT.liftF(at.fold(
        artist          => updateSongs(sql"update songs set artist = $artist where userName = ${lat._1} and artist = ${lat._2} and title = ${lat._3}".update.run)(lat.copy(_2 = artist)),
        title           => updateSongs(sql"update songs set title = $title where userName = ${lat._1} and artist = ${lat._2} and title = ${lat._3}".update.run)(lat.copy(_3 = title)),
        (artist, title) => updateSongs(sql"update songs set artist = $artist, title = $title where userName = ${lat._1} and artist = ${lat._2} and title = ${lat._3}".update.run)(lat.copy(_2 = artist, _3 = title))))
    } yield updated

  private def updateSongs(stmt: ConnectionIO[Int])
                         (implicit lat: (String, String, String)): ConnectionIO[List[Song]] = for {
    _       <- stmt
    updated <- sql"select artist, title, start, end, userName from songs where userName = ${lat._1} and artist = ${lat._2} and title = ${lat._3}"
        .query[Song]
        .to[List]
  } yield updated
}

class H2UserRepository(xa: Transactor.Aux[IO, Unit])
    extends UserRepository[IO] {

  /**
    * Implement save method using doobie syntax.
    * The implementation should save user to the db and return saved object.
    * @param user user to be saved.
    * @return Saved user.
    */
  override def save(user: User): IO[User] = saveImpl(user).transact(xa)

  private def saveImpl(user: User): ConnectionIO[User] =
    for {
      _     <- sql"insert into users values (${user.login}, ${user.secret})".update.run
      saved <- sql"select login, secret from users where login = ${user.login}"
        .query[User]
        .unique
    } yield saved

  override def findByLoginAndSecret(login: String,
                                    secret: String): IO[Option[User]] =
    sql"select login, secret from users where login = $login and secret = $secret"
      .query[User]
      .option
      .transact(xa)

  override def findByLogin(login: String): IO[Option[User]] =
    sql"select login, secret from users where login = $login"
      .query[User]
      .option
      .transact(xa)
}
