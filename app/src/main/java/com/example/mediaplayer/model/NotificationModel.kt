package com.example.mediaplayer.model


sealed class Event {

    data class Metadata(
            val entity: SongModel
    ) : Event()

    data class State(
            val state: Boolean
    ) : Event()

}

/**
 * Used to sync 3 different data sources,
 * metadata, state and favorite
 */
data class MusicNotificationModel
(
        var title: String = "",
        var artist: String = "",
        var album: String = "",
        var isPlaying: Boolean = true,
        var bookmark: Long = -1,
        var duration: Long = -1,
        var isFavorite: Boolean = false
) {


    /**
     * @return true if contains the minimal state for begin posted as a notification
     */
    private fun isValidState(): Boolean {
        return title.isNotBlank() &&
                artist.isNotBlank() &&
                album.isNotBlank() &&
                duration != -1L
    }

    fun updateMetadata(metadata: SongModel): Boolean {
        this.title = metadata.title
        this.artist = metadata.actor
        this.album = metadata.albumCoverUri.toString()
        this.duration = metadata.duration ?: 1
        return isValidState()
    }

    fun updateState(state: Boolean): Boolean {
        this.isPlaying = state
        return isValidState()
    }

    fun isDifferentMetadata(metadata: SongModel): Boolean {
        return this.title != metadata.title ||
                this.artist != metadata.actor ||
                this.album != metadata.albumCoverUri
    }

    fun isDifferentState(state: Boolean): Boolean {
        val isPlaying = state
        // val bookmark = TimeUnit.MILLISECONDS.toSeconds(state.position)
        return this.isPlaying != isPlaying /*||
                TimeUnit.MILLISECONDS.toSeconds(this.bookmark) != bookmark*/
    }


    fun deepCopy(): MusicNotificationModel {
        return MusicNotificationModel(title, artist, album, isPlaying, bookmark, duration, isFavorite
        )
    }

}