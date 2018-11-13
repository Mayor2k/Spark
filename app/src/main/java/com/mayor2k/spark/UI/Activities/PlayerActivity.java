package com.mayor2k.spark.UI.Activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.jaeger.library.StatusBarUtil;
import com.mayor2k.spark.Models.Album;
import com.mayor2k.spark.Models.Artist;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;
import com.mayor2k.spark.Services.MusicService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.mayor2k.spark.Adapters.AlbumAdapter.currentAlbum;
import static com.mayor2k.spark.Services.MusicService.isShuffle;
import static com.mayor2k.spark.Services.MusicService.pausePosition;
import static com.mayor2k.spark.Services.MusicService.playSong;
import static com.mayor2k.spark.Services.MusicService.player;
import static com.mayor2k.spark.UI.Activities.MainActivity.playArray;
import static com.mayor2k.spark.UI.Fragments.AlbumFragment.albumList;
import static com.mayor2k.spark.UI.Fragments.ArtistFragment.artistList;

public class PlayerActivity extends AppCompatActivity {
    public ImageView trackCover;
    public SeekBar seekBar;
    public ImageButton prev,next,shuffle;
    private FloatingActionButton play;
    public TextView title,artist,timeStart,timeEnd;
    private ProgressBar progressBar;
    private GestureDetectorCompat
            gestureDetector;
    private FrameLayout frameLayout;

    public MusicService.MusicServiceBinder musicServiceBinder;
    public MediaControllerCompat mediaController;
    public Handler handler = new Handler();
    public Runnable runnable;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setTransparent(this);
        setContentView(R.layout.activity_player);
        //setDragEdge(SwipeBackLayout.DragEdge.TOP);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        prev = findViewById(R.id.playerPrev);
        play = findViewById(R.id.playerPlay);
        next = findViewById(R.id.playerNext);
        shuffle = findViewById(R.id.playerShuffle);
        if(!isShuffle)
            shuffle.setColorFilter(getResources().getColor(R.color.black_p50));
        else
            shuffle.setColorFilter(getResources().getColor(R.color.white));

        timeStart = findViewById(R.id.seekBarTimeStart);
        timeEnd = findViewById(R.id.seekBarTimeEnd);
        frameLayout = findViewById(R.id.defaultState);
        progressBar = findViewById(R.id.progress);

        trackCover = findViewById(R.id.trackCover);
        gestureDetector = new GestureDetectorCompat(this, new GestureListener());
        trackCover.setOnClickListener(v -> {});
        trackCover.setOnTouchListener(touchListener);

        title = findViewById(R.id.playerTitle);
        artist = findViewById(R.id.playerArtist);
        seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(player.getDuration());
        seekBar.setProgress(player.getCurrentPosition());
        updateView();
        progressListener();

