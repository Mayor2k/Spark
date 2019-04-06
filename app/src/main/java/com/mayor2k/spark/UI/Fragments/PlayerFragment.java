package com.mayor2k.spark.UI.Fragments;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.mayor2k.spark.Interfaces.Constants;
import com.mayor2k.spark.MusicService;
import com.mayor2k.spark.R;
import com.mayor2k.spark.UI.Activities.MainActivity;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.mayor2k.spark.Adapters.SongAdapter.serviceIntent;
import static com.mayor2k.spark.Adapters.SongAdapter.songPosition;
import static com.mayor2k.spark.MusicService.isShuffle;
import static com.mayor2k.spark.MusicService.pausePosition;
import static com.mayor2k.spark.MusicService.playSong;
import static com.mayor2k.spark.MusicService.player;
import static com.mayor2k.spark.UI.Activities.MainActivity.playArray;
import static com.mayor2k.spark.UI.Fragments.BottomPlayerFragment.isFirstClick;

public class PlayerFragment extends Fragment {
    public ImageView trackCover;
    public SeekBar seekBar;
    public ImageButton prev,next,shuffle;
    private FloatingActionButton play;
    public TextView title,artist,timeStart,timeEnd;
    private ProgressBar progressBar;
    private GestureDetectorCompat gestureDetector;
    private FrameLayout frameLayout;

    public MusicService.MusicServiceBinder musicServiceBinder;
    public MediaControllerCompat mediaController;
    public Handler handler = new Handler();
    public Runnable runnable;
    public Toolbar fragmentToolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        prev = view.findViewById(R.id.playerPrev);
        play = view.findViewById(R.id.playerPlay);
        next = view.findViewById(R.id.playerNext);
        shuffle = view.findViewById(R.id.playerShuffle);
        timeStart = view.findViewById(R.id.seekBarTimeStart);
        timeEnd = view.findViewById(R.id.seekBarTimeEnd);
        frameLayout = view.findViewById(R.id.defaultState);
        progressBar = view.findViewById(R.id.progress);
        trackCover = view.findViewById(R.id.trackCover);
        title = view.findViewById(R.id.playerTitle);
        artist = view.findViewById(R.id.playerArtist);
        seekBar = view.findViewById(R.id.seekBar);
        fragmentToolbar = view.findViewById(R.id.playerToolbar);
        return view;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(false);
        if(!isShuffle)
            shuffle.setColorFilter(getResources().getColor(R.color.black_p50));
        else
            shuffle.setColorFilter(getResources().getColor(R.color.white));
        gestureDetector = new GestureDetectorCompat(getActivity(), new GestureListener());
        trackCover.setOnClickListener(v -> {});
        trackCover.setOnTouchListener(touchListener);
        getActivity().bindService(new Intent(getActivity(), MusicService.class), mConnection, BIND_AUTO_CREATE);


        prev.setOnClickListener(v -> mediaController.getTransportControls().skipToPrevious());
        play.setOnClickListener(v -> {
            if(player.isPlaying())
                mediaController.getTransportControls().pause();
            else
                if (isFirstClick){
                    songPosition = playArray.indexOf(playSong);
                    serviceIntent.setAction(Constants.STARTFOREGROUND_ACTION);
                    getActivity().startService(serviceIntent);
                    mediaController.getTransportControls().play();
                    isFirstClick = false;
                }else
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
            public void onStartTrackingTouch(SeekBar seekBar) {
                player.seekTo(pausePosition);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                pausePosition = seekBar.getProgress();
                player.seekTo(pausePosition);
                progressListener();
            }
        });
        //progressListener();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            musicServiceBinder = (MusicService.MusicServiceBinder) service;
            try {
                mediaController = new MediaControllerCompat(
                        getActivity(), musicServiceBinder.getMediaSessionToken());
                mediaController.registerCallback(
                        new MediaControllerCompat.Callback() {
                            @Override
                            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                                updateView();
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
    };

    @SuppressLint("DefaultLocale")
    public void progressListener(){
        try{
            if (player.isPlaying()){
                seekBar.setProgress(player.getCurrentPosition());
                progressBar.setProgress(player.getCurrentPosition());
                seekBar.setMax(player.getDuration());
                progressBar.setMax(player.getDuration());

                timeStart.setText(String.format("%d.%02d", TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition()),
                        TimeUnit.MILLISECONDS.toSeconds(player.getCurrentPosition()%60000)));

                timeEnd.setText(String.format("%d.%02d", TimeUnit.MILLISECONDS.toMinutes(player.getDuration()),
                        TimeUnit.MILLISECONDS.toSeconds(player.getDuration()%60000)));
            }else {
                seekBar.setProgress(pausePosition);
                progressBar.setProgress(pausePosition);
                seekBar.setMax(playSong.getDuration());
                progressBar.setMax(playSong.getDuration());

                timeStart.setText(String.format("%d.%02d", TimeUnit.MILLISECONDS.toMinutes(pausePosition),
                        TimeUnit.MILLISECONDS.toSeconds(pausePosition%60000)));

                timeEnd.setText(String.format("%d.%02d", TimeUnit.MILLISECONDS.toMinutes(playSong.getDuration()),
                        TimeUnit.MILLISECONDS.toSeconds(playSong.getDuration()%60000)));
            }
            play.setImageResource(!player.isPlaying()?R.drawable.ic_play_24dp:R.drawable.ic_pause_24dp);
        }catch (IllegalStateException e){
            e.printStackTrace();
            handler.removeCallbacks(runnable);
        }

        runnable = this::progressListener;
        handler.postDelayed(runnable,100);
    }

    public void updateView(){
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
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        getActivity().unbindService(mConnection);
    }

    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gestureDetector.onTouchEvent(event);

        }
    };

    private void hideSystemUI() {
        View decorView = getActivity().getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
    private void showSystemUI() {
        View decorView = getActivity().getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
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
                if (isFirstClick){
                    songPosition = playArray.indexOf(playSong);
                    serviceIntent.setAction(Constants.STARTFOREGROUND_ACTION);
                    getActivity().startService(serviceIntent);
                    mediaController.getTransportControls().play();
                    isFirstClick = false;
                }else
                    mediaController.getTransportControls().play();
            return true;
        }
    }
}