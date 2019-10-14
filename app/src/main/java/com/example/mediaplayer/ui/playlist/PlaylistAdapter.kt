package com.example.mediaplayer.ui.playlist


import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayer.databinding.PlaylistLayoutBinding
import com.example.mediaplayer.model.SongModel
import com.example.mediaplayer.ui.OnItemClickListener
import kotlinx.android.synthetic.main.playlist_layout.view.*


class PlaylistAdapter(private val itemListener: OnItemClickListener) : ListAdapter<SongModel, PlaylistAdapter.ViewHolder>(DiffCallBack) {

    /**
     *diff util class to calculate the difference between two list if the the old list has changed
     *with minimum changes it can do
     */
    object DiffCallBack : DiffUtil.ItemCallback<SongModel>() {
        override fun areItemsTheSame(oldItem: SongModel, newItem: SongModel): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: SongModel, newItem: SongModel): Boolean {
            return oldItem.title == newItem.title
        }

    }

    class ViewHolder(val binding: PlaylistLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SongModel, listener: OnItemClickListener) {
            binding.playlistModel = item
            binding.root.playlistContainer.setOnClickListener { listener.onClick(adapterPosition) }
        }


        /**
         * return the view that viewHolder will hold
         */
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val inflater = LayoutInflater.from(parent.context)
                val binding = PlaylistLayoutBinding.inflate(inflater)
                return ViewHolder(binding)
            }
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.v("PlaylistBind", "binding")

        holder.bind(getItem(position)!!, itemListener)
    }


}
