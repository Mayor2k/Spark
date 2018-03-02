package com.mayor2k.spark.UI.Activities;

import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.github.florent37.glidepalette.GlidePalette;
import com.mayor2k.spark.Models.Album;
import com.mayor2k.spark.R;

import java.lang.annotation.Target;

import static com.mayor2k.spark.UI.Activities.MainActivity.currentAlbum;
import static com.mayor2k.spark.UI.Fragments.AlbumFragment.albumList;

public class AlbumActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    Album album = albumList.get(currentAlbum);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        ImageView albumCover = findViewById(R.id.albumCover);
        ListView trackList = findViewById(R.id.trackList);
        final TextView albumTitle = findViewById(R.id.albumTitle);
        LinearLayout titleArea = findViewById(R.id.titleArea);
        albumTitle.setText(album.getTitle());

        Glide.with(this)
                .load(album.getUri())
                .apply(new RequestOptions()
                        .override(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
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
        //getLoaderManager().initLoader(1,null,null);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
