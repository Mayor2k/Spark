package com.mayor2k.spark.UI.Activities;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.support.v7.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.elmargomez.typer.Font;
import com.elmargomez.typer.Typer;
import com.github.florent37.glidepalette.GlidePalette;
import com.jaeger.library.StatusBarUtil;
import com.mayor2k.spark.Adapters.CustomAdapter;
import com.mayor2k.spark.Interfaces.Constants;
import com.mayor2k.spark.Models.Album;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;

import java.util.ArrayList;
import java.util.Objects;

import static com.mayor2k.spark.Adapters.SongAdapter.serviceIntent;
import static com.mayor2k.spark.Adapters.SongAdapter.songPosition;
import static com.mayor2k.spark.UI.Activities.MainActivity.currentAlbum;
import static com.mayor2k.spark.UI.Fragments.AlbumFragment.albumList;
import static com.mayor2k.spark.UI.Fragments.SongFragment.musicUri;

public class AlbumActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    Album album = albumList.get(currentAlbum);
    public static ArrayList<Song> albumSongs;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        StatusBarUtil.setTransparent(this);

        // status bar height
        int statusBarHeight=0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        // action bar height
        int actionBarHeight;
        final TypedArray styledAttributes = this.getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize }
        );
        actionBarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        View gradient = findViewById(R.id.gradientView);
        gradient.setLayoutParams(new CollapsingToolbarLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                actionBarHeight+statusBarHeight));

        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.albumCollapsing);
        collapsingToolbarLayout.setTitle(album.getTitle());
        Typeface font = Typer.set(this).getFont(Font.ROBOTO_MEDIUM);

        final FloatingActionButton actionButton = findViewById(R.id.action_button);

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                songPosition = 0;
                serviceIntent.setAction(Constants.START_ALBUM_ACTION);
                v.getContext().startService(serviceIntent);
            }
        });

        collapsingToolbarLayout.setExpandedTitleTypeface(font);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_24dp_white);
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
                                        actionButton.setBackgroundTintList(ColorStateList.valueOf(color));
                                        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_24dp_white);
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
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DURATION
    };

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = MediaStore.Audio.Albums.ALBUM + "=?";
        String [] selectionArgs = {album.getTitle()};
        String sortOrder = MediaStore.Audio.AudioColumns.ARTIST + " ASC, "
                + MediaStore.Audio.AudioColumns.TRACK + " ASC";
        return new CursorLoader(AlbumActivity.this, musicUri, COLUMNS, selection,
                selectionArgs, sortOrder);
    }
    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
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
            albumSongs.add(new Song(songId, songTitle, songArtist, songAlbum,
                    pathId, uri, songTrack, songDuration));
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}