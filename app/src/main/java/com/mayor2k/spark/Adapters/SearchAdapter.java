package com.mayor2k.spark.Adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.mayor2k.spark.Interfaces.Constants;
import com.mayor2k.spark.Models.Album;
import com.mayor2k.spark.Models.Artist;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;
import com.mayor2k.spark.UI.Activities.AlbumActivity;
import com.mayor2k.spark.UI.Activities.ArtistActivity;

import java.util.ArrayList;

import static com.mayor2k.spark.Adapters.SongAdapter.isCircle;
import static com.mayor2k.spark.Adapters.SongAdapter.serviceIntent;
import static com.mayor2k.spark.Adapters.SongAdapter.songPosition;
import static com.mayor2k.spark.UI.Activities.SearchActivity.searchList;
import static com.mayor2k.spark.UI.Fragments.AlbumFragment.albumList;
import static com.mayor2k.spark.Adapters.AlbumAdapter.currentAlbum;
import static com.mayor2k.spark.UI.Fragments.ArtistFragment.artistList;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private ArrayList<Object> objects;

    final private int SONG_HEADER = 0;
    final private int ALBUM_HEADER = 1;
    final private int ARTIST_HEADER = 2;
    final private int NORMAL_ITEM = 3;

    public SearchAdapter(ArrayList<Object> theObjects){
        objects=theObjects;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemTitle,itemDescription,headerText;
        LinearLayout itemArea;
        ImageButton itemMenu;
        ViewHolder(View v) {
            super(v);
            itemImage = v.findViewById(R.id.itemImageView);
            itemTitle = v.findViewById(R.id.itemTopTextView);
            itemDescription = v.findViewById(R.id.itemBottomTextView);
            //headerTextView
            headerText = v.findViewById(R.id.headerTextView);

            itemArea = v.findViewById(R.id.itemArea);
            itemMenu = v.findViewById(R.id.linearMenu);
        }
    }

    private final View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            int position = (Integer)v.getTag();
            Object object = objects.get(position);
            if (object instanceof Song){
                songPosition = position-1;
                serviceIntent.setAction(Constants.START_SEARCH_ACTION);
                v.getContext().startService(serviceIntent);
            }else if(object instanceof Album){
                currentAlbum = albumList.indexOf(object);
                Intent intent = new Intent(v.getContext(), AlbumActivity.class);
                v.getContext().startActivity(intent);
            }else if(object instanceof Artist){
                Intent intent = new Intent(v.getContext(), ArtistActivity.class);
                intent.putExtra("currentArtist",artistList.indexOf(object));
                v.getContext().startActivity(intent);
            }
        }
    };

    @NonNull
    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType==NORMAL_ITEM){
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.linear_item, parent, false);
            View item_area = view.findViewById(R.id.itemArea);
            item_area.getTag();
            item_area.setOnClickListener(onClickListener);
        }
        else{
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.header_item, parent, false);
        }

        return new SearchAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.ViewHolder holder, int position) {
        if (getItemViewType(position)==SONG_HEADER)
            holder.headerText.setText("Song");
        else if (getItemViewType(position)==ALBUM_HEADER)
            holder.headerText.setText("Album");
        else if (getItemViewType(position)==ARTIST_HEADER)
            holder.headerText.setText("Artist");

        else{
            holder.itemArea.setPadding(10,position==0?10:0,0,10);

            holder.itemArea.setTag(position);
            holder.itemMenu.setTag(position);
            if (objects.get(position) instanceof Song){
                Song song = (Song) objects.get(position);
                holder.itemTitle.setText(song.getTitle());
                holder.itemDescription.setText(song.getArtist());

                Glide.with(holder.itemImage.getContext())
                        .asBitmap()
                        .load(song.getUri())
                        .apply(isCircle?new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop():
                                new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                        )
                        .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                holder.itemImage.setImageBitmap(resource);
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                RoundedBitmapDrawable circularBitmapDrawable =
                                        RoundedBitmapDrawableFactory.create(holder.itemImage.getContext().getResources(),
                                                BitmapFactory.decodeResource(holder.itemImage.getContext().getResources(), R.drawable.album));
                                circularBitmapDrawable.setCircular(true);
                                holder.itemImage.setImageDrawable(circularBitmapDrawable);
                            }
                        });

            }else if (objects.get(position) instanceof Album){
                Album album = (Album) objects.get(position);
                holder.itemTitle.setText(album.getTitle());
                holder.itemDescription.setText(album.getArtist());

                Glide.with(holder.itemImage.getContext())
                        .asBitmap()
                        .load(album.getUri())
                        .apply(isCircle?new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop():
                                new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                        )
                        .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                holder.itemImage.setImageBitmap(resource);
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                holder.itemImage.setImageResource(R.drawable.album);
                            }
                        });
            }else if (objects.get(position) instanceof Artist){
                Artist artist = (Artist) objects.get(position);
                holder.itemTitle.setText(artist.getTitle());
                holder.itemDescription.setText(String.valueOf(artist.getSongInfo())+" song "
                        +String.valueOf(artist.getAlbumInfo())+" album");

                Glide.with(holder.itemImage.getContext())
                        .asBitmap()
                        .load(artist.getUrl())
                        .apply(isCircle?new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop():
                                new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                        )
                        .into(new SimpleTarget<Bitmap>(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                holder.itemImage.setImageBitmap(resource);
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                holder.itemImage.setImageResource(R.drawable.album);
                            }
                        });
            }
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
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemViewType(int position) {
        if (searchList.get(position)=="SONG_HEADER")
            return SONG_HEADER;
        else if (searchList.get(position)=="ALBUM_HEADER")
            return ALBUM_HEADER;
        else if (searchList.get(position)=="ARTIST_HEADER")
            return ARTIST_HEADER;
        else
            return NORMAL_ITEM;
    }
}