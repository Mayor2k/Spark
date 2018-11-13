package com.mayor2k.spark.Adapters;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androidessence.recyclerviewcursoradapter.RecyclerViewCursorViewHolder;
import com.mayor2k.spark.Interfaces.ItemTouchHelperAdapter;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;

import java.util.ArrayList;
import java.util.Collections;

public class QueueActivityAdapter extends RecyclerView.Adapter<QueueActivityAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    private ArrayList playlist;
    public QueueActivityAdapter( ArrayList playlist) {
        this.playlist = playlist;
    }

    public class ViewHolder extends RecyclerViewCursorViewHolder {
        TextView songTitle,songArtist;
        ViewHolder(View view) {
            super(view);
            songTitle = view.findViewById(R.id.itemTopTextView);
            songArtist = view.findViewById(R.id.itemBottomTextView);
        }
        @Override
        public void bindCursor(Cursor cursor) {}
    }

    @NonNull
    @Override
    public QueueActivityAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.linear_item, parent, false);
        return  new QueueActivityAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QueueActivityAdapter.ViewHolder holder, int position) {
        Song song = (Song) playlist.get(position);
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
