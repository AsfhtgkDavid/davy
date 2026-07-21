package dev.daika.davy.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Anime(
    @SerialName("anime_id")
    val id: Int,
    val title: String,
    val description: String,
    @SerialName("anime_url")
    val url: String,
    val poster: AnimePosters,
    val rating: AnimeRating,

    @SerialName("other_titles")
    val otherTitles: List<String>? = null,
    val genres: List<AnimeGenre>? = null,

    val videos: List<AnimeVideoDto>? = null,
)

@Serializable
data class AnimePosters(
    val small: String,
    val medium: String,
    val big: String,
    val fullsize: String,
    val huge: String,
    val mega: String,
)

@Serializable
data class AnimeRating(
    val average: Double,
    val counters: Int
)

@Serializable
data class AnimeGenre(
    val id: Int,
    val title: String,
)

@Serializable
data class AnimeVideoDto(
    @SerialName("video_id")
    val id: Int,
    @SerialName("iframe_url")
    val iframeUrl: String,
    val data: PlayerDataDto,
    val number: String, // WHY STRING?
)

@Serializable
data class PlayerDataDto(
    val player: String,
    val dubbing: String,
)

data class AnimeTranslation(
    val title: String,
    val availablePlayers: List<AnimePlayer>
) {
    companion object {
        fun fromVideoDto(videoDto: List<AnimeVideoDto>): List<AnimeTranslation> {
            val translationsMap =
                mutableMapOf<String, MutableMap<String, MutableList<AnimeEpisode>>>()

            for (video in videoDto) {
                val translationTitle = video.data.dubbing
                val translation = translationsMap.getOrPut(translationTitle) { mutableMapOf() }
                translation.getOrPut(video.data.player) {
                    mutableListOf()
                }.add(
                    AnimeEpisode(
                        videoId = video.id,
                        title = video.number,
                        iframeUrl = video.iframeUrl
                    )
                )
            }

            return translationsMap.map { (title, players) ->
                AnimeTranslation(title, players.map { (player, episodes) ->
                    AnimePlayer(player, episodes)
                })
            }
        }
    }
}

data class AnimePlayer(
    val player: String,
    val episodes: List<AnimeEpisode>
)

data class AnimeEpisode(
    val videoId: Int,
    val title: String,
    val iframeUrl: String
)