package com.mayor2k.spark.UI.Fragments;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mayor2k.spark.Adapters.ArtistAdapter;
import com.mayor2k.spark.Interfaces.ApiService;
import com.mayor2k.spark.LastFmApi;
import com.mayor2k.spark.Models.Album;
import com.mayor2k.spark.Models.Artist;
import com.mayor2k.spark.Models.LastFmModels.LastFmModel;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.mayor2k.spark.UI.Activities.MainActivity.TAG;
import static com.mayor2k.spark.UI.Fragments.AlbumFragment.albumList;
import static com.mayor2k.spark.UI.Fragments.SongFragment.musicUri;
import static com.mayor2k.spark.UI.Fragments.SongFragment.songList;

public class ArtistFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private RecyclerView artistView;
    public static ArrayList<Artist> artistList;
    private ArtistAdapter artistAdapter;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artist, container, false);
        artistView = view.findViewById(R.id.artist_grid);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        SubMenu subMenu = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, "Grid size");
        subMenu.add(Menu.NONE,1,Menu.NONE,"1");
        subMenu.add(Menu.NONE,2,Menu.NONE,"2");
        subMenu.add(Menu.NONE,3,Menu.NONE,"3");
        subMenu.add(Menu.NONE,4,Menu.NONE,"4");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        SharedPreferences sPref = getActivity().getPreferences(MODE_PRIVATE);
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor ed = sPref.edit();
        switch (item.getItemId()){
            case 1:
                ed.putInt("ArtistSpanCount",1);
                ed.apply();
                onActivityCreated(null);
                return true;
            case 2:
                ed.putInt("ArtistSpanCount",2);
                ed.apply();
                onActivityCreated(null);
                return true;
            case 3:
                ed.putInt("ArtistSpanCount",3);
                ed.apply();
                onActivityCreated(null);
                return true;
            case 4:
                ed.putInt("ArtistSpanCount",4);
                ed.apply();
                onActivityCreated(null);
                return true;
            default:
                return super .onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        try{
            artistList = new ArrayList<>();
            artistAdapter = new ArtistAdapter(artistList,getContext(),getActivity());

            int spanCount;
            SharedPreferences sPref = getActivity().getPreferences(MODE_PRIVATE);
            if (!sPref.contains("ArtistSpanCount"))
                spanCount = 2;
            else
                spanCount = sPref.getInt("ArtistSpanCount", -1);

            if (spanCount==1)
                artistView.setLayoutManager(new LinearLayoutManager(getActivity()));
            else
                artistView.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));

            artistView.setAdapter(artistAdapter);
        }catch (IllegalArgumentException e){
            Toast.makeText(getActivity(),"Nothing found",Toast.LENGTH_LONG).show();
        }
        getLoaderManager().initLoader(1, null, this);
    }

    private final String[] COLUMNS = new String[]{
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST
    };

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), musicUri, COLUMNS, null, null,
                MediaStore.Audio.Artists.ARTIST + " ASC");
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if(data==null){
            return;
        }
        ArrayList<String>checking = new ArrayList<>();
        if (data.moveToFirst()) {
            int idColumn = data.getColumnIndex(MediaStore.Audio.Artists._ID);
            int titleColumn = data.getColumnIndex(MediaStore.Audio.Artists.ARTIST);
            for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
                long artistId = data.getLong(idColumn);
                final String artistTitle = data.getString(titleColumn);

                if (!checking.contains(artistTitle)) {
                    checking.add(artistTitle);
                    SharedPreferences sPref = getActivity().getPreferences(MODE_PRIVATE);
                    if (!sPref.contains(artistTitle)){
                        ApiService api = LastFmApi.getApiService();
                        Call<LastFmModel> call = api.getArtistImage(artistTitle);
                        call.enqueue(new Callback<LastFmModel>() {
                            @Override
                            public void onResponse(@NonNull Call<LastFmModel> call, @NonNull Response<LastFmModel> response) {
                                SharedPreferences sPref = getActivity().getPreferences(MODE_PRIVATE);
                                SharedPreferences.Editor ed = sPref.edit();
                                try{
                                    ed.putString(artistTitle, response.body().getArtist().getImage().get(2).getText());
                                }catch (NullPointerException e){
                                    Log.i(TAG, "onResponse is null");
                                }
                                ed.apply();
                            }

                            @Override
                            public void onFailure(@NonNull Call<LastFmModel> call, @NonNull Throwable t) {

                            }
                        });
                    }
                    String url = sPref.getString(artistTitle, "DEFAULT");

                    int songInfo=0;
                    int albumInfo=0;
                    for(int i = 0; songList.size()>i;i++){
                        Song song = songList.get(i);
                        if (Objects.equals(song.getArtist(), artistTitle)){
                            songInfo++;
                        }
                    }

                    for(int i = 0; albumList.size()>i;i++){
                        Album song = albumList.get(i);
                        if (Objects.equals(song.getArtist(), artistTitle)){
                            albumInfo++;
                        }
                    }
                    artistList.add(new Artist(artistId, artistTitle, url, songInfo, albumInfo));
                }
            }
            artistAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        artistAdapter.swapCursor(null);
    }
}
