package com.mayor2k.spark.UI.Activities;

import android.database.Cursor;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.florent37.glidepalette.GlidePalette;
import com.mayor2k.spark.Models.Album;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;

import java.util.ArrayList;
import java.util.Objects;

import static com.mayor2k.spark.UI.Activities.MainActivity.currentAlbum;
import static com.mayor2k.spark.UI.Fragments.AlbumFragment.albumList;
import static com.mayor2k.spark.UI.Fragments.SongFragment.musicUri;

public class AlbumActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    Album album = albumList.get(currentAlbum);
    private SimpleCursorAdapter cursorAdapter;
    private ArrayList<Song> albumSongs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        ImageView albumCover = findViewById(R.id.albumCover);
        final TextView albumTitle = findViewById(R.id.albumTitle);
        LinearLayout titleArea = findViewById(R.id.titleArea);
        ListView trackList = findViewById(R.id.trackList);
        albumTitle.setText(album.getTitle());
        cursorAdapter = new SimpleCursorAdapter(this,R.layout.song_item,null,
                new String[] { MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.DURATION,MediaStore.Audio.Media.TRACK},
                new int[] { R.id.songName, R.id.songDuration, R.id.songNumber }, 1);
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
                                        albumTitle.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                                    }
                                })
                        .intoBackground(titleArea)
                )
                .into(albumCover);
        trackList.setAdapter(cursorAdapter);
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
            int albumColumn = data.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM);
            String dataAlbum = data.getString(albumColumn);
            Log.i("TAGGING","is "+Objects.equals(dataAlbum, album.getTitle()));
            if (Objects.equals(dataAlbum, album.getTitle()))
                Log.i("make","sth");
        }
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }
}