        bindService(new Intent(this, MusicService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                musicServiceBinder = (MusicService.MusicServiceBinder) service;
                try {
                    mediaController = new MediaControllerCompat(
                            PlayerActivity.this, musicServiceBinder.getMediaSessionToken());
                    mediaController.registerCallback(
                            new MediaControllerCompat.Callback() {
                                @Override
                                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                                    updateView();
                                    if (state == null)
                                        return;
                                    boolean playing =
                                            state.getState() == PlaybackStateCompat.STATE_PLAYING;
                                    play.setEnabled(player.isPlaying() == playing);
                                    prev.setEnabled(player.isPlaying() == playing);
                                    next.setEnabled(player.isPlaying() == playing);
                                }
                            }
                    );
                }
                catch (RemoteException e) {
                    mediaController = null;
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                musicServiceBinder = null;
                mediaController = null;
            }
        }, BIND_AUTO_CREATE);

        prev.setOnClickListener(v -> mediaController.getTransportControls().skipToPrevious());

        play.setOnClickListener(v -> {
            if(player.isPlaying())
                mediaController.getTransportControls().pause();
            else
                mediaController.getTransportControls().play();
        });

        next.setOnClickListener(v -> mediaController.getTransportControls().skipToNext());

        shuffle.setOnClickListener(v -> {
            if(!isShuffle){
                isShuffle = true;
                Collections.shuffle(playArray);
                shuffle.setColorFilter(getResources().getColor(R.color.white));
            }else{
                isShuffle = false;
                shuffle.setColorFilter(getResources().getColor(R.color.black_p50));
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                pausePosition = seekBar.getProgress();
                player.seekTo(pausePosition);
                progressListener();
            }
        });
    }
    public void menuItemListener(MenuItem item) {
        switch (item.getItemId()){
            case (R.id.findLyrics):
                Toast.makeText(this,"lyrics",Toast.LENGTH_SHORT).show();
                break;
            case (R.id.addToPlaylist):
                Toast.makeText(this,"playlist",Toast.LENGTH_SHORT).show();
                break;
            case (R.id.goToQueue):
                startActivity(new Intent(PlayerActivity.this, QueueActivity.class));
                break;
            case (R.id.goToAlbum):
                for (int i=0;albumList.size()>i;i++){
                    Album album = albumList.get(i);
                    if (Objects.equals(album.getTitle(), playSong.getAlbum())) {
                        currentAlbum = albumList.indexOf(album);
                        break;
                    }
                }
                startActivity(new Intent(PlayerActivity.this, AlbumActivity.class));
                break;
            case (R.id.goToArtist):
                Intent intent = new Intent(PlayerActivity.this, ArtistActivity.class);
                for (int i=0;artistList.size()>i;i++){
                    Artist artist = artistList.get(i);
                    if (Objects.equals(artist.getTitle(), playSong.getArtist())) {
                        intent.putExtra("currentArtist",artistList.indexOf(artist));
                        break;
                    }
                }
                startActivity(intent);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.player_navigation, menu);
        return true;
    }

    @SuppressLint("DefaultLocale")
    public void progressListener(){
        seekBar.setProgress(player.getCurrentPosition());
        seekBar.setMax(player.getDuration());
        progressBar.setProgress(player.getCurrentPosition());
        progressBar.setMax(player.getDuration());

        timeStart.setText(String.format("%d.%02d", TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition()),
                TimeUnit.MILLISECONDS.toSeconds(player.getCurrentPosition()%60000)));

        timeEnd.setText(String.format("%d.%02d", TimeUnit.MILLISECONDS.toMinutes(player.getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(player.getDuration()%60000)));

        play.setImageResource(!player.isPlaying()?R.drawable.ic_play_24dp:R.drawable.ic_pause_24dp);

        runnable = this::progressListener;
        handler.postDelayed(runnable,100);
    }

    private void updateView(){
        try{
            Glide.with(trackCover.getContext())
                    .asBitmap()
                    .load(playSong.getUri())
                    .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            trackCover.setImageBitmap(resource);
                            Palette p = Palette.from(resource).generate();
                            int color = p.getMutedColor(p.getVibrantColor(p.getDominantColor(0)));
                            play.setBackgroundTintList(ColorStateList.valueOf(color));
                            seekBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                            seekBar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                            progressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            trackCover.setImageResource(R.drawable.album);
                            seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.silver), PorterDuff.Mode.SRC_IN);
                            seekBar.getThumb().setColorFilter(getResources().getColor(R.color.silver), PorterDuff.Mode.SRC_IN);
                            progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.silver), PorterDuff.Mode.SRC_IN);
                            play.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.silver)));
                        }
                    });
            title.setText(playSong.getTitle());
            artist.setText(playSong.getArtist());
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);

        }
    };

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (frameLayout.getVisibility()==View.VISIBLE){
                frameLayout.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                hideSystemUI();
            }
            else{
                frameLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                showSystemUI();
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (player.isPlaying()) {
                mediaController.getTransportControls().pause();
                frameLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                showSystemUI();
            }
            else
                mediaController.getTransportControls().play();
            return true;
        }
    }
}
