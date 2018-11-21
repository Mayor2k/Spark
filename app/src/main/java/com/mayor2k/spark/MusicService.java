package com.mayor2k.spark;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.NotificationTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mayor2k.spark.Interfaces.Constants;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.UI.Activities.PlayerActivity;

import java.io.IOException;

import static com.mayor2k.spark.UI.Activities.AlbumActivity.albumSongs;
import static com.mayor2k.spark.UI.Activities.ArtistActivity.artistSongs;
import static com.mayor2k.spark.UI.Activities.MainActivity.TAG;
import static com.mayor2k.spark.Adapters.SongAdapter.songPosition;
import static com.mayor2k.spark.UI.Activities.MainActivity.changePlayerTitle;
import static com.mayor2k.spark.UI.Activities.MainActivity.playArray;
import static com.mayor2k.spark.UI.Activities.SearchActivity.searchList;
import static com.mayor2k.spark.UI.Fragments.SongFragment.songList;
import static com.mayor2k.spark.Helper.CoverUtil.getCoverBitmap;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener {
    public static MediaPlayer player = new MediaPlayer();
    public static Song playSong;
    public static int pausePosition;
    public static boolean isShuffle = false;
    public static boolean isQueue;
    public static int queuePosition;
    public static NotificationManager mNotifyMgr;
    private SharedPreferences sPref;
    public MediaSessionCompat mediaSession;
    public MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
    final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
            .setActions(PlaybackStateCompat.ACTION_PLAY
                    | PlaybackStateCompat.ACTION_STOP
                    | PlaybackStateCompat.ACTION_PAUSE
                    | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
    public AudioManager audioManager;

    @Override
    public void onCreate() {
        mediaSession = new MediaSessionCompat(this, TAG);
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(mediaSessionCallback);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mediaSession.setActive(true);
        sPref = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate();
    }

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && sPref.getBoolean("notifications_style",true)) {
                showNotification24();
            }else{
                showNotification();
            }
        }else if (intent.getAction().equals(Constants.START_ALBUM_ACTION)) {
            playArray=albumSongs;
            songStream(songPosition);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && sPref.getBoolean("notifications_style",true)) {
                showNotification24();
            }else{
                showNotification();
            }
        }else if (intent.getAction().equals(Constants.START_SEARCH_ACTION)){
            playArray=searchList;
            songStream(songPosition);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && sPref.getBoolean("notifications_style",true)) {
                showNotification24();
            }else{
                showNotification();
            }
        }else if (intent.getAction().equals(Constants.START_ARTIST_ACTION)){
            playArray=artistSongs;
            songStream(songPosition);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && sPref.getBoolean("notifications_style",true)) {
                showNotification24();
            }else{
                showNotification();
            }
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
        playSong = (Song) playArray.get(songPosition);
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
        player.setOnCompletionListener(mp -> mediaSessionCallback.onSkipToNext());

        MediaMetadataCompat.Builder metadata = metadataBuilder
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, getCoverBitmap(playSong,getApplicationContext()))
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, playSong.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, playSong.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, playSong.getTitle());
        mediaSession.setMetadata(metadata.build());

        startAudioFocus(AudioManager.AUDIOFOCUS_GAIN);

        mediaSession.setPlaybackState(
                stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());

        registerReceiver(
                becomingNoisyReceiver,
                new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void showNotification24(){
        Intent notificationIntent = new Intent(getApplicationContext(), PlayerActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        notificationIntent.setAction(Constants.MAIN_ACTION);

        PendingIntent notification = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Intent prevIntent = new Intent(getApplicationContext(), MusicService.class);
        prevIntent.setAction(Constants.PREV_ACTION);
        PendingIntent prev = PendingIntent.getService(getApplicationContext(), 1, prevIntent, 0);
        NotificationCompat.Action prevAction = new NotificationCompat.Action(R.drawable.ic_previous_24dp_black,
                "prev", prev);

        Intent pauseIntent = new Intent(getApplicationContext(), MusicService.class);
        pauseIntent.setAction(Constants.PAUSE_ACTION);
        PendingIntent pause = PendingIntent.getService(getApplicationContext(), 1, pauseIntent, 0);
        NotificationCompat.Action pauseAction = new NotificationCompat.Action(R.drawable.ic_pause_24dp_black,
                "pause", pause);

        Intent playIntent = new Intent(getApplicationContext(), MusicService.class);
        playIntent.setAction(Constants.PLAY_ACTION);
        PendingIntent play = PendingIntent.getService(getApplicationContext(), 1, playIntent, 0);
        NotificationCompat.Action playAction = new NotificationCompat.Action(R.drawable.ic_play_24dp_black,
                "play", play);

        Intent nextIntent = new Intent(getApplicationContext(), MusicService.class);
        nextIntent.setAction(Constants.NEXT_ACTION);
        PendingIntent next = PendingIntent.getService(getApplicationContext(), 1, nextIntent, 0);
        NotificationCompat.Action nextAction = new NotificationCompat.Action(R.drawable.ic_next_24dp_black,
                "next", next);

        String CHANNEL_ID = "CHANNEL_02";
        CharSequence name = "NAME_1";
        int importance = NotificationManager.IMPORTANCE_LOW;
        Bitmap bitmap;
        bitmap = getCoverBitmap(playSong,getBaseContext());
        if(bitmap==null)
            bitmap = BitmapFactory.decodeResource(getApplication().getResources(), R.drawable.album);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setLargeIcon(bitmap)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_play_24dp)
                .setSubText(playSong.getAlbum())
                .setContentTitle(playSong.getTitle())
                .setContentText(playSong.getArtist())
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0,1,2)
                        .setMediaSession(mediaSession.getSessionToken()))
                .setContentIntent(notification)
                .setColorized(true)
                .setShowWhen(false)
                .addAction(prevAction)
                .addAction(player.isPlaying()?pauseAction:playAction)
                .addAction(nextAction)
                .setOngoing(player.isPlaying())
                .setPriority(player.isPlaying() ?
                        NotificationCompat.PRIORITY_MAX : NotificationCompat.PRIORITY_LOW);

        if (player.isPlaying()){
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotifyMgr.createNotificationChannel(mChannel);
            }
            startForeground(2,builder.build());
        }else{
            stopForeground(false);
            mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                mChannel.setLightColor(0);
                mChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PRIVATE);
                mNotifyMgr.createNotificationChannel(mChannel);
            }
            mNotifyMgr.notify(2, builder.build());
        }
    }


    public void showNotification() {
        final RemoteViews views = new RemoteViews(getPackageName(), R.layout.notification);
        final RemoteViews viewsBig = new RemoteViews(getPackageName(), R.layout.notification_big);

        views.setTextViewText(R.id.status_bar_track_name, playSong.getTitle());
        views.setTextViewText(R.id.status_bar_artist_name, playSong.getArtist());

        viewsBig.setTextViewText(R.id.status_bar_track_name, playSong.getTitle());
        viewsBig.setTextViewText(R.id.status_bar_album_name, playSong.getAlbum());
        viewsBig.setTextViewText(R.id.status_bar_artist_name, playSong.getArtist());

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

        String CHANNEL_ID = "CHANNEL_01";
        CharSequence name = "NAME";
        int importance = NotificationManager.IMPORTANCE_LOW;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID)
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
                        if (sPref.getBoolean("notifications_color",true)){
                            Palette p = Palette.from(resource).generate();
                            int color = p.getMutedColor(p.getVibrantColor(p.getDominantColor(0)));
                            views.setInt(R.id.root,"setBackgroundColor", color);
                            viewsBig.setInt(R.id.root,"setBackgroundColor",color);
                            setNotificationColor(views,viewsBig,true);
                        }
                        else {
                            views.setInt(R.id.root,"setBackgroundColor",
                                    ContextCompat.getColor(getApplicationContext(), R.color.transparent));
                            viewsBig.setInt(R.id.root,"setBackgroundColor",
                                    ContextCompat.getColor(getApplicationContext(), R.color.transparent));
                            setNotificationColor(views,viewsBig,false);
                        }
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
                        setNotificationColor(views,viewsBig,false);
                        viewsTarget.onResourceReady(BitmapFactory.decodeResource
                                (getApplicationContext().getResources(),
                                        R.drawable.cover),null);
                        viewsBigTarget.onResourceReady(BitmapFactory.decodeResource
                                (getApplicationContext().getResources(), R.drawable.cover),null);
                    }});

        views.setOnClickPendingIntent(R.id.status_bar_prev, prev);
        viewsBig.setOnClickPendingIntent(R.id.status_bar_prev, prev);

        views.setOnClickPendingIntent(R.id.status_bar_play, player.isPlaying() ? pause : play);
        viewsBig.setOnClickPendingIntent(R.id.status_bar_play, player.isPlaying() ? pause : play);

        views.setOnClickPendingIntent(R.id.status_bar_next, next);
        viewsBig.setOnClickPendingIntent(R.id.status_bar_next, next);

        if (player.isPlaying()){
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                mNotifyMgr =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotifyMgr.createNotificationChannel(mChannel);
            }
            startForeground(1,builder.build());
        }else{
            stopForeground(false);
            mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
                mChannel.setLightColor(0);
                mChannel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PRIVATE);
                mNotifyMgr.createNotificationChannel(mChannel);
            }
            mNotifyMgr.notify(1, builder.build());
        }
    }

    void setNotificationColor(RemoteViews views, RemoteViews viewsBig, Boolean isWhite){
        if (isWhite){
            views.setImageViewResource(R.id.status_bar_prev, R.drawable.ic_previous_24dp);
            viewsBig.setImageViewResource(R.id.status_bar_prev, R.drawable.ic_previous_24dp);
            views.setImageViewResource(R.id.status_bar_next, R.drawable.ic_next_24dp);
            viewsBig.setImageViewResource(R.id.status_bar_next, R.drawable.ic_next_24dp);
            views.setImageViewResource(R.id.status_bar_play,
                    player.isPlaying() ? R.drawable.ic_pause_24dp : R.drawable.ic_play_24dp);
            viewsBig.setImageViewResource(R.id.status_bar_play,
                    player.isPlaying() ? R.drawable.ic_pause_24dp : R.drawable.ic_play_24dp);

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
        }else{
            views.setImageViewResource(R.id.status_bar_prev, R.drawable.ic_previous_24dp_black);
            viewsBig.setImageViewResource(R.id.status_bar_prev, R.drawable.ic_previous_24dp_black);
            views.setImageViewResource(R.id.status_bar_next, R.drawable.ic_next_24dp_black);
            viewsBig.setImageViewResource(R.id.status_bar_next, R.drawable.ic_next_24dp_black);
            views.setImageViewResource(R.id.status_bar_play,
                    player.isPlaying() ? R.drawable.ic_pause_24dp_black : R.drawable.ic_play_24dp_black);
            viewsBig.setImageViewResource(R.id.status_bar_play,
                    player.isPlaying() ? R.drawable.ic_pause_24dp_black : R.drawable.ic_play_24dp_black);

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
        }
    }

    @Override
    public void onDestroy() {
        mediaSessionCallback.onStop();
        player.release();
        mediaSession.release();
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
                }catch (ClassCastException e){
                    songStream(1);
                }

            }
            startAudioFocus(AudioManager.AUDIOFOCUS_GAIN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && sPref.getBoolean("notifications_style",true)) {
                showNotification24();
            }else{
                showNotification();
            }
            Log.i(TAG, "Clicked Previous");
            changePlayerTitle(playSong.getTitle());
            super.onSkipToPrevious();
        }

        @Override
        public void onPause() {
            player.pause();
            pausePosition = player.getCurrentPosition();
            try {
                unregisterReceiver(becomingNoisyReceiver);
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }

            audioManager.abandonAudioFocus(audioFocusChangeListener);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && sPref.getBoolean("notifications_style",true)) {
                showNotification24();
            }else{
                showNotification();
            }
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
            startAudioFocus(AudioManager.AUDIOFOCUS_GAIN);
            registerReceiver(
                    becomingNoisyReceiver,
                    new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && sPref.getBoolean("notifications_style",true)) {
                showNotification24();
            }else{
                showNotification();
            }
            Log.i(TAG, "Clicked play");
            mediaSession.setPlaybackState(
                    stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
            mediaSession.setActive(true);
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
            } catch (IndexOutOfBoundsException|ClassCastException e) {
                songStream(songPosition - 1);
            }
            startAudioFocus(AudioManager.AUDIOFOCUS_GAIN);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && sPref.getBoolean("notifications_style",true)) {
                showNotification24();
            }else{
                showNotification();
            }
            Log.i(TAG, "Clicked Next");
            changePlayerTitle(playSong.getTitle());
            super.onSkipToNext();
        }

        @Override
        public void onStop() {
            player.stop();
            audioManager.abandonAudioFocus(audioFocusChangeListener);
            try{
                unregisterReceiver(becomingNoisyReceiver);
            }catch (IllegalArgumentException e){
                e.printStackTrace();
            }
            if (player.isPlaying())
                stopForeground(true);
            else
                mNotifyMgr.cancelAll();
        }
    };

    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener =
            focusChange -> {
                switch (focusChange) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        Log.i("TAGGING","AUDIOFOCUS_GAIN");
                        mediaSessionCallback.onPlay();
                        break;
                    case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                        Log.i("TAGGING","AUDIOFOCUS_GAIN_TRANSIENT");
                        pausePosition=player.getCurrentPosition();
                        mediaSessionCallback.onPlay();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        Log.i("TAGGING","AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                        mediaSessionCallback.onPlay();
                        break;
                    case AudioManager.AUDIOFOCUS_LOSS:
                        Log.i("TAGGING","AUDIOFOCUS_LOSS");
                        mediaSessionCallback.onPause();
                        break;
                    default:
                        mediaSessionCallback.onPause();
                        break;
                }
            };

    void startAudioFocus(int audioFocus){
        int audioFocusResult = audioManager.requestAudioFocus(
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

    final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                mediaSessionCallback.onPause();
            }
        }
    };
}