package com.mayor2k.spark.UI.Activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;

import android.content.res.ColorStateList;
import android.content.res.TypedArray;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.jaeger.library.StatusBarUtil;
import com.liuguangqiang.swipeback.SwipeBackActivity;
import com.liuguangqiang.swipeback.SwipeBackLayout;
import com.mayor2k.spark.R;
import com.mayor2k.spark.Services.MusicService;

import java.util.concurrent.TimeUnit;

import static com.mayor2k.spark.Services.MusicService.pausePosition;
import static com.mayor2k.spark.Services.MusicService.playSong;
import static com.mayor2k.spark.Services.MusicService.player;

public class PlayerActivity extends AppCompatActivity {
    public ImageView trackCover;
    public SeekBar seekBar;
    public ImageButton prev,next;
    private FloatingActionButton play;
    public TextView title,artist,timeStart,timeEnd;
    private LinearLayout playerArea;

    public MusicService.MusicServiceBinder musicServiceBinder;
    public MediaControllerCompat mediaController;
    public Handler handler = new Handler();
    public Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setTransparent(this);
        setContentView(R.layout.activity_player);
        //setDragEdge(SwipeBackLayout.DragEdge.TOP);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_24dp_white);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

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

        Log.i("tagging",""+actionBarHeight);
        //View gradient = findViewById(R.id.gradientView);
        //gradient.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,actionBarHeight+statusBarHeight));

        prev = findViewById(R.id.playerPrev);
        play = findViewById(R.id.playerPlay);
        next = findViewById(R.id.playerNext);

        timeStart = findViewById(R.id.seekBarTimeStart);
        timeEnd = findViewById(R.id.seekBarTimeEnd);

        trackCover = findViewById(R.id.trackCover);
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


    @SuppressLint("DefaultLocale")
    public void progressListener(){
        seekBar.setProgress(player.getCurrentPosition());
        seekBar.setMax(player.getDuration());

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
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            trackCover.setImageResource(R.drawable.cover);
                            seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.silver), PorterDuff.Mode.SRC_IN);
                            seekBar.getThumb().setColorFilter(getResources().getColor(R.color.silver), PorterDuff.Mode.SRC_IN);
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
}