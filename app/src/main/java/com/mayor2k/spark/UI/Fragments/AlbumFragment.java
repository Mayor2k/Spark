package com.mayor2k.spark.UI.Fragments;

import android.content.ContentUris;
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

import com.mayor2k.spark.Adapters.AlbumAdapter;
import com.mayor2k.spark.Models.Album;
import com.mayor2k.spark.R;
import com.mayor2k.spark.UI.Activities.MainActivity;

import java.util.ArrayList;

import static android.support.constraint.Constraints.TAG;
import static com.mayor2k.spark.UI.Activities.MainActivity.toolbar;
import static com.mayor2k.spark.UI.Fragments.SongFragment.musicUri;

public class AlbumFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private RecyclerView albumView;
    private AlbumAdapter albumAdapter;
    public static ArrayList<Album> albumList;

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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case 1:
                Toast.makeText(getContext(),"asd",Toast.LENGTH_SHORT).show();
                return true;
            case 2:
                Log.i("TAGGING","2 IN CLICK");
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
            albumList = new ArrayList<>();
            albumAdapter = new AlbumAdapter(albumList, getContext());
            albumView.setLayoutManager(new GridLayoutManager(getActivity(),2));
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
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        albumAdapter.swapCursor(null);
    }
}