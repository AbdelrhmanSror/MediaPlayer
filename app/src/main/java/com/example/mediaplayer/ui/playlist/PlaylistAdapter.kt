package com.example.mediaplayer.ui.playlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayer.databinding.PlaylistLayoutBinding
import com.example.mediaplayer.model.PlayListModel
import com.example.mediaplayer.ui.playlist.PlaylistAdapter.ViewHolder.Companion.from

class PlaylistAdapter(private val playLists: List<PlayListModel>, private val listener: OnClickListener) : RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    class ViewHolder(private val binding: PlaylistLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        /**
         * @param item to bind its parameters with view
         * method for binding the view with its data
         */
        fun bind(item: PlayListModel, playLists: List<PlayListModel>, listener: OnClickListener) {
            binding.playlistModel = item
            binding.playlistContainer.setOnClickListener { listener.onClick(playLists, adapterPosition) }


        }

        companion object {
            /**
             * method reponsible for inflating the view using databinding
             *
             * @param parent to get context from
             * @return ViewHolder object
             */
            fun from(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = PlaylistLayoutBinding.inflate(inflater)
                return ViewHolder(binding)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return from(parent)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(playLists[position], playLists, listener)

    }

    override fun getItemCount(): Int {
        return playLists.size

    }

    interface OnClickListener {
        fun onClick(playLists: List<PlayListModel>, itemClickIndex: Int)
    }
}
