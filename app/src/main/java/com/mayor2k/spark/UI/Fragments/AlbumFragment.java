package com.mayor2k.spark.UI.Fragments;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.mayor2k.spark.Adapters.AlbumAdapter;
import com.mayor2k.spark.Models.Album;
import com.mayor2k.spark.Models.Artist;
import com.mayor2k.spark.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import static com.mayor2k.spark.UI.Fragments.SongFragment.musicUri;

public class AlbumFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private GridView albumView;
    private AlbumAdapter albumAdapter;
    public static ArrayList<Album> albumList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        albumView = view.findViewById(R.id.album_grid);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try{
            albumList = new ArrayList<>();
            albumAdapter = new AlbumAdapter(getActivity(),null,albumList);
            albumView.setAdapter(albumAdapter);
        }catch (IllegalArgumentException e){
            Toast.makeText(getActivity(),"Nothing found",Toast.LENGTH_LONG).show();
        }
        getLoaderManager().initLoader(1,null,this);
    }

    private final String[] COLUMNS = new String[]{
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.ALBUM_ID,
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), musicUri, COLUMNS, null, null,
                MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data==null){
            return;
        }
        ArrayList<String>checking = new ArrayList<>();
        if (data.moveToFirst()) {
            int idColumn = data.getColumnIndex(MediaStore.Audio.Albums._ID);
            int titleColumn = data.getColumnIndex(MediaStore.Audio.Albums.ALBUM);
            int artistColumn = data.getColumnIndex(MediaStore.Audio.Albums.ARTIST);
            int cover = data.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ID);
            for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
                long albumId = data.getLong(idColumn);

                long songCover = data.getLong(cover);
                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri uri = ContentUris.withAppendedId(sArtworkUri, songCover);

                String artistTitle = data.getString(artistColumn);
                String albumTitle = data.getString(titleColumn);

                if (!checking.contains(albumTitle)) {
                    checking.add(albumTitle);
                    albumList.add(new Album(albumId ,albumTitle,artistTitle, uri));
                }
            }
            albumAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        albumAdapter.swapCursor(null);
    }
}