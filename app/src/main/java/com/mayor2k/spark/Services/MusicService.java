package com.mayor2k.spark.Services;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Target;
import android.util.Log;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.NotificationTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.florent37.glidepalette.GlidePalette;
import com.mayor2k.spark.Interfaces.Constants;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;
import com.mayor2k.spark.UI.Activities.PlayerActivity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.mayor2k.spark.UI.Activities.AlbumActivity.albumSongs;
import static com.mayor2k.spark.UI.Activities.MainActivity.TAG;
import static com.mayor2k.spark.Adapters.SongAdapter.songPosition;
import static com.mayor2k.spark.UI.Activities.MainActivity.playArray;
import static com.mayor2k.spark.UI.Fragments.SongFragment.songList;
import static com.mayor2k.spark.Utils.CoverUtil.drawableToBitmap;
import static com.mayor2k.spark.Utils.CoverUtil.getCoverBitmap;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener {
    public static MediaPlayer player = new MediaPlayer();
    public static Song playSong;
    public static int pausePosition;
    public static boolean isQueue;
    public static int queuePosition;
    int audioFocusResult;
    public static NotificationManager mNotifyMgr;
    public MediaSessionCompat mediaSession;
    public MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
    final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
            .setActions(PlaybackStateCompat.ACTION_PLAY
                    | PlaybackStateCompat.ACTION_STOP
                    | PlaybackStateCompat.ACTION_PAUSE
                    | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
    public AudioManager audioManager;

    public MusicIntentReceiver myReceiver = new MusicIntentReceiver();

    @Override
    public void onCreate() {
        mediaSession = new MediaSessionCompat(this, TAG);
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(mediaSessionCallback);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "MyWakelockTag");
        wakeLock.acquire();
        //IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        //registerReceiver(myReceiver, filter);
        mediaSession.setActive(true);
        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(Constants.PREV_ACTION)) {
            mediaSessionCallback.onSkipToPrevious();
        } else if (intent.getAction().equals(Constants.PAUSE_ACTION)) {
            mediaSessionCallback.onPause();
        } else if (intent.getAction().equals(Constants.PLAY_ACTION)) {
            mediaSessionCallback.onPlay();
        } else if (intent.getAction().equals(Constants.NEXT_ACTION)) {
            mediaSessionCallback.onSkipToNext();
        } else if (intent.getAction().equals(Constants.STARTFOREGROUND_ACTION)) {
            playArray=songList;
            songStream(songPosition);
            showNotification(true);
        }else if (intent.getAction().equals(Constants.START_ALBUM_ACTION)) {
            playArray=albumSongs;
            songStream(songPosition);
            showNotification(true);
        }
        return START_NOT_STICKY;
    }

    public void songStream(int songPos) {
        try{
            player.reset();
        }catch (RuntimeException e){
            e.printStackTrace();
        }
        songPosition = songPos;
        playSong = playArray.get(songPosition);
        long currSong = playSong.getId();
        Log.i(TAG, "Artist " + playSong.getArtist() + " Song " + playSong.getTitle());
        Uri trackUri = ContentUris.withAppendedId
                (android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,currSong);
        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
        try{
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }catch (RuntimeException e){
            e.printStackTrace();
        }

        try {
            player.prepare();
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
        }
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaSessionCallback.onSkipToNext();
            }
        });

        MediaMetadataCompat.Builder metadata = metadataBuilder
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, getCoverBitmap(playSong,getApplicationContext()));
        mediaSession.setMetadata(metadata.build());

        startAudioFocus(AudioManager.AUDIOFOCUS_GAIN);

        mediaSession.setPlaybackState(
                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());

        mediaSession.setActive(true);
        player.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicServiceBinder();
    }

    @Override
    public boolean stopService(Intent name) {
        mNotifyMgr.cancelAll();
        return super.stopService(name);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void showNotification(Boolean isUpdate) {
        final RemoteViews views = new RemoteViews(getPackageName(), R.layout.notification);
        final RemoteViews viewsBig = new RemoteViews(getPackageName(), R.layout.notification_big);

        views.setTextViewText(R.id.status_bar_track_name, playSong.getTitle());
        views.setTextViewText(R.id.status_bar_artist_name, playSong.getArtist());

        views.setImageViewResource(R.id.status_bar_prev, isCover()?
                R.drawable.ic_previous_24dp:R.drawable.ic_previous_24dp_black);
        viewsBig.setImageViewResource(R.id.status_bar_prev, isCover()?
                R.drawable.ic_previous_24dp:R.drawable.ic_previous_24dp_black);

        viewsBig.setTextViewText(R.id.status_bar_track_name, playSong.getTitle());
        viewsBig.setTextViewText(R.id.status_bar_album_name, playSong.getAlbum());
        viewsBig.setTextViewText(R.id.status_bar_artist_name, playSong.getArtist());

        views.setImageViewResource(R.id.status_bar_next, isCover()?
                R.drawable.ic_next_24dp:R.drawable.ic_next_24dp_black);
        viewsBig.setImageViewResource(R.id.status_bar_next, isCover()?
                R.drawable.ic_next_24dp:R.drawable.ic_next_24dp_black);


        Intent notificationIntent = new Intent(getApplicationContext(), PlayerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.setAction(Constants.MAIN_ACTION);
        PendingIntent notification = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        mediaSession.setSessionActivity(
                PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0));

        Intent prevIntent = new Intent(getApplicationContext(), MusicService.class);
        prevIntent.setAction(Constants.PREV_ACTION);
        PendingIntent prev = PendingIntent.getService(getApplicationContext(), 1, prevIntent, 0);

        Intent pauseIntent = new Intent(getApplicationContext(), MusicService.class);
        pauseIntent.setAction(Constants.PAUSE_ACTION);
        PendingIntent pause = PendingIntent.getService(getApplicationContext(), 1, pauseIntent, 0);

        Intent playIntent = new Intent(getApplicationContext(), MusicService.class);
        playIntent.setAction(Constants.PLAY_ACTION);
        PendingIntent play = PendingIntent.getService(getApplicationContext(), 1, playIntent, 0);

        Intent nextIntent = new Intent(getApplicationContext(), MusicService.class);
        nextIntent.setAction(Constants.NEXT_ACTION);
        PendingIntent next = PendingIntent.getService(getApplicationContext(), 1, nextIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setCustomContentView(views)
                .setCustomBigContentView(viewsBig)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_play_24dp)
                .setContentIntent(notification)
                .setOngoing(player.isPlaying())
                .setPriority(player.isPlaying() ?
                        NotificationCompat.PRIORITY_MAX : NotificationCompat.PRIORITY_LOW);

        final NotificationTarget viewsTarget = new NotificationTarget(
                getApplicationContext(),
                R.id.status_bar_album_art,
                views,
                builder.build(), 1);

        final NotificationTarget viewsBigTarget = new NotificationTarget(
                getApplicationContext(),
                R.id.status_bar_album_art,
                viewsBig,
                builder.build(), 1);

        if (isUpdate){
            Glide.with(getApplicationContext())
                    .asBitmap()
                    .load(playSong.getUri())
                    .apply(new RequestOptions()
                            .override(NotificationTarget.SIZE_ORIGINAL)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                    )
                    .into(new SimpleTarget<Bitmap>(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL, com.bumptech.glide.request.target.Target.SIZE_ORIGINAL) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Palette p = Palette.from(resource).generate();
                            int color = p.getMutedColor(p.getVibrantColor(p.getDominantColor(-111)));
                            views.setInt(R.id.root,"setBackgroundColor", color);
                            viewsBig.setInt(R.id.root,"setBackgroundColor",color);

                            viewsBig.setTextColor(R.id.status_bar_track_name,
                                    ContextCompat.getColor(getApplicationContext(), R.color.white));
                            viewsBig.setTextColor(R.id.status_bar_album_name,
                                    ContextCompat.getColor(getApplicationContext(), R.color.white));
                            viewsBig.setTextColor(R.id.status_bar_artist_name,
                                    ContextCompat.getColor(getApplicationContext(), R.color.white));

                            views.setTextColor(R.id.status_bar_track_name,
                                    ContextCompat.getColor(getApplicationContext(), R.color.white));
                            views.setTextColor(R.id.status_bar_artist_name,
                                    ContextCompat.getColor(getApplicationContext(), R.color.white));

                            viewsTarget.onResourceReady(resource,null);
                            viewsBigTarget.onResourceReady(resource,null);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            views.setInt(R.id.root,"setBackgroundColor",
                                    ContextCompat.getColor(getApplicationContext(), R.color.transparent));
                            viewsBig.setInt(R.id.root,"setBackgroundColor",
                                    ContextCompat.getColor(getApplicationContext(), R.color.transparent));

                            viewsBig.setTextColor(R.id.status_bar_track_name,
                                    ContextCompat.getColor(getApplicationContext(), R.color.black));
                            viewsBig.setTextColor(R.id.status_bar_album_name,
                                    ContextCompat.getColor(getApplicationContext(), R.color.black));
                            viewsBig.setTextColor(R.id.status_bar_artist_name,
                                    ContextCompat.getColor(getApplicationContext(), R.color.black));

                            views.setTextColor(R.id.status_bar_track_name,
                                    ContextCompat.getColor(getApplicationContext(), R.color.black));
                            views.setTextColor(R.id.status_bar_artist_name,
                                    ContextCompat.getColor(getApplicationContext(), R.color.black));

                            viewsTarget.onResourceReady(BitmapFactory.decodeResource
                                    (getApplicationContext().getResources(),
                                    R.drawable.cover),null);
                            viewsBigTarget.onResourceReady(BitmapFactory.decodeResource
                                    (getApplicationContext().getResources(), R.drawable.cover),null);
                        }
                    });
        }

        views.setOnClickPendingIntent(R.id.status_bar_prev, prev);
        viewsBig.setOnClickPendingIntent(R.id.status_bar_prev, prev);

        if (isCover()){
            views.setImageViewResource(R.id.status_bar_play,
                    player.isPlaying() ? R.drawable.ic_pause_24dp : R.drawable.ic_play_24dp);
            viewsBig.setImageViewResource(R.id.status_bar_play,
                    player.isPlaying() ? R.drawable.ic_pause_24dp : R.drawable.ic_play_24dp);
        }else {
            views.setImageViewResource(R.id.status_bar_play,
                    player.isPlaying() ? R.drawable.ic_pause_24dp_black : R.drawable.ic_play_24dp_black);
            viewsBig.setImageViewResource(R.id.status_bar_play,
                    player.isPlaying() ? R.drawable.ic_pause_24dp_black : R.drawable.ic_play_24dp_black);
        }

        views.setOnClickPendingIntent(R.id.status_bar_play, player.isPlaying() ? pause : play);
        viewsBig.setOnClickPendingIntent(R.id.status_bar_play, player.isPlaying() ? pause : play);

        views.setOnClickPendingIntent(R.id.status_bar_next, next);
        viewsBig.setOnClickPendingIntent(R.id.status_bar_next, next);

        if (player.isPlaying()){
            startForeground(1,builder.build());
        }else{
            stopForeground(false);
            mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mNotifyMgr.notify(1, builder.build());
        }
    }

    @Override
    public void onDestroy() {
        player.stop();
        super.onDestroy();
    }

    public MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback() {
        @Override
        public void onSkipToPrevious() {
            if (player.getCurrentPosition() > 3000) {
                songStream(songPosition);
            } else {
                try {
                    if (isQueue) {
                        songStream(queuePosition);
                        isQueue = false;
                    }
                    else
                        songStream(songPosition - 1);
                } catch (ArrayIndexOutOfBoundsException e) {
                    songStream(0);
                }
            }
            startAudioFocus(AudioManager.AUDIOFOCUS_GAIN);
            showNotification(true);
            Log.i(TAG, "Clicked Previous");
            super.onSkipToPrevious();
        }

        @Override
        public void onPause() {
            player.pause();
            pausePosition = player.getCurrentPosition();
            showNotification(false);
            Log.i(TAG, "Clicked pause Current position is" + pausePosition);
            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
            super.onPause();
        }

        @Override
        public void onPlay() {
            player.seekTo(pausePosition);
            player.start();
            int audioFocusResult = audioManager.requestAudioFocus(
                    audioFocusChangeListener,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
            if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
                return;
            mediaSession.setActive(true);
            startAudioFocus(AudioManager.AUDIOFOCUS_GAIN);
            showNotification(false);
            Log.i(TAG, "Clicked play");
            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
            super.onPlay();
        }

        @Override
        public void onSkipToNext() {
            try {
                if(isQueue) {
                    songStream(queuePosition);
                    isQueue = false;
                }
                else
                    songStream(songPosition + 1);
            } catch (IndexOutOfBoundsException e) {
                songStream(songPosition - 1);
            }
            startAudioFocus(AudioManager.AUDIOFOCUS_GAIN);
            showNotification(true);
            Log.i(TAG, "Clicked Next");
            super.onSkipToNext();
        }

        @Override
        public void onStop() {
            audioManager.abandonAudioFocus(audioFocusChangeListener);
            //unregisterReceiver(myReceiver);
            if (player.isPlaying())
                stopForeground(true);
            else
                mNotifyMgr.cancelAll();
            player.stop();
        }
    };

    private Boolean isCover(){
        Boolean check;
        ContentResolver res = getApplicationContext().getContentResolver();
        try {
            InputStream in = res.openInputStream(playSong.getUri());
            check = true;
        } catch (FileNotFoundException e) {
            check = false;
        }
        return check;
    }

    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener =
            focusChange -> {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        mediaSessionCallback.onPlay();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        mediaSessionCallback.onPause();
                        break;
                    default:
                        mediaSessionCallback.onPause();
                        break;
                }
            };

    void startAudioFocus(int audioFocus){
        audioFocusResult = audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                audioFocus);
        if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            return;
    }

    public class MusicServiceBinder extends Binder {
        public MediaSessionCompat.Token getMediaSessionToken() {
            return mediaSession.getSessionToken();
        }
    }

    public class MusicIntentReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", 1);
                switch (state) {
                    case 0:
                        if(player.isPlaying())
                            mediaSessionCallback.onPause();
                        Log.d(TAG, "Headphones is unplugged");
                        break;
                    case 1:
                        Log.d(TAG, "Headphones is plugged");
                        break;
                    default:
                        Log.d(TAG, "???");
                }
            }
        }
    }
}