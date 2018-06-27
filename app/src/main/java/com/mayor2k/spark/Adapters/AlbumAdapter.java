package com.mayor2k.spark.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.florent37.glidepalette.GlidePalette;
import com.mayor2k.spark.Models.Album;
import com.mayor2k.spark.R;

import java.util.ArrayList;

public class AlbumAdapter extends CursorAdapter {
    private ArrayList<Album> albums;
    private LayoutInflater albumInf;

    public AlbumAdapter(Context c, Cursor cursor, ArrayList<Album> theAlbum){
        super(c,cursor,1);
        albums=theAlbum;
        albumInf=LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return albums.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @SuppressLint("CheckResult")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        @SuppressLint("ViewHolder") LinearLayout albumLayout = (LinearLayout)albumInf.inflate
                (R.layout.album_view, parent, false);

        final TextView albumView = albumLayout.findViewById(R.id.albumName);
        final TextView artistView = albumLayout.findViewById(R.id.albumArtist);
        final ImageView coverView = albumLayout.findViewById(R.id.albumCover);
        final LinearLayout colorArea = albumLayout.findViewById(R.id.colorArea);
        final Context context = coverView.getContext();

        final Album album = albums.get(position);
        albumView.setText(album.getTitle());
        artistView.setText(album.getArtist());

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
                                        albumView.setTextColor(ContextCompat.getColor(context, R.color.white));
                                        artistView.setTextColor(ContextCompat.getColor(context, R.color.white));
                                    }
                                })
                        .intoBackground(colorArea)
                )
                .into(coverView);
        albumLayout.setTag(position);
        return albumLayout;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }
}
