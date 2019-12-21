package com.example.mediaplayer.model


data class ArtistModel(
        val id: Long,
        val name: String,
        val albumArtist: String,
        val songs: Int
) {


    fun withSongs(songs: Int): ArtistModel {
        return ArtistModel(
                id = id,
                name = name,
                albumArtist = albumArtist,
                songs = songs
        )
    }
}