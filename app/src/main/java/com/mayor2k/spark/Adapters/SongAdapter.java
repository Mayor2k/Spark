package com.mayor2k.spark.Adapters;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.mayor2k.spark.Dialogs.BottomSheetDialog;
import com.mayor2k.spark.Interfaces.Constants;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;
import com.mayor2k.spark.Services.MusicService;

import static android.content.Context.MODE_PRIVATE;
import static com.mayor2k.spark.UI.Activities.MainActivity.getScreenWidth;
import static com.mayor2k.spark.UI.Activities.MainActivity.playArray;

public class SongAdapter extends RecyclerViewCursorAdapter<SongAdapter.ViewHolder> {
    private ArrayList<Song> songs;
    public static int songPosition;
    public static Intent serviceIntent;
    public static int parentTag;
    static BottomSheetDialogFragment bottomSheetDialogFragment =
            new BottomSheetDialog();
    private Context context;
    private FragmentActivity fragmentActivity;
    private int spanCount;

    public SongAdapter(ArrayList<Song> theSongs,Context theContext,FragmentActivity theFragmentActivity){
        super(theContext);
        context = theContext;
        songs=theSongs;
        fragmentActivity=theFragmentActivity;

        SharedPreferences sPref = fragmentActivity.getPreferences(MODE_PRIVATE);
        if (!sPref.contains("SongSpanCount"))
            spanCount = 1;
        else
            spanCount = sPref.getInt("SongSpanCount", -1);

        if (spanCount!=1)
            setupCursorAdapter(null, 0, R.layout.grid_item, false);
        else
            setupCursorAdapter(null, 0, R.layout.linear_item, false);
    }

    class ViewHolder extends RecyclerViewCursorViewHolder {
        ImageView coverView;
        TextView songTitle,songArtist;
        LinearLayout songArea,colorArea;
        ImageButton songMenu;
        ViewHolder(View v) {
            super(v);
            coverView = v.findViewById(R.id.itemImageView);
            songTitle = v.findViewById(R.id.itemTopTextView);
            songArtist = v.findViewById(R.id.itemBottomTextView);
            //getting view for bind tag
            songArea = v.findViewById(R.id.itemArea);
            if (spanCount!=1)
                colorArea = v.findViewById(R.id.gridColorArea);
            else
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

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (spanCount!=1)
            view = LayoutInflater.from(mContext)
                    .inflate(R.layout.grid_item, parent, false);
        else
            view = LayoutInflater.from(mContext)
                    .inflate(R.layout.linear_item, parent, false);
        view.setOnClickListener(mOnClickListener);

        if (spanCount==1){
            View menu = view.findViewById(R.id.linearMenu);
            menu.setOnClickListener(menuOnClickListener);
        }
        serviceIntent = new Intent(view.getContext(), MusicService.class);

        return new ViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        Song song = songs.get(position);

        holder.songArea.setTag(position);
        holder.songTitle.setText(song.getTitle());
        holder.songArtist.setText(song.getArtist());

        if(spanCount!=1) {
            float itemSize = getScreenWidth(context)/spanCount;
            int padding = (int) ((int)itemSize*0.03f);
            float factor = context.getResources().getDisplayMetrics().density;

            holder.coverView.getLayoutParams().width = (int) (itemSize*factor);
            holder.coverView.getLayoutParams().height = (int) (itemSize*factor);
            holder.coverView.requestLayout();
            holder.songArea.getLayoutParams().width = (int) (itemSize*factor);
            holder.songArea.requestLayout();

            //set left padding only for first layout on row
            //set bottom padding only for last row
            holder.songArea.setPadding(position%spanCount==0?padding:0,padding,
                    padding,position==getItemCount()-1?padding:0);

            Glide.with(context)
                    .asBitmap()
                    .load(song.getUri())
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                    )
                    .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            holder.coverView.setImageBitmap(resource);
                            Palette.from(resource).generate(p -> {
                                holder.colorArea.setBackgroundColor(p.getMutedColor(p.getVibrantColor(p.getDominantColor(0))));
                            });
                            holder.songTitle.setTextColor(ContextCompat.getColor(context, R.color.white));
                            holder.songArtist.setTextColor(ContextCompat.getColor(context, R.color.white));
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            holder.coverView.setImageResource(R.drawable.album);
                            holder.colorArea.setBackgroundColor(ContextCompat.getColor(context, R.color.item_area));
                            holder.songTitle.setTextColor(ContextCompat.getColor(context, R.color.black));
                            holder.songArtist.setTextColor(ContextCompat.getColor(context, R.color.black_p50));
                        }
                    });
        }
        else{
            Glide.with(holder.coverView.getContext())
                    .asBitmap()
                    .load(song.getUri())
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                    )
                    .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            holder.coverView.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            holder.coverView.setImageResource(R.drawable.album);
                        }
                    });
        }

        mCursorAdapter.getCursor().moveToPosition(position);
        setViewHolder(holder);
        mCursorAdapter.bindView(
                null, mContext, mCursorAdapter.getCursor());
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

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}