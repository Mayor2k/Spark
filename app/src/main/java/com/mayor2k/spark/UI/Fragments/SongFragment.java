package com.mayor2k.spark.UI.Fragments;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.mayor2k.spark.Adapters.SongAdapter;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;
import com.mayor2k.spark.UI.Activities.MainActivity;

import java.util.ArrayList;
import java.util.Objects;

public class SongFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private RecyclerView songView;
    public static ArrayList<Song> songList;
    public final static Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private SongAdapter songAdt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_song, container, false);
        songView = view.findViewById(R.id.trackList);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            songList = new ArrayList<>();
            songAdt = new SongAdapter(songList,getContext());
            songView.setLayoutManager(new LinearLayoutManager(getActivity()));
            songView.setAdapter(songAdt);
        } catch (IllegalArgumentException e) {
            Toast.makeText(getActivity(), "Nothing found", Toast.LENGTH_LONG).show();
        }
        getLoaderManager().initLoader(0, null, this);
    }

    private final String[] COLUMNS = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DURATION
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), musicUri, COLUMNS, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.i("TAGGING","that work!!!");
        if (data==null){
            return;
        }
        if (data.moveToFirst()) {
            int titleColumn = data.getColumnIndex(MediaStore.MediaColumns.TITLE);
            int idColumn = data.getColumnIndex(BaseColumns._ID);
            int artistColumn = data.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST);
            int albumColumn = data.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM);
            int column_index = data.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            int trackColumn = data.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK);
            int durationColumn = data.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
            int cover = data.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
            for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
                long songId = data.getLong(idColumn);
                String songTitle = data.getString(titleColumn);
                String songArtist = data.getString(artistColumn);
                String songAlbum = data.getString(albumColumn);
                String pathId = data.getString(column_index);
                long songCover = data.getLong(cover);
                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri uri = ContentUris.withAppendedId(sArtworkUri, songCover);

                String track = data.getString(trackColumn);
                int songTrack;
                if (track.length() == 4) {
                    if (!Objects.equals(track.substring(2), "0"))
                        songTrack = Integer.parseInt(track.substring(2));
                    else
                        songTrack = Integer.parseInt(track.substring(3));
                } else
                    songTrack = Integer.parseInt(track);

                int songDuration = data.getInt(durationColumn);
                songList.add(new Song(songId, songTitle, songArtist, songAlbum,
                        pathId, uri, songTrack, songDuration));
            }
            songAdt.swapCursor(data);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.i("TAGGING","RESTART...");
        songAdt.swapCursor(null);
    }
}