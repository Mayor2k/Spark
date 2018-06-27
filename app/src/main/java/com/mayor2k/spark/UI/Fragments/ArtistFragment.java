package com.mayor2k.spark.UI.Fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mayor2k.spark.Adapters.ArtistAdapter;
import com.mayor2k.spark.Interfaces.ApiService;
import com.mayor2k.spark.LastFmApi;
import com.mayor2k.spark.Models.Artist;
import com.mayor2k.spark.Models.LastFmModels.Image;
import com.mayor2k.spark.Models.LastFmModels.LastFmModel;
import com.mayor2k.spark.R;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.mayor2k.spark.UI.Activities.MainActivity.TAG;
import static com.mayor2k.spark.UI.Fragments.SongFragment.musicUri;

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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try{
            artistList = new ArrayList<>();
            artistAdapter = new ArtistAdapter(artistList,getContext());
            artistView.setLayoutManager(new GridLayoutManager(getActivity(),2));
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

                    SharedPreferences sPref = getActivity().getPreferences(MODE_PRIVATE);
                    String url = sPref.getString(artistTitle, "DEFAULT");
                    artistList.add(new Artist(artistId, artistTitle, url));
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
