package com.mayor2k.spark.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.mayor2k.spark.Models.Album;
import com.mayor2k.spark.R;
import com.mayor2k.spark.UI.Activities.AlbumActivity;
import com.mayor2k.spark.UI.Activities.MainActivity;
import com.mayor2k.spark.UI.Fragments.AlbumFragment;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.mayor2k.spark.UI.Activities.MainActivity.TAG;
import static com.mayor2k.spark.UI.Activities.MainActivity.getScreenWidth;

public class AlbumAdapter extends RecyclerViewCursorAdapter<AlbumAdapter.ViewHolder> {
    private ArrayList<Album> albums;
    private Context context;
    private FragmentActivity fragmentActivity;
    public static int currentAlbum;

    public AlbumAdapter(ArrayList<Album> theAlbum, Context theContext, FragmentActivity theFragmentActivity){
        super(theContext);
        albums=theAlbum;
        context=theContext;
        fragmentActivity=theFragmentActivity;

        if (checkLayout())
            setupCursorAdapter(null, 0, R.layout.grid_item, false);
        else
            setupCursorAdapter(null, 0, R.layout.linear_item, false);
    }

    class ViewHolder extends RecyclerViewCursorViewHolder {
        TextView artistName,albumTitle;
        ImageView imageView;
        LinearLayout colorArea,album;

        ViewHolder(View v){
            super(v);
            albumTitle = v.findViewById(R.id.itemTopTextView);
            artistName = v.findViewById(R.id.itemBottomTextView);
            imageView = v.findViewById(R.id.itemImageView);
            if (checkLayout())
                colorArea = v.findViewById(R.id.gridColorArea);
            //getting view for bind tag
            album = v.findViewById(R.id.itemArea);
        }

        @Override
        public void bindCursor(Cursor cursor) {

        }
    }

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
        view.setOnClickListener(onClickListener);
        return new ViewHolder(view);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            currentAlbum = (Integer)v.getTag();
            Intent intent = new Intent(v.getContext(), AlbumActivity.class);
            v.getContext().startActivity(intent);
        }
    };

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        SharedPreferences sPref = fragmentActivity.getPreferences(MODE_PRIVATE);
        int spanCount;
        if (!sPref.contains("AlbumSpanCount"))
            spanCount = 2;
        else
            spanCount = sPref.getInt("AlbumSpanCount", -1);

        Album album = albums.get(position);
        holder.album.setTag(position);
        holder.albumTitle.setText(album.getTitle());
        holder.artistName.setText(album.getArtist());

        if (checkLayout()) {
            float itemSize = (getScreenWidth(holder.albumTitle.getContext())-5*spanCount*2)/spanCount;
            float factor = holder.itemView.getContext().getResources().getDisplayMetrics().density;
            holder.imageView.getLayoutParams().width = (int) (itemSize*factor);
            holder.imageView.getLayoutParams().height = (int) (itemSize*factor);
            holder.imageView.requestLayout();
            holder.album.getLayoutParams().width = (int) (itemSize*factor);
            holder.album.requestLayout();

            Glide.with(context)
                    .load(album.getUri())
                    .apply(new RequestOptions()
                            .override(Target.SIZE_ORIGINAL)
                            .error(R.drawable.album)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                    )
                    .listener(GlidePalette.with(String.valueOf(album.getUri()))
                            .use(GlidePalette.Profile.MUTED)
                            .intoCallBack(
                                    new GlidePalette.CallBack() {
                                        @Override
                                        public void onPaletteLoaded(@Nullable Palette palette) {
                                            holder.albumTitle.setTextColor(ContextCompat.getColor(context, R.color.white));
                                            holder.artistName.setTextColor(ContextCompat.getColor(context, R.color.white));
                                        }
                                    })
                            .intoBackground(holder.colorArea)
                    )
                    .into(holder.imageView);
        }
        else
            Glide.with(context)
                    .load(album.getUri())
                    .apply(new RequestOptions()
                            .override(Target.SIZE_ORIGINAL)
                            .error(R.drawable.album)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                    )
                    .into(holder.imageView);

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
        return albums.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private boolean checkLayout(){
        SharedPreferences sPref = fragmentActivity.getPreferences(MODE_PRIVATE);
        return sPref.getInt("AlbumSpanCount", -1) != 1;
    }
}