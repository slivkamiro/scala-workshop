package workshop.classTwo

import scala.language.higherKinds

trait SongRepository[F[_]] {
  def findAll(): F[List[Song]]
  def findByArtist(artist: String): F[List[Song]]
  def findByTitle(title: String): F[List[Song]]
  def findByArtistAndTitle(artist: String, title: String): F[List[Song]]
  def saveSong(song: Song): F[Song]

  def updateSong(userName: String,
                 artist: String,
                 title: String,
                 songUpdate: SongUpdate): F[List[Song]]
}

case class Song(artist: String,
                title: String,
                start: Int,
                end: Int,
                userName: String)
