package dev.daika.davy.data.model

import dev.daika.davy.domain.entity.Anime
import dev.daika.davy.domain.entity.AnimeRating

fun AnimeSearchDto.toEntity(): Anime {
    return Anime(
        id = anime_id,
        title = title,
        description = description.orEmpty(),
        url = anime_url,
        poster = poster?.medium?.let {
            if (it.startsWith("//")) "https:$it" else it
        }.orEmpty(),
        rating = rating?.toEntity() ?: AnimeRating(0.0, 0)
    )
}