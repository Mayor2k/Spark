package com.mayor2k.spark.Adapters;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.mayor2k.spark.Models.Album;
import com.mayor2k.spark.Models.Artist;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;

import java.util.ArrayList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder>{
    private ArrayList<Object> objects;

    public SearchAdapter(ArrayList<Object> theObjects){
        objects=theObjects;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemTitle,itemDescription;
        LinearLayout itemArea;
        ImageButton itemMenu;
        ViewHolder(View v) {
            super(v);
            itemImage = v.findViewById(R.id.itemImageView);
            itemTitle = v.findViewById(R.id.itemTopTextView);
            itemDescription = v.findViewById(R.id.itemBottomTextView);

            itemArea = v.findViewById(R.id.itemArea);
            itemMenu = v.findViewById(R.id.linearMenu);
        }
    }

    @NonNull
    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.linear_item, parent, false);
        return new SearchAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.ViewHolder holder, int position) {
        holder.itemArea.setTag(position);
        holder.itemMenu.setTag(position);
        if (objects.get(position) instanceof Song){
            Song song = (Song) objects.get(position);
            holder.itemTitle.setText(song.getTitle());
            holder.itemDescription.setText(song.getArtist());

            Glide.with(holder.itemImage.getContext())
                    .load(song.getUri())
                    .apply(new RequestOptions()
                            .override(Target.SIZE_ORIGINAL)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .error(R.drawable.album)
                    )
                    .into(holder.itemImage);

        }else if (objects.get(position) instanceof Album){
            Album album = (Album) objects.get(position);
            holder.itemTitle.setText(album.getTitle());
            holder.itemDescription.setText(album.getArtist());

            Glide.with(holder.itemImage.getContext())
                    .load(album.getUri())
                    .apply(new RequestOptions()
                            .override(Target.SIZE_ORIGINAL)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .error(R.drawable.album)
                    )
                    .into(holder.itemImage);
        }else if (objects.get(position) instanceof Artist){
            Artist artist = (Artist) objects.get(position);
            holder.itemTitle.setText(artist.getTitle());
            holder.itemDescription.setText(String.valueOf(artist.getSongInfo())+" song "
                    +String.valueOf(artist.getAlbumInfo())+" album");
        }
    }


    @Override
    public long getItemId(int arg0) {
        return super.getItemId(arg0);
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }
}
