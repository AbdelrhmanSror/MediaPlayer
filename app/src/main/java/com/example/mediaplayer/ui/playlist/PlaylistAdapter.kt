package com.example.mediaplayer.ui.playlist
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayer.PLAYLIST_VIEWHOLDER
import com.example.mediaplayer.model.PlayListModel

class PlaylistAdapter(private val playLists: List<PlayListModel>
                      , private val viewHolderType: String
                      , private val listener: OnClickListener) : RecyclerView.Adapter<ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewHolderType) {
            PLAYLIST_VIEWHOLDER -> PlayListViewHolder.from(parent)
            else -> PlayListBottomSheetViewHolder.from(parent)
        }
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(playLists, listener)
    }

    override fun getItemCount(): Int {
        return playLists.size

    }

}

class OnClickListener(val clickListener: (playLists: List<PlayListModel>, itemClickIndex: Int) -> Unit) {
    fun onClick(playLists: List<PlayListModel>, itemClickIndex: Int) = clickListener(playLists, itemClickIndex)
}