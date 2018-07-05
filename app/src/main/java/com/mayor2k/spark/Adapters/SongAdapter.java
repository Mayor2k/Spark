package com.mayor2k.spark.Adapters;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
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
import com.github.florent37.glidepalette.GlidePalette;
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
    public static BottomSheetDialogFragment bottomSheetDialogFragment =
            new BottomSheetDialog();
    private Context context;
    private FragmentActivity fragmentActivity;

    public SongAdapter(ArrayList<Song> theSongs,Context theContext,FragmentActivity theFragmentActivity){
        super(theContext);
        context = theContext;
        songs=theSongs;
        fragmentActivity=theFragmentActivity;

        if (checkLayout())
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
            if (checkLayout())
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
        if (checkLayout())
            view = LayoutInflater.from(mContext)
                    .inflate(R.layout.grid_item, parent, false);
        else
            view = LayoutInflater.from(mContext)
                    .inflate(R.layout.linear_item, parent, false);
        view.setOnClickListener(mOnClickListener);

        if (!checkLayout()){
            View menu = view.findViewById(R.id.linearMenu);
            menu.setOnClickListener(menuOnClickListener);
        }
        serviceIntent = new Intent(view.getContext(), MusicService.class);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        SharedPreferences sPref = fragmentActivity.getPreferences(MODE_PRIVATE);
        int spanCount;
        if (!sPref.contains("SongSpanCount"))
            spanCount = 2;
        else
            spanCount = sPref.getInt("SongSpanCount", -1);

        Song song = songs.get(position);
        holder.songArea.setTag(position);
        if (!checkLayout())
            holder.songMenu.setTag(position);

        holder.songTitle.setText(song.getTitle());
        holder.songArtist.setText(song.getArtist());

        if(checkLayout()) {
            float itemSize = (getScreenWidth(holder.coverView.getContext())-5*spanCount*2)/spanCount;
            float factor = holder.coverView.getContext().getResources().getDisplayMetrics().density;

            holder.coverView.getLayoutParams().width = (int) (itemSize*factor);
            holder.coverView.getLayoutParams().height = (int) (itemSize*factor);
            holder.coverView.requestLayout();
            holder.songArea.getLayoutParams().width = (int) (itemSize*factor);
            holder.songArea.requestLayout();

            Glide.with(context)
                    .load(song.getUri())
                    .apply(new RequestOptions()
                            .override(Target.SIZE_ORIGINAL)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .error(R.drawable.cover)
                    )
                    .listener(GlidePalette.with(String.valueOf(song.getUri()))
                            .use(GlidePalette.Profile.MUTED)
                            .intoCallBack(
                                    new GlidePalette.CallBack() {
                                        @Override
                                        public void onPaletteLoaded(@Nullable Palette palette) {
                                            holder.songTitle.setTextColor(ContextCompat.getColor(context, R.color.white));
                                            holder.songArtist.setTextColor(ContextCompat.getColor(context, R.color.white));

                                        }
                                    })
                            .intoBackground(holder.colorArea)
                    )
                    .into(holder.coverView);
        }
        else{
            Glide.with(holder.coverView.getContext())
                    .load(song.getUri())
                    .apply(new RequestOptions()
                            .override(Target.SIZE_ORIGINAL)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .error(R.drawable.cover)
                    )
                    .into(holder.coverView);
        }

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
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private boolean checkLayout(){
        SharedPreferences sPref = fragmentActivity.getPreferences(MODE_PRIVATE);
        return sPref.getInt("SongSpanCount", -1) != 1;
    }
}