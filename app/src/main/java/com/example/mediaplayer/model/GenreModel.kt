package com.example.mediaplayer.model


data class GenreModel(
        val id: Long,
        val name: String,
        val size: Int
) {

    fun withSongs(songs: Int): GenreModel {
        return GenreModel(
                id = id,
                name = name,
                size = songs
        )
    }

}