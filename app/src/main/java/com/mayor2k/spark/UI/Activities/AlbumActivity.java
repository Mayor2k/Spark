package com.mayor2k.spark.UI.Activities;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.ImageView;
import android.support.v7.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.florent37.glidepalette.GlidePalette;
import com.mayor2k.spark.Adapters.CustomAdapter;
import com.mayor2k.spark.Models.Album;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;

import java.util.ArrayList;
import java.util.Objects;

import static com.mayor2k.spark.UI.Activities.MainActivity.currentAlbum;
import static com.mayor2k.spark.UI.Fragments.AlbumFragment.albumList;
import static com.mayor2k.spark.UI.Fragments.SongFragment.musicUri;
import static com.mayor2k.spark.UI.Fragments.SongFragment.songList;

public class AlbumActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    Album album = albumList.get(currentAlbum);
    public ArrayList<Song> albumSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.albumCollapsing);
        collapsingToolbarLayout.setTitle(album.getTitle());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        }
        ImageView albumCover = findViewById(R.id.albumCover);
        RecyclerView trackList = findViewById(R.id.trackList);
        albumSongs = new ArrayList<>();
        CustomAdapter customAdapter = new CustomAdapter(albumSongs);

        Glide.with(this)
                .load(album.getUri())
                .apply(new RequestOptions()
                        .override(Target.SIZE_ORIGINAL)
                        .error(R.drawable.ic_album_black_24dp)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                )
                .listener(GlidePalette.with(String.valueOf(album.getUri()))
                        .use(GlidePalette.Profile.MUTED)
                        .intoCallBack(
                                new GlidePalette.CallBack() {
                                    @Override
                                    public void onPaletteLoaded(@Nullable Palette palette) {
                                        int color = palette.getMutedColor(0);
                                        collapsingToolbarLayout.setContentScrimColor(color);
                                        collapsingToolbarLayout.setStatusBarScrimColor(color);

                                    }
                                })
                )
                .into(albumCover);
        trackList.setLayoutManager(new LinearLayoutManager(this));
        trackList.setAdapter(customAdapter);
        getSupportLoaderManager().initLoader(1,null,this);
    }

    private final String[] COLUMNS = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.Media.TITLE
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, musicUri, COLUMNS, null, null,
                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
            int i = data.getPosition();
            int albumColumn = data.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM);
            String dataAlbum = data.getString(albumColumn);
            if (Objects.equals(dataAlbum, album.getTitle())){
                Song song = songList.get(i);
                albumSongs.add(song);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}