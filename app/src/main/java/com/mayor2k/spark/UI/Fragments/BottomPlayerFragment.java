package com.mayor2k.spark.UI.Fragments;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.MusicService;
import com.mayor2k.spark.R;

import java.io.Serializable;
import java.util.ArrayList;

import static android.content.Context.BIND_AUTO_CREATE;
import static com.mayor2k.spark.MusicService.playSong;
import static com.mayor2k.spark.MusicService.player;
import static com.mayor2k.spark.UI.Activities.MainActivity.isPlayerOpen;
import static com.mayor2k.spark.UI.Activities.MainActivity.playArray;

public class BottomPlayerFragment extends Fragment {
    private TextView songTitle;
    private ImageButton playButton;
    private ProgressBar progressBar;
    private Handler handler = new Handler();
    ServiceConnection serviceConnection;
    Runnable runnable;

    private MusicService.MusicServiceBinder musicServiceBinder;
    private MediaControllerCompat mediaController;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_player, container, false);
        songTitle = view.findViewById(R.id.bottom_player_song_title);
        playButton = view.findViewById(R.id.bottom_player_play);
        progressBar = view.findViewById(R.id.progressBar);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        progressListener();
        playButton.setOnClickListener(v -> {
            if(player.isPlaying())
                mediaController.getTransportControls().pause();
            else
                mediaController.getTransportControls().play();
        });
        ServiceConnection serviceConnection = new ServiceConnection(){

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
                                    if (state==null)
                                        return;
                                    songTitle.setText(playSong.getTitle());
                                }

                                @Override
                                public void onSessionEvent(String event, Bundle extras) {
                                    super.onSessionEvent(event, extras);
                                    Log.i("tagstate",""+event);
                                }
                            }
                    );
                }
                catch (RemoteException |IllegalArgumentException e) {
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
        getActivity().bindService(new Intent(getActivity(), MusicService.class), serviceConnection,BIND_AUTO_CREATE);
    }

    public void progressListener(){
        if (player.isPlaying())
            progressBar.setProgress(player.getCurrentPosition());
        progressBar.setMax(player.getDuration());
        playButton.setImageResource(!player.isPlaying()?R.drawable.ic_play_black_24dp:R.drawable.ic_pause_24dp_black);
        runnable = this::progressListener;
        if (isPlayerOpen)
            playButton.setVisibility(View.GONE);
        else
            playButton.setVisibility(View.VISIBLE);
        handler.postDelayed(runnable,100);
    }
}
