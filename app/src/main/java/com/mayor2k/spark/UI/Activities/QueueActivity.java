package com.mayor2k.spark.UI.Activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.mayor2k.spark.Adapters.QueueActivityAdapter;
import com.mayor2k.spark.Helper.SimpleItemTouchHelperCallback;
import com.mayor2k.spark.Interfaces.OnStartDragListener;
import com.mayor2k.spark.R;
import com.mayor2k.spark.MusicService;

import static com.mayor2k.spark.Adapters.SongAdapter.isCircle;
import static com.mayor2k.spark.MusicService.playSong;
import static com.mayor2k.spark.UI.Activities.MainActivity.playArray;

public class QueueActivity extends AppCompatActivity implements
        OnStartDragListener {
    private MusicService.MusicServiceBinder musicServiceBinder;
    private MediaControllerCompat mediaController;
    private TextView songTitle, songArtist;
    private ImageView songImage;
    private ItemTouchHelper itemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);
        songTitle = findViewById(R.id.songTitle);
        songArtist = findViewById(R.id.songArtist);
        songImage = findViewById(R.id.itemImageView);
        RecyclerView recyclerView = findViewById(R.id.trackList);
        setPlayingSong();
        QueueActivityAdapter queueActivityAdapter = new QueueActivityAdapter(playArray,this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(queueActivityAdapter);
        recyclerView.scrollToPosition(playArray.indexOf(playSong));
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(queueActivityAdapter);
        itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        bindService(new Intent(this, MusicService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                musicServiceBinder = (MusicService.MusicServiceBinder) service;
                try {
                    mediaController = new MediaControllerCompat(
                            QueueActivity.this, musicServiceBinder.getMediaSessionToken());
                    mediaController.registerCallback(
                            new MediaControllerCompat.Callback() {
                                @Override
                                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                                    setPlayingSong();
                                    queueActivityAdapter.notifyDataSetChanged();
                                    recyclerView.scrollToPosition(playArray.indexOf(playSong));
                                    if (state == null)
                                        return;
                                }
                            }
                    );
                }
                catch (RemoteException|IllegalArgumentException e) {
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
    }

    private void setPlayingSong(){
        try{
            songTitle.setText(playSong.getTitle());
            songArtist.setText(playSong.getArtist());
            Glide.with(this)
                    .asBitmap()
                    .load(playSong.getUri())
                    .apply(isCircle?new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop():
                            new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                    )
                    .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            songImage.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getResources(),
                                            BitmapFactory.decodeResource(getResources(), R.drawable.album));
                            circularBitmapDrawable.setCircular(true);
                            songImage.setImageDrawable(circularBitmapDrawable);
                        }
                    });
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        itemTouchHelper.startDrag(viewHolder);
    }
}
