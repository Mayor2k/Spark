package com.mayor2k.spark.UI.Activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;

import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.liuguangqiang.swipeback.SwipeBackActivity;
import com.liuguangqiang.swipeback.SwipeBackLayout;
import com.mayor2k.spark.R;
import com.mayor2k.spark.Services.MusicService;

import java.util.concurrent.TimeUnit;

import static com.mayor2k.spark.Services.MusicService.pausePosition;
import static com.mayor2k.spark.Services.MusicService.playSong;
import static com.mayor2k.spark.Services.MusicService.player;
import static com.mayor2k.spark.Utils.CoverUtil.getCoverBitmap;

public class PlayerActivity extends AppCompatActivity {
    public ImageView trackCover;
    public SeekBar seekBar;
//CHECK!
    public ImageButton prev;
    public ImageButton play;
    public ImageButton next;

    public TextView title;
    public TextView artist;
    public TextView timeStart;
    public TextView timeEnd;

    public MusicService.MusicServiceBinder musicServiceBinder;
    public MediaControllerCompat mediaController;
    public Handler handler = new Handler();
    public Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        //setDragEdge(SwipeBackLayout.DragEdge.TOP);
        prev = findViewById(R.id.playerPrev);
        play = findViewById(R.id.playerPlay);
        next = findViewById(R.id.playerNext);

        trackCover = findViewById(R.id.trackCover);
        title = findViewById(R.id.playerTitle);
        artist = findViewById(R.id.playerArtist);
        seekBar = findViewById(R.id.seekBar);
        //seekBar.setProgress(player.getCurrentPosition());

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



        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaController.getTransportControls().skipToPrevious();
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(player.isPlaying())
                    mediaController.getTransportControls().pause();
                else
                    mediaController.getTransportControls().play();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaController.getTransportControls().skipToNext();
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


    @SuppressLint("DefaultLocale")
    public void progressListener(){
        seekBar.setProgress(player.getCurrentPosition());
        seekBar.setMax(player.getDuration());

        timeStart = findViewById(R.id.seekBarTimeStart);
        timeStart.setText(String.format("%d.%02d", TimeUnit.MILLISECONDS.toMinutes(player.getCurrentPosition()),
                TimeUnit.MILLISECONDS.toSeconds(player.getCurrentPosition()%60000)));

        timeEnd = findViewById(R.id.seekBarTimeEnd);
        timeEnd.setText(String.format("%d.%02d", TimeUnit.MILLISECONDS.toMinutes(player.getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(player.getDuration()%60000)));

        play.setImageResource(!player.isPlaying()?R.drawable.ic_play_24dp:R.drawable.ic_pause_24dp);

        runnable = new Runnable() {
            @Override
            public void run() {
                progressListener();
            }
        };
        handler.postDelayed(runnable,1000);
    }

    private void updateView(){
        try{
            Glide.with(trackCover.getContext())
                    .load(playSong.getUri())
                    .apply(new RequestOptions()
                            .override(Target.SIZE_ORIGINAL)
                            .error(R.drawable.cover)
                    )
                    .into(trackCover);
            title.setText(playSong.getTitle());
            artist.setText(playSong.getArtist());
        }catch (IllegalArgumentException e){
            e.printStackTrace();

        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }
}