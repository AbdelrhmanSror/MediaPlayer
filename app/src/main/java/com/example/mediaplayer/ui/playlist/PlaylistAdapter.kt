package com.example.mediaplayer.ui.playlist


import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mediaplayer.R
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
            binding.spinner.setOnClickListener {
                //creating a popup menu
                val popup = PopupMenu(binding.spinner.context, binding.spinner)
                //inflating menu from xml resource
                popup.inflate(R.menu.track_option_menu)
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.edit -> {
                            Toast.makeText(binding.spinner.context, "edit", Toast.LENGTH_LONG).show()
                            true
                        }
                        R.id.delete -> {
                            Toast.makeText(binding.spinner.context, "delete", Toast.LENGTH_LONG).show()
                            true
                        }
                        R.id.add_to_favourite -> {
                            Toast.makeText(binding.spinner.context, "add to favourite", Toast.LENGTH_LONG).show()
                            true
                        }
                        else -> false
                    }

                }

                //displaying the popup
                popup.show()

            }
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
        holder.bind(getItem(position)!!, itemListener)
    }


}
