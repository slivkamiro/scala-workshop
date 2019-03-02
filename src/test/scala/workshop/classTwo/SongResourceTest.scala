package workshop.classTwo

import cats.effect._
import cats.implicits._
import org.http4s._
import org.http4s.headers.Authorization
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.mutable.ListBuffer

class SongResourceTest extends FlatSpec with Matchers {

  behavior of "SongResource"

  it should "require authorization" in new SongResource(new SongService(new InMemSongRepository(10)), InMemUserRepository) {
    val request = Request[IO](uri = Uri(path = "/songs"))
    endpoints(request).value.unsafeRunSync().map(_.status) shouldBe Some(Status.Unauthorized)
  }

  it should "accept parameters" in new SongResource(new SongService(new InMemSongRepository(10)), InMemUserRepository) {
    val request = Request[IO](
      uri     = Uri(path  = "/songs",
                    query = Query("artist" -> Some("great"), "title" -> Some("song"))
      ),
      headers = Headers(Authorization(BasicCredentials("test", "1234"))))
    endpoints(request).value.unsafeRunSync().map(_.status) shouldBe Some(Status.NotFound)
  }
}

class InMemSongRepository(maxSame: Int) extends SongRepository[IO] {

  private val songs = ListBuffer[Song]()

  private val append = appendMax(maxSame) _

  private def appendMax(max: Int)(song: Song) = for {
    cnt <- IO(songs.count(songFilter))
    res <- IO {
      if (cnt > max) songs.find(songFilter).map(songs.indexOf).foreach(songs.remove)
      songs += song
      song
    }
  } yield res

  private def songFilter(song: Song): Boolean =
    song.artist === song.artist && song.title === song.title && song.userName === song.userName

  override def findAll(): IO[List[Song]] = IO(songs.toList)

  override def findByArtist(artist: String): IO[List[Song]] = IO(songs.filter(_.artist === artist).toList)

  override def findByTitle(title: String): IO[List[Song]] = IO(songs.filter(_.title === title).toList)

  override def findByArtistAndTitle(artist: String, title: String): IO[List[Song]] =
    IO(songs.filter(t => t.artist === artist && t.title === title).toList)

  override def saveSong(song: Song): IO[Song] = append(song)

  override def updateSong(userName: String, artist: String, title: String, songUpdate: SongUpdate): IO[List[Song]] = IO {
    val songsToUpdate = songs.filter(song => song.artist === artist && song.title === title)
    (songUpdate.artist, songUpdate.title) match {
      case (None, None) => songsToUpdate.toList
      case (Some(a), None) =>
        songsToUpdate.map(songs.indexOf).foreach(songs.remove)
        val updated = songs.map(song => song.copy(artist = a))
        songs ++= updated
        updated.toList
      case (None, Some(t)) =>
        songsToUpdate.map(songs.indexOf).foreach(songs.remove)
        val updated = songs.map(song => song.copy(title = t))
        songs ++= updated
        updated.toList
      case (Some(a), Some(t)) =>
        songsToUpdate.map(songs.indexOf).foreach(songs.remove)
        val updated = songs.map(song => song.copy(artist = a, title = t))
        songs ++= updated
        updated.toList
    }
  }
}

object InMemUserRepository extends UserRepository[IO] {

  private val users = ListBuffer[User](User("test", "1234"))

  private def append(user: User): IO[User] =  IO {
    users += user
    user
  }

  override def save(user: User): IO[User] = for {
    old    <- IO(users.find(u => u.login == user.login))
    _      = old.map(users.indexOf).foreach(users.remove)
    saved <- append(user)
  } yield saved

  override def findByLoginAndSecret(login: String, secret: String): IO[Option[User]] =
    IO(users.find(user => user.login === login && user.secret === secret))

  override def findByLogin(login: String): IO[Option[User]] =
    IO(users.find(user => user.login === login))
}
