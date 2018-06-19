package com.mayor2k.spark.UI.Fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.mayor2k.spark.Adapters.ArtistAdapter;
import com.mayor2k.spark.Adapters.SongAdapter;
import com.mayor2k.spark.Models.Artist;
import com.mayor2k.spark.R;

import java.util.ArrayList;

import static com.mayor2k.spark.UI.Fragments.SongFragment.musicUri;

public class ArtistFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private RecyclerView artistView;
    public static ArrayList<Artist> artistList;
    private ArtistAdapter artistAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), musicUri, COLUMNS, null, null,
                MediaStore.Audio.Artists.ARTIST + " ASC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data==null){
            return;
        }
        ArrayList<String>checking = new ArrayList<>();
        if (data.moveToFirst()) {
            int idColumn = data.getColumnIndex(MediaStore.Audio.Artists._ID);
            int titleColumn = data.getColumnIndex(MediaStore.Audio.Artists.ARTIST);
            for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
                long artistId = data.getLong(idColumn);
                String artistTitle = data.getString(titleColumn);
                if (!checking.contains(artistTitle)) {
                    checking.add(artistTitle);
                    artistList.add(new Artist(artistId, artistTitle));
                }
            }
            artistAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        artistAdapter.swapCursor(null);
    }
}
