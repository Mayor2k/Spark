package com.mayor2k.spark.Adapters;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.mayor2k.spark.Interfaces.Constants;
import com.mayor2k.spark.Models.Album;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.mayor2k.spark.Adapters.SongAdapter.bottomSheetDialogFragment;
import static com.mayor2k.spark.Adapters.SongAdapter.isCircle;
import static com.mayor2k.spark.Adapters.SongAdapter.parentTag;
import static com.mayor2k.spark.Adapters.SongAdapter.serviceIntent;
import static com.mayor2k.spark.Adapters.SongAdapter.songPosition;
import static com.mayor2k.spark.UI.Activities.MainActivity.playArray;

public class ArtistActivityAdapter extends RecyclerView.Adapter<ArtistActivityAdapter.ViewHolder>{
    private ArrayList<Song> songs;

    public ArtistActivityAdapter(ArrayList<Song> theSongs){
        songs = theSongs;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView songTitle,songDuration;
        LinearLayout songArea;
        ImageButton songMenu;
        ImageView songCover;
        ViewHolder(View v) {
            super(v);
            songCover = v.findViewById(R.id.itemImageView);
            songTitle = v.findViewById(R.id.itemTopTextView);
            songDuration = v.findViewById(R.id.itemBottomTextView);

            songArea = v.findViewById(R.id.itemArea);
            songMenu = v.findViewById(R.id.linearMenu);
        }
    }

    private final View.OnClickListener mOnClickListener = v -> {
        songPosition=(Integer)v.getTag();
        serviceIntent.setAction(Constants.START_ARTIST_ACTION);
        v.getContext().startService(serviceIntent);
    };

    private final View.OnClickListener menuOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            parentTag = (Integer)((View) v.getParent()).getTag();
            playArray=songs;
            bottomSheetDialogFragment.show(((FragmentActivity)v.getContext()).getSupportFragmentManager(),
                    bottomSheetDialogFragment.getTag());
        }
    };

    @NonNull
    @Override
    public ArtistActivityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.linear_item, parent, false);
        view.setOnClickListener(mOnClickListener);
        View menu = view.findViewById(R.id.linearMenu);
        menu.setOnClickListener(menuOnClickListener);
        return new ArtistActivityAdapter.ViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song song = songs.get(position);

        holder.songArea.setTag(position);
        holder.songMenu.setTag(position);

        holder.songArea.setPadding(10,position==0?10:0,0,10);

        Glide.with(holder.songCover.getContext())
                .asBitmap()
                .load(song.getUri())
                .apply(isCircle?new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop():
                        new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                )
                .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        holder.songCover.setImageBitmap(resource);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        holder.songCover.setImageResource(R.drawable.album);
                    }
                });

        holder.songTitle.setText(song.getTitle());
        holder.songDuration.setText(String.format("%d.%02d", TimeUnit.MILLISECONDS.toMinutes(song.getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(song.getDuration()%60000)));
    }

    @Override
    public long getItemId(int arg0) {
        return super.getItemId(arg0);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
