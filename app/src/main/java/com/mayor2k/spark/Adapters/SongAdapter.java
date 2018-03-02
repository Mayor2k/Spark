package com.mayor2k.spark.Adapters;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;

import static com.mayor2k.spark.Utils.CoverUtil.drawableToBitmap;

public class SongAdapter extends CursorAdapter {
    private ArrayList<Song> songs;
    private LayoutInflater songInf;

    public SongAdapter(Context c,Cursor cursor, ArrayList<Song> theSongs){
        super(c,cursor,1);
        songs=theSongs;
        songInf=LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, final View convertView, ViewGroup parent) {
        @SuppressLint("ViewHolder") LinearLayout songLay = (LinearLayout)songInf.inflate
                (R.layout.song_view, parent, false);

        TextView titleView = songLay.findViewById(R.id.songName);
        TextView artistView = songLay.findViewById(R.id.songArtist);
        final ImageView coverView = songLay.findViewById(R.id.songCover);
        Song song = songs.get(position);

        titleView.setText(song.getTitle());
        artistView.setText(song.getArtist());

        Glide.with(coverView.getContext())
                .asBitmap()
                .load(song.getUri())
                .apply(new RequestOptions()
                        .override(64)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                )
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        coverView.setImageBitmap(resource);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        coverView.setImageBitmap(BitmapFactory.decodeResource(coverView.getContext().getResources(),
                                R.drawable.cover_64dp));
                    }
                });
        songLay.setTag(position);
        return songLay;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }
}
