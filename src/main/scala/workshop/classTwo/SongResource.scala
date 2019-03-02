package workshop.classTwo

import cats._
import cats.effect._
import cats.implicits._
import io.circe.generic.auto._
import io.circe.literal._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware
import org.http4s.server.middleware.authentication.BasicAuth
import org.http4s.{AuthedService, EntityDecoder, HttpRoutes}

import scala.language.higherKinds

class SongResource[F[_]: Monad: Effect](songService: SongService[F], userRepository: UserRepository[F])
    extends Http4sDsl[F] {

  implicit val songDecoder: EntityDecoder[F, SongDto] = jsonOf[F, SongDto]
  implicit val songsDecoder: EntityDecoder[F, List[SongDto]] =
    jsonOf[F, List[SongDto]]
  implicit val songUpdateDecoder: EntityDecoder[F, SongUpdate] =
    jsonOf[F, SongUpdate]

  object ArtistOptParam extends OptionalQueryParamDecoderMatcher[String]("artist")
  object TitleOptParam extends OptionalQueryParamDecoderMatcher[String]("title")

  val authMiddleware: AuthMiddleware[F, User] =
    BasicAuth[F, User]("Ringito", creds => userRepository.findByLoginAndSecret(creds.username, creds.password))

  val endpoints: HttpRoutes[F] =
    authMiddleware(AuthedService {
      case GET -> Root / "songs" :? ArtistOptParam(artist) +& TitleOptParam(title) as user =>
        (artist, title) match {
          case (Some(a), Some(t)) =>
            songService.findSong(user, a, t).flatMap(_.fold(NotFound())(song => Ok(song.asJson)))
          case (Some(a), None)    =>
            songService.findSongByArtist(user, a).flatMap(l => Ok(l.asJson))
          case (None, Some(t))    =>
            songService.findSongByTitle(user, t).flatMap(l => Ok(l.asJson))
          case (None, None)       =>
            songService.listSongs(user).flatMap(l => Ok(l.asJson))
        }

      case authReq @ POST -> Root / "songs" as user =>
        for {
          songs <- authReq.req.as[List[SongDto]]
          _     <- songService.saveSongs(user, songs)
          resp  <- Ok(json"""{"message": "Saved"}""")
        } yield resp

      /**
        * write put endpoint that will update songs artist/title
        * API should be like:
        * PUT /songs?artist=<oldArtist>&title=<oldTitle>
        * Content-Type: application/json
        *
        * {
        * "artist": "changed artist",
        * "title": "changed title"
        * }
        *
        * Returns updated object.
        *
        * Properties in request body are optional. If both missing no update will happen and
        * current entity will be returned. Both query parameters should be required here.
        */

    })
}

case class SongUpdate(artist: Option[String], title: Option[String])

case class SongDto(artist: String, title: String, start: Int, end: Int)
object SongDto {
  def apply(song: Song): SongDto =
    SongDto(song.artist, song.title, song.start, song.end)
}
