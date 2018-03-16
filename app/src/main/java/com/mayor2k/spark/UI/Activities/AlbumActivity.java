package com.mayor2k.spark.UI.Activities;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.Typeface;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.support.v7.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.elmargomez.typer.Font;
import com.elmargomez.typer.Typer;
import com.github.florent37.glidepalette.GlidePalette;
import com.mayor2k.spark.Adapters.CustomAdapter;
import com.mayor2k.spark.Models.Album;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;

import java.util.ArrayList;

import static com.mayor2k.spark.UI.Activities.MainActivity.currentAlbum;
import static com.mayor2k.spark.UI.Fragments.AlbumFragment.albumList;
import static com.mayor2k.spark.UI.Fragments.SongFragment.musicUri;
import static com.mayor2k.spark.UI.Fragments.SongFragment.songList;

public class AlbumActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    Album album = albumList.get(currentAlbum);
    public ArrayList<Song> albumSongs;

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        //getting status bar height
        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        int contentViewTop =
                window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight= contentViewTop - statusBarHeight;

        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.albumCollapsing);
        collapsingToolbarLayout.setTitle(album.getTitle());
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.drawable.gradient);
        Typeface font = Typer.set(this).getFont(Font.ROBOTO_MEDIUM);
        collapsingToolbarLayout.setExpandedTitleTypeface(font);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        }
        View gradientView = findViewById(R.id.gradientView);
        CollapsingToolbarLayout.LayoutParams params = (CollapsingToolbarLayout.LayoutParams) gradientView.getLayoutParams();
        params.height = toolbar.getHeight()+titleBarHeight;
        gradientView.setLayoutParams(params);

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
            MediaStore.Audio.AudioColumns.TRACK,
            MediaStore.Audio.AudioColumns.ARTIST,
            MediaStore.MediaColumns.TITLE,
            MediaStore.Audio.Albums.ALBUM
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = MediaStore.Audio.Albums.ALBUM + "=?";
        String [] selectionArgs = {album.getTitle()};
        String sortOrder = MediaStore.Audio.AudioColumns.ARTIST + " ASC, "
                + MediaStore.Audio.AudioColumns.TRACK + " ASC";
        return new CursorLoader(AlbumActivity.this, musicUri, COLUMNS, selection,
                selectionArgs,sortOrder);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        for (data.moveToFirst(); !data.isAfterLast(); data.moveToNext()) {
            int titleColumn = data.getColumnIndex(MediaStore.MediaColumns.TITLE);
            String title = data.getString(titleColumn);
            Log.i("tagging","is "+title);
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