package com.mayor2k.spark.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
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
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.florent37.glidepalette.GlidePalette;
import com.mayor2k.spark.Models.Artist;
import com.mayor2k.spark.R;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;
import static com.mayor2k.spark.UI.Activities.MainActivity.getScreenWidth;


public class ArtistAdapter  extends RecyclerViewCursorAdapter<ArtistAdapter.ViewHolder> {
    private ArrayList<Artist> artists;
    private Context context;
    private FragmentActivity fragmentActivity;
    private int spanCount;

    public ArtistAdapter(ArrayList<Artist> theArtist, Context theContext, FragmentActivity theFragmentActivity){
        super(theContext);
        artists=theArtist;
        context=theContext;
        fragmentActivity=theFragmentActivity;
        SharedPreferences sPref = fragmentActivity.getPreferences(MODE_PRIVATE);

        if (!sPref.contains("ArtistSpanCount"))
            spanCount = 2;
        else
            spanCount = sPref.getInt("ArtistSpanCount", -1);

        if (checkLayout())
            setupCursorAdapter(null, 0, R.layout.grid_item, false);
        else
            setupCursorAdapter(null, 0, R.layout.linear_item, false);
    }

    class ViewHolder extends RecyclerViewCursorViewHolder {
        LinearLayout artistArea,colorArea;
        ImageView artistCover;
        TextView artistTitle,artistInfo;
        ViewHolder(View v) {
            super(v);
            artistCover = v.findViewById(R.id.itemImageView);
            artistTitle = v.findViewById(R.id.itemTopTextView);
            artistInfo = v.findViewById(R.id.itemBottomTextView);
            if (checkLayout())
                colorArea = v.findViewById(R.id.gridColorArea);
            //getting view for bind tag
            artistArea = v.findViewById(R.id.itemArea);

        }

        @Override
        public void bindCursor(Cursor cursor) {

        }
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };


    @NonNull
    @Override
    public ArtistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (checkLayout())
            view = LayoutInflater.from(mContext)
                    .inflate(R.layout.grid_item, parent, false);
        else
            view = LayoutInflater.from(mContext)
                    .inflate(R.layout.linear_item, parent, false);
        view.setOnClickListener(mOnClickListener);

        return new ArtistAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final Artist artist = artists.get(position);
        holder.artistArea.setTag(position);
        holder.artistTitle.setText(artist.getTitle());

        holder.artistInfo.setText(String.valueOf(artist.getSongInfo())+" song "
                +String.valueOf(artist.getAlbumInfo())+" album");

        if (checkLayout()){
            float itemSize = getScreenWidth(context)/spanCount;
            int padding = (int) ((int)itemSize*0.03f);
            float factor = context.getResources().getDisplayMetrics().density;

            holder.artistCover.getLayoutParams().width = (int) (itemSize*factor);
            holder.artistCover.getLayoutParams().height = (int) (itemSize*factor);
            holder.artistCover.requestLayout();
            holder.artistArea.getLayoutParams().width = (int) (itemSize*factor);
            holder.artistArea.requestLayout();

            //set left padding only for first layout on row
            //set bottom padding only for last row
            holder.artistArea.setPadding(position%spanCount==0?padding:0,padding,
                    padding,position==getItemCount()-1?padding:0);

            Glide.with(context)
                    .asBitmap()
                    .load(artist.getUrl())
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                    )
                    .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL,Target.SIZE_ORIGINAL) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            holder.artistCover.setImageBitmap(resource);
                            holder.artistTitle.setTextColor(ContextCompat.getColor(context, R.color.white));
                            holder.artistInfo.setTextColor(ContextCompat.getColor(context, R.color.white));
                            Palette.from(resource).generate(p -> holder.colorArea.setBackgroundColor(p.getMutedColor(p.getVibrantColor(p.getDominantColor(0)))));
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            holder.artistCover.setImageResource(R.drawable.album);
                            holder.colorArea.setBackgroundColor(ContextCompat.getColor(context, R.color.item_area));
                            holder.artistTitle.setTextColor(ContextCompat.getColor(context, R.color.black));
                            holder.artistInfo.setTextColor(ContextCompat.getColor(context, R.color.black_p50));
                        }
                    });
    }
        else
            Glide.with(context)
                    .load(artist.getUrl())
                    .apply(new RequestOptions()
                            .override(Target.SIZE_ORIGINAL)
                            .error(R.drawable.cover)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                    )
                    .into(holder.artistCover);

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
        return artists.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private boolean checkLayout(){
        SharedPreferences sPref = fragmentActivity.getPreferences(MODE_PRIVATE);
        return sPref.getInt("ArtistSpanCount", -1) != 1;
    }
}