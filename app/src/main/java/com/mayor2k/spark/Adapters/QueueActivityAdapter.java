package com.mayor2k.spark.Adapters;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.androidessence.recyclerviewcursoradapter.RecyclerViewCursorViewHolder;
import com.mayor2k.spark.Interfaces.Constants;
import com.mayor2k.spark.Interfaces.ItemTouchHelperAdapter;
import com.mayor2k.spark.Interfaces.OnStartDragListener;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;

import java.util.ArrayList;
import java.util.Collections;

import static com.mayor2k.spark.Adapters.SongAdapter.serviceIntent;
import static com.mayor2k.spark.Adapters.SongAdapter.songPosition;

public class QueueActivityAdapter extends RecyclerView.Adapter<QueueActivityAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    private ArrayList playlist;
    private OnStartDragListener onStartDragListener;
    public QueueActivityAdapter( ArrayList playlist,OnStartDragListener onStartDragListener) {
        this.playlist = playlist;
        this.onStartDragListener = onStartDragListener;
    }

    public class ViewHolder extends RecyclerViewCursorViewHolder {
        TextView songTitle,songArtist;
        ImageButton dragSong;
        ViewHolder(View view) {
            super(view);
            songTitle = view.findViewById(R.id.itemTopTextView);
            songArtist = view.findViewById(R.id.itemBottomTextView);
            dragSong = view.findViewById(R.id.dragSong);
        }
        @Override
        public void bindCursor(Cursor cursor) {}
    }

    @NonNull
    @Override
    public QueueActivityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.queue_item, parent, false);
        return  new QueueActivityAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueActivityAdapter.ViewHolder holder, int position) {
        Song song = (Song) playlist.get(position);
        holder.dragSong.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) ==
                        MotionEvent.ACTION_DOWN) {
                    onStartDragListener.onStartDrag(holder);
                }
                return false;
            }
        });
        holder.songTitle.setText(song.getTitle());
        holder.songArtist.setText(song.getArtist());
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(playlist, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(playlist, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        playlist.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public long getItemId(int arg0) {
        return super.getItemId(arg0);
    }

    @Override
    public int getItemCount() {
        return playlist.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
