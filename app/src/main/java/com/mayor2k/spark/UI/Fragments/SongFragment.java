package com.mayor2k.spark.UI.Fragments;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.BaseColumns;
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

import com.mayor2k.spark.Adapters.SongAdapter;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

public class SongFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private RecyclerView songView;
    public static ArrayList<Song> songList = new ArrayList<>();
    public final static Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    private SongAdapter songAdt;
    private ArrayList<Long> songIds = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_song, container, false);
        songView = view.findViewById(R.id.trackList);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        SubMenu subMenu = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, "Grid size");
        subMenu.add(Menu.NONE, 1, Menu.NONE, "1");
        subMenu.add(Menu.NONE, 2, Menu.NONE, "2");
        subMenu.add(Menu.NONE, 3, Menu.NONE, "3");
        subMenu.add(Menu.NONE, 4, Menu.NONE, "4");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        SharedPreferences sPref = getActivity().getPreferences(MODE_PRIVATE);
        @SuppressLint("CommitPrefEdits") SharedPreferences.Editor ed = sPref.edit();
        switch (item.getItemId()) {
            case 1:
                ed.putInt("SongSpanCount", 1);
                ed.apply();
                onActivityCreated(null);
                return true;
            case 2:
                ed.putInt("SongSpanCount", 2);
                ed.apply();
                onActivityCreated(null);
                return true;
            case 3:
                ed.putInt("SongSpanCount", 3);
                ed.apply();
                onActivityCreated(null);
                return true;
            case 4:
                ed.putInt("SongSpanCount", 4);
                ed.apply();
                onActivityCreated(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        try {
            songAdt = new SongAdapter(songList, getActivity());
            songAdt.setHasStableIds(true);
            int spanCount;
            SharedPreferences sPref = getActivity().getPreferences(MODE_PRIVATE);
            if (!sPref.contains("SongSpanCount"))
                spanCount = 1;
            else
                spanCount = sPref.getInt("SongSpanCount", -1);

            if (spanCount == 1)
                songView.setLayoutManager(new LinearLayoutManager(getActivity()));
            else {
                songView.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
            }
            songView.setAdapter(songAdt);

        } catch (IllegalArgumentException e) {
            Toast.makeText(getActivity(), "Nothing found", Toast.LENGTH_LONG).show();
        }
        if (getLoaderManager().getLoader(0) == null)
            getLoaderManager().initLoader(0, null, this);
        else
            getLoaderManager().getLoader(0).onContentChanged();
    }

    public static final String[] COLUMNS = new String[]{
            MediaStore.Audio.Media._ID,//0
            MediaStore.Audio.Media.TITLE,//1
            MediaStore.Audio.Media.ALBUM,//2
            MediaStore.Audio.Media.ARTIST,//3
            MediaStore.Audio.Media.DATA,//4
            MediaStore.Audio.Media.ALBUM_ID,//5
            MediaStore.Audio.Media.TRACK,//6
            MediaStore.Audio.Media.DURATION//7
    };

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), musicUri, COLUMNS, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data==null)
            return;
        Runnable asyncLoader = () -> {
            if (data.moveToFirst()) {
                for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
                    long songId = data.getLong(0);
                    if (!songIds.contains(songId))
                        songIds.add(songId);
                    else
                        continue;
                    String songTitle = data.getString(1);
                    String songArtist = data.getString(3);
                    String songAlbum = data.getString(2);
                    String pathId = data.getString(4);
                    long songCover = data.getLong(5);
                    Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                    Uri uri = ContentUris.withAppendedId(sArtworkUri, songCover);
                    String track = data.getString(6);
                    int songTrack;
                    if (track.length() == 4) {
                        if (!Objects.equals(track.substring(2), "0"))
                            songTrack = Integer.parseInt(track.substring(2));
                        else
                            songTrack = Integer.parseInt(track.substring(3));
                    } else
                        songTrack = Integer.parseInt(track);

                    int songDuration = data.getInt(7);
                    Song song = new Song(songId, songTitle, songArtist, songAlbum,
                            pathId, uri, songTrack, songDuration);
                    songList.add(data.getPosition(),song);
                }
            }
        };
        getActivity().runOnUiThread(asyncLoader);
        songAdt.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        songAdt.swapCursor(null);
    }
}
