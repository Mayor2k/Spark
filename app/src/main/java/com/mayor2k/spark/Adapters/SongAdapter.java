package com.mayor2k.spark.Adapters;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

import com.androidessence.recyclerviewcursoradapter.RecyclerViewCursorAdapter;
import com.androidessence.recyclerviewcursoradapter.RecyclerViewCursorViewHolder;
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

public class SongAdapter extends RecyclerViewCursorAdapter<SongAdapter.ViewHolder> {
    private ArrayList<Song> songs;
    public static int songPosition;
    public static Intent serviceIntent;
    public static int parentTag;
    public static BottomSheetDialogFragment bottomSheetDialogFragment =
            new BottomSheetDialog();
    private Context context;

    public SongAdapter(ArrayList<Song> theSongs,Context theContext){
        super(theContext);
        context = theContext;
        songs=theSongs;

        setupCursorAdapter(null, 0, R.layout.linear_item, false);
    }

    class ViewHolder extends RecyclerViewCursorViewHolder {
        ImageView coverView;
        TextView songTitle,songArtist;
        LinearLayout songArea;
        ImageButton songMenu;
        ViewHolder(View v) {
            super(v);
            coverView = v.findViewById(R.id.itemImageView);
            songTitle = v.findViewById(R.id.itemTopTextView);
            songArtist = v.findViewById(R.id.itemBottomTextView);
            //getting view for bind tag
            songArea = v.findViewById(R.id.itemArea);
            songMenu = v.findViewById(R.id.linearMenu);
        }

        @Override
        public void bindCursor(Cursor cursor) {

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
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.linear_item, parent, false);
        view.setOnClickListener(mOnClickListener);
        View menu = view.findViewById(R.id.linearMenu);
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
        mCursorAdapter.getCursor().moveToPosition(position);
        setViewHolder(holder);
        mCursorAdapter.bindView(null, mContext, mCursorAdapter.getCursor());
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
