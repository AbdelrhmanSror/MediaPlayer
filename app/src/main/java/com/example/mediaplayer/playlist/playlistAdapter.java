package com.example.mediaplayer.playlist;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mediaplayer.databinding.PlaylistLayoutBinding;
import java.util.List;

public class playlistAdapter extends RecyclerView.Adapter<playlistAdapter.ViewHolder> {
    private List<PlayListModel> playLists;
    private onClickListener listener;

     playlistAdapter(List<PlayListModel> playLists, onClickListener listener) {
        this.playLists = playLists;
        this.listener = listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private PlaylistLayoutBinding binding;

        ViewHolder(@NonNull PlaylistLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.playlistContainer.setOnClickListener(v->
                    listener.onClick(playLists,getAdapterPosition()));
        }

        /**
         * @param item to bind its parameters with view
         *             method for binding the view with its data
         */
        void bind(PlayListModel item) {
            binding.setPlaylistModel(item);

        }

    }

    @NonNull
    @Override
    public playlistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return from(parent);
    }

    /**
     * method reponsible for inflating the view using databinding
     *
     * @param parent to get context from
     * @return ViewHolder object
     */
    private ViewHolder from(@NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        PlaylistLayoutBinding binding = PlaylistLayoutBinding.inflate(inflater);
        return new ViewHolder(binding);
    }



    @Override
    public void onBindViewHolder(@NonNull playlistAdapter.ViewHolder holder, int position) {
        holder.bind(playLists.get(position));

    }

    @Override
    public int getItemCount() {
        if(playLists!=null)
            return playLists.size();
        else return 0;

    }

    public interface onClickListener {
         void onClick(List<PlayListModel> playListModels,int itemClickIndex);
    }
}
