package com.mayor2k.spark.Adapters;

import java.util.ArrayList;

import android.content.Intent;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.mayor2k.spark.Dialogs.BottomSheetDialog;
import com.mayor2k.spark.Interfaces.Constants;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;
import com.mayor2k.spark.Services.MusicService;

import static com.mayor2k.spark.UI.Activities.MainActivity.playArray;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
    private ArrayList<Song> songs;
    public static int songPosition;
    public static Intent serviceIntent;
    public static int parentTag;
    public static BottomSheetDialogFragment bottomSheetDialogFragment =
            new BottomSheetDialog();
    public SongAdapter(ArrayList<Song> theSongs){
        songs=theSongs;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView coverView;
        TextView songTitle,songArtist;
        LinearLayout songArea;
        ImageButton songMenu;
        ViewHolder(View v) {
            super(v);
            coverView = v.findViewById(R.id.songCover);
            songTitle = v.findViewById(R.id.songName);
            songArtist = v.findViewById(R.id.songArtist);
            //getting view for bind tag
            songArea = v.findViewById(R.id.songArea);
            songMenu = v.findViewById(R.id.songMenu);
        }
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            songPosition=(Integer)v.getTag();
            serviceIntent.setAction(Constants.STARTFOREGROUND_ACTION);
            v.getContext().startService(serviceIntent);
        }
    };

    private final View.OnClickListener menuOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            playArray=songs;
            parentTag = (Integer)((View) v.getParent()).getTag();
            bottomSheetDialogFragment.show(((FragmentActivity)v.getContext()).getSupportFragmentManager(),
                    bottomSheetDialogFragment.getTag());
        }
    };

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.song_view, parent, false);
        view.setOnClickListener(mOnClickListener);
        View menu = view.findViewById(R.id.songMenu);
        menu.setOnClickListener(menuOnClickListener);
        serviceIntent = new Intent(view.getContext(), MusicService.class);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Song song = songs.get(position);

        holder.songArea.setTag(position);
        holder.songMenu.setTag(position);

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
