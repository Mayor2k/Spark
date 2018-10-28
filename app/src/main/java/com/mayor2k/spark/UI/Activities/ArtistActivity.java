package com.mayor2k.spark.UI.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.elmargomez.typer.Font;
import com.elmargomez.typer.Typer;
import com.jaeger.library.StatusBarUtil;

import static com.mayor2k.spark.Adapters.AlbumAdapter.currentAlbum;
import static com.mayor2k.spark.Adapters.SongAdapter.songPosition;
import com.mayor2k.spark.Adapters.ArtistActivityAdapter;
import com.mayor2k.spark.Adapters.HorizontalRecyclerViewAdapter;
import com.mayor2k.spark.Interfaces.Constants;
import com.mayor2k.spark.Models.Album;
import com.mayor2k.spark.Models.Artist;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import static com.mayor2k.spark.Adapters.SongAdapter.serviceIntent;
import static com.mayor2k.spark.UI.Fragments.AlbumFragment.albumList;
import static com.mayor2k.spark.UI.Fragments.ArtistFragment.artistList;
import static com.mayor2k.spark.UI.Fragments.SongFragment.songList;

public class ArtistActivity extends AppCompatActivity {
    static public ArrayList<Song> artistSongs;
    static public ArrayList<Album> artistAlbums;
    public static LinearLayout horizontalScrollView;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);
        StatusBarUtil.setTransparent(this);

        // status bar height
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        // action bar height
        int actionBarHeight;
        final TypedArray styledAttributes = this.getTheme().obtainStyledAttributes(
                new int[]{android.R.attr.actionBarSize}
        );
        actionBarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        View gradient = findViewById(R.id.gradientView);
        gradient.setLayoutParams(new CollapsingToolbarLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                actionBarHeight + statusBarHeight));

        Artist currentArtist = artistList.get(getIntent().getIntExtra("currentArtist", -1));

        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.artistCollapsing);

        collapsingToolbarLayout.setTitle(currentArtist.getTitle());
        Typeface font = Typer.set(this).getFont(Font.ROBOTO_MEDIUM);

        final FloatingActionButton actionButton = findViewById(R.id.action_button);

        actionButton.setOnClickListener(v -> {
            Collections.shuffle(artistSongs);
            songPosition = 0;
            serviceIntent.setAction(Constants.START_ARTIST_ACTION);
            v.getContext().startService(serviceIntent);
        });

        collapsingToolbarLayout.setExpandedTitleTypeface(font);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_24dp_white);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        ImageView albumCover = findViewById(R.id.artistCover);
        RecyclerView trackList = findViewById(R.id.trackList);
        RecyclerView scrollAlbum = findViewById(R.id.scrollAlbum);
        artistSongs = new ArrayList<>();
        artistAlbums = new ArrayList<>();
        ArrayList<Song> songs = new ArrayList<>();
        for (int i = 0; albumList.size() > i; i++) {
            Album album = albumList.get(i);
            if (Objects.equals(album.getArtist(), currentArtist.getTitle())) {
                for (int count = 0; songList.size() > count; count++) {
                    Song song = songList.get(count);
                    if (Objects.equals(song.getAlbum(), album.getTitle()))
                        songs.add(song);
                }
                Collections.sort(songs, (item, t1) -> item.getTrack() - t1.getTrack());
                artistSongs.addAll(songs);
                songs.clear();
                artistAlbums.add(album);
            }
        }
        ArtistActivityAdapter customAdapter = new ArtistActivityAdapter(artistSongs);
        HorizontalRecyclerViewAdapter horizontalRecyclerViewAdapter = new HorizontalRecyclerViewAdapter(this, artistAlbums);

        Glide.with(this)
                .asBitmap()
                .load(currentArtist.getUrl())
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                )
                .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        albumCover.setImageBitmap(resource);
                        Palette p = Palette.from(resource).generate();
                        int color = p.getMutedColor(p.getVibrantColor(p.getDominantColor(0)));
                        collapsingToolbarLayout.setContentScrimColor(color);
                        collapsingToolbarLayout.setStatusBarScrimColor(color);
                        actionButton.setBackgroundTintList(ColorStateList.valueOf(color));
                        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_24dp_white);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        albumCover.setImageResource(R.drawable.album);
                        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_24dp_white);
                        collapsingToolbarLayout.setContentScrimColor(Color.parseColor("#cccccc"));
                        collapsingToolbarLayout.setStatusBarScrimColor(Color.parseColor("#cccccc"));

                    }
                });
        trackList.setNestedScrollingEnabled(false);
        trackList.setLayoutManager(new LinearLayoutManager(this));
        trackList.setAdapter(customAdapter);
        /*trackList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = recyclerView.getChildCount();//смотрим сколько элементов на экране
                int totalItemCount = recyclerView.getItemCount();//сколько всего элементов
                int firstVisibleItems = recyclerView.findFirstVisibleItemPosition();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });*/

        scrollAlbum.setNestedScrollingEnabled(false);
        scrollAlbum.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        scrollAlbum.setAdapter(horizontalRecyclerViewAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}