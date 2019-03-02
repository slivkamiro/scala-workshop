package workshop.classTwo

import cats._
import cats.implicits._
import org.scalatest._

import scala.collection.mutable.ListBuffer

class SongServiceSuite extends FlatSpec with Matchers {

  behavior of "SongService"
  val currentUser = User("test", "test")
  val otherUser   = User("other", "otter")

  it should "find all saved songs" in new SongService(
    new TestInMemSongRepository(10)) {

    saveSongs(otherUser,
              List(
                SongDto("aaa", "bbb", 0, 1)
              ))

    saveSongs(currentUser,
              List(
                SongDto("aaa", "ccc", 0, 1),
                SongDto("ddd", "bbb", 0, 1)
              ))

    listSongs(currentUser) shouldBe List(SongDto("ddd", "bbb", 0, 1),
                                         SongDto("aaa", "ccc", 0, 1),
                                         SongDto("aaa", "bbb", 0, 1))

    findSong(currentUser, "aaa", "bbb") shouldBe Some(
      SongDto("aaa", "bbb", 0, 1))
    findSongByArtist(currentUser, "aaa") shouldBe List(
      SongDto("aaa", "ccc", 0, 1),
      SongDto("aaa", "bbb", 0, 1))
    findSongByTitle(currentUser, "bbb") shouldBe List(
      SongDto("ddd", "bbb", 0, 1),
      SongDto("aaa", "bbb", 0, 1))
  }

  it should "find most used cut for a song" in new SongService(
    new TestInMemSongRepository(10)) {
    saveSongs(
      currentUser,
      List(
        SongDto("aaa", "ccc", 0, 1),
        SongDto("aaa", "ccc", 1, 2),
        SongDto("aaa", "ccc", 0, 3),
        SongDto("aaa", "ccc", 1, 4),
        SongDto("aaa", "ccc", 1, 3),
        SongDto("aaa", "ccc", 1, 3),
        SongDto("aaa", "ccc", 0, 2),
        SongDto("aaa", "ccc", 1, 1),
        SongDto("aaa", "ccc", 0, 3),
        SongDto("aaa", "ccc", 1, 4)
      )
    )

    findSong(currentUser, "aaa", "ccc") shouldBe Some(
      SongDto("aaa", "ccc", 1, 3))
  }

  it should "find most used cut for song user saved only using user's data" in new SongService(
    new TestInMemSongRepository(10)) {
    saveSongs(currentUser,
              List(SongDto("aaa", "ccc", 0, 1),
                   SongDto("aaa", "ccc", 1, 2),
                   SongDto("aaa", "ccc", 0, 3),
                   SongDto("aaa", "ccc", 1, 4),
                   SongDto("aaa", "ccc", 1, 2)))
    saveSongs(otherUser,
              List(
                SongDto("aaa", "ccc", 1, 3),
                SongDto("aaa", "ccc", 0, 3),
                SongDto("aaa", "ccc", 1, 1),
                SongDto("aaa", "ccc", 0, 3),
                SongDto("aaa", "ccc", 1, 4)
              ))

    findSong(currentUser, "aaa", "ccc") shouldBe Some(
      SongDto("aaa", "ccc", 1, 2))
  }
}

class TestInMemSongRepository(maxSame: Int) extends SongRepository[Id] {

  private val songs = ListBuffer[Song]()

  private val append = appendMax(maxSame) _

  private def appendMax(max: Int)(song: Song): Song = {
    val songFilter: Song => Boolean = song =>
      song.artist === song.artist && song.title === song.title && song.userName === song.userName
    val cnt = songs.count(songFilter)
    if (cnt > max)
      songs.find(songFilter).map(songs.indexOf).foreach(songs.remove)
    songs += song
    song
  }

  override def findAll(): Id[List[Song]] = songs.toList

  override def findByArtist(artist: String): Id[List[Song]] =
    songs.filter(_.artist === artist).toList

  override def findByTitle(title: String): Id[List[Song]] =
    songs.filter(_.title === title).toList

  override def findByArtistAndTitle(artist: String,
                                    title: String): Id[List[Song]] =
    songs.filter(t => t.artist === artist && t.title === title).toList

  override def saveSong(song: Song): Id[Song] = append(song)

  override def updateSong(userName: String, artist: String, title: String, songUpdate: SongUpdate): Id[List[Song]] = {
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
