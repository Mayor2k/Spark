package com.mayor2k.spark.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import com.bumptech.glide.request.target.Target;
import com.github.florent37.glidepalette.GlidePalette;
import com.mayor2k.spark.Models.Artist;
import com.mayor2k.spark.R;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;


public class ArtistAdapter  extends RecyclerViewCursorAdapter<ArtistAdapter.ViewHolder> {
    private ArrayList<Artist> artists;
    private Context context;
    private FragmentActivity fragmentActivity;

    public ArtistAdapter(ArrayList<Artist> theArtist, Context theContext, FragmentActivity theFragmentActivity){
        super(theContext);
        artists=theArtist;
        context=theContext;
        fragmentActivity=theFragmentActivity;

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

        if (checkLayout())
            Glide.with(context)
                    .load(artist.getUrl())
                    .apply(new RequestOptions()
                            .override(Target.SIZE_ORIGINAL)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .error(R.drawable.cover)
                    )
                    .listener(GlidePalette.with(artist.getUrl())
                            .use(GlidePalette.Profile.MUTED)
                            .intoCallBack(
                                    new GlidePalette.CallBack() {
                                        @Override
                                        public void onPaletteLoaded(@Nullable Palette palette) {
                                            holder.artistTitle.setTextColor(ContextCompat.getColor(context, R.color.white));
                                            holder.artistInfo.setTextColor(ContextCompat.getColor(context, R.color.white));

                                        }
                                    })
                            .intoBackground(holder.colorArea)
                    )
                    .into(holder.artistCover);
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
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private boolean checkLayout(){
        SharedPreferences sPref = fragmentActivity.getPreferences(MODE_PRIVATE);
        return sPref.getInt("ArtistSpanCount", -1) != 1;
    }
}