package com.mayor2k.spark.UI.Fragments;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mayor2k.spark.Adapters.AlbumAdapter;
import com.mayor2k.spark.Models.Album;
import com.mayor2k.spark.R;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.mayor2k.spark.UI.Fragments.SongFragment.musicUri;

public class AlbumFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private RecyclerView albumView;
    private AlbumAdapter albumAdapter;
    public static ArrayList<Album> albumList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        albumView = view.findViewById(R.id.albumRecyclerView);
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
                ed.putInt("AlbumSpanCount",1);
                ed.apply();
                onActivityCreated(null);
                return true;
            case 2:
                ed.putInt("AlbumSpanCount",2);
                ed.apply();
                onActivityCreated(null);
                return true;
            case 3:
                ed.putInt("AlbumSpanCount",3);
                ed.apply();
                onActivityCreated(null);
                return true;
            case 4:
                ed.putInt("AlbumSpanCount",4);
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
            albumAdapter = new AlbumAdapter(albumList, getContext(),getActivity());

            int spanCount;
            SharedPreferences sPref = getActivity().getPreferences(MODE_PRIVATE);
            if (!sPref.contains("AlbumSpanCount"))
                spanCount = 2;
            else
                spanCount = sPref.getInt("AlbumSpanCount", -1);

            if (spanCount==1)
                albumView.setLayoutManager(new LinearLayoutManager(getActivity()));
            else
                albumView.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
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

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), musicUri, COLUMNS, null, null,
                MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        ArrayList<String>checking = new ArrayList<>();
        albumList.clear();
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
            albumAdapter.notifyDataSetChanged();
            albumAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {}
}