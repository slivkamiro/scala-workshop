package workshop.classTwo

import cats._
import cats.implicits._

import scala.language.higherKinds

class SongService[F[_]: Monad](R: SongRepository[F]) {

  def updateSong(user: User, artist: String, title: String, songUpdate: SongUpdate): F[Option[SongDto]] = {
    for {
      updateLst <- R.updateSong(user.login, artist, title, songUpdate)
    } yield
      collectBestCuts(s => (s.artist, s.title), user, updateLst).map {
        case ((artistU, titleU), start, end) =>
          SongDto(artistU, titleU, start, end)
      }.headOption
  }

  def findSong(user: User, artist: String, title: String): F[Option[SongDto]] =
    for {
      songs <- R.findByArtistAndTitle(artist, title)
    } yield
      collectBestCuts(s => (s.artist, s.title), user, songs).map {
        case (_, start, end) =>
          SongDto(artist, title, start, end)
      }.headOption

  def listSongs(user: User): F[List[SongDto]] =
    for {
      songs <- R.findAll()
    } yield
      collectBestCuts(s => (s.artist, s.title), user, songs).map {
        case ((artist, title), start, end) =>
          SongDto(artist, title, start, end)
      }

  def findSongByArtist(user: User, artist: String): F[List[SongDto]] =
    for {
      songs <- R.findByArtist(artist)
    } yield
      collectBestCuts(_.title, user, songs).map {
        case (title, start, end) =>
          SongDto(artist, title, start, end)
      }

  def findSongByTitle(user: User, title: String): F[List[SongDto]] =
    for {
      songs <- R.findByTitle(title)
    } yield
      collectBestCuts(_.artist, user, songs).map {
        case (artist, start, end) =>
          SongDto(artist, title, start, end)
      }

  private def collectBestCuts[Key](keySelector: Song => Key, user: User, songs: List[Song]): List[(Key, Int, Int)] = {
    val bestCutOf  = findBestCut(keySelector) _
    val (my, rest) = songs.partition(_.userName === user.login)
    val myBest     = bestCutOf(my)
    val myStuff    = my.map(keySelector).distinct
    val restBest   = bestCutOf(rest.filter(s => !myStuff.contains(keySelector(s))))
    myBest ++ restBest
  }

  private def findBestCut[Key](groupingKey: Song => Key)(songs: List[Song]): List[(Key, Int, Int)] =
    songs
      .groupBy(groupingKey)
      .mapValues(songs => (bestCut(songs, _.start), bestCut(songs, _.end)))
      .map { case (key, (start, end)) => (key, start, end) }
      .toList

  private def bestCut(songs: List[Song], cut: Song => Int): Int =
    songs.groupBy(cut).mapValues(_.size).maxBy(_._2)._1

  def saveSongs(user: User, songs: List[SongDto]): F[Unit] =
    for {
      _ <- songs.map { song =>
        R.saveSong(Song(song.artist, song.title, song.start, song.end, user.login))
      }.sequence
    } yield ()
}
