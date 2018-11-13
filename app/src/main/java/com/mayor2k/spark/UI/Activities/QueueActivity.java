package com.mayor2k.spark.UI.Activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
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
import com.mayor2k.spark.R;

import static com.mayor2k.spark.Adapters.SongAdapter.isCircle;
import static com.mayor2k.spark.Services.MusicService.playSong;
import static com.mayor2k.spark.UI.Activities.MainActivity.playArray;

public class QueueActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queue);
        TextView songTitle = findViewById(R.id.songTitle);
        songTitle.setText(playSong.getTitle());
        TextView songArtist = findViewById(R.id.songArtist);
        songArtist.setText(playSong.getArtist());
        ImageView songImage = findViewById(R.id.itemImageView);
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
        RecyclerView recyclerView = findViewById(R.id.trackList);
        QueueActivityAdapter queueActivityAdapter = new QueueActivityAdapter(playArray);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(queueActivityAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(queueActivityAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
}
