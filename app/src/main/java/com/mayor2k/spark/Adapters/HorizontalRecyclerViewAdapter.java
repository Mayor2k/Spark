package com.mayor2k.spark.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.mayor2k.spark.Models.Album;
import com.mayor2k.spark.R;
import com.mayor2k.spark.UI.Activities.AlbumActivity;

import java.util.ArrayList;

import static com.mayor2k.spark.Adapters.AlbumAdapter.currentAlbum;

public class HorizontalRecyclerViewAdapter extends RecyclerView.Adapter<HorizontalRecyclerViewAdapter.ViewHolder>{
    private Context context;
    private ArrayList<Album> albums;
    public HorizontalRecyclerViewAdapter(Context context, ArrayList<Album> albums){
        this.context = context;
        this.albums = albums;
    }
    class ViewHolder extends RecyclerView.ViewHolder{
        TextView albumTitle, albumArtist ;
        ImageView albumImage;
        LinearLayout colorArea, albumArea;
        ViewHolder(View itemView) {
            super(itemView);
            albumTitle = itemView.findViewById(R.id.itemTopTextView);
            albumArtist = itemView.findViewById(R.id.itemBottomTextView);
            albumImage = itemView.findViewById(R.id.itemImageView);
            colorArea = itemView.findViewById(R.id.gridColorArea);
            albumArea = itemView.findViewById(R.id.itemArea);
        }
    }

    private final View.OnClickListener onClickListener = v -> {
        currentAlbum = albums.indexOf(albums.get((Integer) v.getTag()));
        Intent intent = new Intent(v.getContext(), AlbumActivity.class);
        v.getContext().startActivity(intent);
    };

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.grid_item, parent, false);
        view.setOnClickListener(onClickListener);
        return new HorizontalRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Album album = albums.get(position);
        holder.albumArea.setTag(position);

        float factor = context.getResources().getDisplayMetrics().density;
        holder.albumImage.getLayoutParams().width = (int) (100 * factor);
        holder.albumImage.getLayoutParams().height = (int) (100 * factor);
        holder.albumImage.requestLayout();
        holder.albumArea.getLayoutParams().width = (int) (100 * factor);
        holder.albumArea.requestLayout();

        holder.albumArea.setPadding(position == 0 ? 10 : 0, 10, 10, 0);

        holder.albumTitle.setText(album.getTitle());
        holder.albumArtist.setText(String.valueOf(album.getYear()));

        Glide.with(context)
                .asBitmap()
                .load(album.getUri())
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                )
                .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        holder.albumImage.setImageBitmap(resource);
                        holder.albumTitle.setTextColor(context.getResources().getColor(R.color.white));
                        holder.albumArtist.setTextColor(context.getResources().getColor(R.color.white));
                        Palette.from(resource).generate(p ->
                                holder.colorArea.setBackgroundColor(p.getMutedColor(p.getVibrantColor(p.getDominantColor(0)))));
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        holder.albumImage.setImageResource(R.drawable.album);
                        holder.colorArea.setBackgroundColor(context.getResources().getColor(R.color.item_area));
                        holder.albumTitle.setTextColor(context.getResources().getColor(R.color.black));
                        holder.albumArtist.setTextColor(context.getResources().getColor(R.color.black));
                    }
                });
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
}
