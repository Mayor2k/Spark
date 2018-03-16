package com.mayor2k.spark.Adapters;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.mayor2k.spark.Interfaces.Constants;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
    private ArrayList<Song> songs;

    public SongAdapter(ArrayList<Song> theSongs){
        songs=theSongs;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView coverView;
        TextView songTitle,songArtist;
        ViewHolder(View v) {
            super(v);
            coverView = v.findViewById(R.id.songCover);
            songTitle = v.findViewById(R.id.songName);
            songArtist = v.findViewById(R.id.songArtist);
        }
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId()==R.id.songArea){
                songPosition=(Integer)v.getTag();
                serviceIntent.setAction(Constants.STARTFOREGROUND_ACTION);
                startService(serviceIntent);
            }
            else if (v.getId()==R.id.songMenu){
                parentTag = (Integer)((View) v.getParent()).getTag();
                bottomSheetDialogFragment.show(getSupportFragmentManager(),
                        bottomSheetDialogFragment.getTag());
            }
        }
    };

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_view, parent, false);
        view.setOnClickListener(mOnClickListener);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Song song = songs.get(position);

        holder.songTitle.setText(song.getTitle());
        holder.songArtist.setText(song.getArtist());

        Glide.with(holder.coverView.getContext())
                .load(song.getUri())
                .apply(new RequestOptions()
                        .override(Target.SIZE_ORIGINAL)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .error(R.drawable.cover)
                )
                .into(holder.coverView);
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
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
