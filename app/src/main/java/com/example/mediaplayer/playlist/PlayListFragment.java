package com.example.mediaplayer.playlist;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.example.mediaplayer.R;
import com.example.mediaplayer.databinding.PlaylistFragmentBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static com.example.mediaplayer.constants.CHOSEN_SONG_INDEX;
import static com.example.mediaplayer.constants.LIST_SONG;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlayListFragment extends Fragment {
    private NavController navController;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE= 5;
    private PlaylistFragmentBinding binding;
    public PlayListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         binding=PlaylistFragmentBinding.inflate(inflater);
        //find the nav controller so i can use it to navigate
         navController=Navigation.findNavController(Objects.requireNonNull(getActivity()),R.id.nav_host_fragment);


        return binding.getRoot();
    }

    private void navigate(List<PlayListModel> playListModels, int itemClickedIndex)
    {
        Bundle bundle=new Bundle();
        bundle.putParcelableArrayList(LIST_SONG,(ArrayList)playListModels);
        bundle.putInt(CHOSEN_SONG_INDEX,itemClickedIndex);
        navController.navigate(R.id.action_playListFragment_to_chosenSongFragment,bundle);
    }

    @Override
    public void onStart() {
        super.onStart();
        checkpermission();

    }

    private void prepareMusicList()
    {
        List<PlayListModel>playListModels;
        PlayListViewModel playListViewModel = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(PlayListViewModel.class);
        playListModels=playListViewModel.getPlayLists();
        //creating adapter and set it with the playlists
        playlistAdapter adapter= new playlistAdapter(playListModels, this::navigate);
        //setup recyclerview with adapter
        binding.playList.setAdapter(adapter);
        if(playListModels==null)
        {
            binding.noAudioText.setVisibility(View.VISIBLE);
        }
    }

    private void checkpermission()
    {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(getActivity(),"You should grant this permission so application can access your audio files",Toast.LENGTH_LONG).show();
                // No explanation needed; request the permission
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

            } else {
                // No explanation needed; request the permission
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

            }
        } else {

            // Permission has already been granted
           prepareMusicList();

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has already been granted
               prepareMusicList();
            }
        }

    }


}
