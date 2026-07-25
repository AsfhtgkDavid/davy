package dev.daika.davy.data.model

import dev.daika.davy.domain.entity.AnimeEpisode
import dev.daika.davy.domain.entity.AnimePlayer
import dev.daika.davy.domain.entity.AnimeTranslation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

fun List<AnimeVideoDto>.toEntity(): List<AnimeTranslation> {
    return this.groupBy { it.data.dubbing }
        .map { (dubbing, videosInDubbing) ->
            AnimeTranslation(
                title = dubbing,
                availablePlayers = videosInDubbing
                    .groupBy { it.data.player }
                    .map { (player, episodes) ->
                        AnimePlayer(
                            player = player,
                            episodes = episodes.map {
                                AnimeEpisode(it.id, it.number, it.iframeUrl)
                            }
                        )
                    }
            )
        }
}