package com.mayor2k.spark.Adapters;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mayor2k.spark.Interfaces.Constants;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.mayor2k.spark.Adapters.SongAdapter.songPosition;
import static com.mayor2k.spark.Adapters.SongAdapter.serviceIntent;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {
    private ArrayList<Song> songs;

    public CustomAdapter(ArrayList<Song> theSongs){
        songs=theSongs;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView songNumber,songTitle,songDuration;
        LinearLayout songArea;
        ImageButton songMenu;
        ViewHolder(View v) {
            super(v);
            songNumber = v.findViewById(R.id.songNumber);
            songTitle = v.findViewById(R.id.songTitle);
            songDuration = v.findViewById(R.id.songDuration);

            songArea = v.findViewById(R.id.songArea);
            songMenu = v.findViewById(R.id.songMenu);
        }
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            songPosition=(Integer)v.getTag();
            serviceIntent.setAction(Constants.ALBUM_ACTION);
            v.getContext().startService(serviceIntent);
        }
    };

    private final View.OnClickListener menuOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            songPosition=(Integer)v.getTag();
            serviceIntent.setAction(Constants.ALBUM_ACTION);
            v.getContext().startService(serviceIntent);
        }
    };

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_item, parent, false);
        view.setOnClickListener(mOnClickListener);
        View menu = view.findViewById(R.id.songMenu);
        menu.setOnClickListener(menuOnClickListener);
        return new ViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Song song = songs.get(position);

        holder.songArea.setTag(position);
        holder.songMenu.setTag(position);

        holder.songNumber.setText(song.getTrack()!=0?String.valueOf(song.getTrack()):"-");
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
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
