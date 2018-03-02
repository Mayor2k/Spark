package com.mayor2k.spark.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mayor2k.spark.Models.Artist;
import com.mayor2k.spark.R;
import com.mayor2k.spark.UI.Activities.MainActivity;

import java.util.ArrayList;

import static com.mayor2k.spark.Utils.CoverUtil.isCover;

public class ArtistAdapter extends CursorAdapter {
    private ArrayList<Artist> artists;
    private LayoutInflater artistInf;

    public ArtistAdapter(Context c, Cursor cursor, ArrayList<Artist> theArtist){
        super(c,cursor,1);
        artists=theArtist;
        artistInf=LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return artists.size();
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
    public View getView(int position, View convertView, ViewGroup parent) {

        LinearLayout artistLayout = (LinearLayout)artistInf.inflate
                (R.layout.artist_view, parent, false);

        TextView artistView = artistLayout.findViewById(R.id.artistTitle);
        //ImageView coverView = (ImageView)artistLayout.findViewById(R.id.artistCover);
        //LinearLayout colorArea = (LinearLayout)artistLayout.findViewById(R.id.colorArea);

        Artist currentArtist = artists.get(position);
        artistView.setText(currentArtist.getTitle());

        artistLayout.setTag(position);
        return artistLayout;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }
}
