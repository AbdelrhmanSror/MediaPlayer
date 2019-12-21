package com.example.mediaplayer.model


data class AlbumModel(
        val id: Long,
        val artistId: Long,
        val title: String,
        val artist: String,
        val albumArtist: String,
        val songs: Int
) {

    fun withSongs(songs: Int): AlbumModel {
        return AlbumModel(
                id = id,
                artistId = artistId,
                title = title,
                artist = artist,
                albumArtist = albumArtist,
                songs = songs
        )
    }

}