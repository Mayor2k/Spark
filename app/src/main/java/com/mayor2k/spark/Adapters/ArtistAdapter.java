package com.mayor2k.spark.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
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
import com.mayor2k.spark.Models.Artist;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;
import com.mayor2k.spark.Services.MusicService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import static com.mayor2k.spark.LastFm.FinalLastFm.last_fm_ending;
import static com.mayor2k.spark.LastFm.FinalLastFm.last_fm_get_artist_method;
import static com.mayor2k.spark.LastFm.FinalLastFm.last_fm_ws_address;
import static com.mayor2k.spark.UI.Activities.MainActivity.TAG;
import static com.mayor2k.spark.Utils.InternetUtil.isConnected;

public class ArtistAdapter  extends RecyclerViewCursorAdapter<ArtistAdapter.ViewHolder> {
    private ArrayList<Artist> artists;
    Context context;

    public ArtistAdapter(ArrayList<Artist> theArtist, Context theContext){
        super(theContext);
        artists=theArtist;
        context=theContext;

        setupCursorAdapter(null, 0, R.layout.artist_item, false);
    }

    class ViewHolder extends RecyclerViewCursorViewHolder {
        LinearLayout artistArea,colorArea;
        ImageView artistCover;
        TextView artistTitle;
        ViewHolder(View v) {
            super(v);
            artistCover = v.findViewById(R.id.artistCover);
            artistTitle = v.findViewById(R.id.artistTitle);
            colorArea = v.findViewById(R.id.colorArea);
            //getting view for bind tag
            artistArea = v.findViewById(R.id.artistArea);

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


    @Override
    public ArtistAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.artist_item, parent, false);
        view.setOnClickListener(mOnClickListener);

        return new ArtistAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Artist artist = artists.get(position);
        holder.artistArea.setTag(position);
        holder.artistTitle.setText(artist.getTitle());

        if(isConnected(holder.colorArea.getContext())){
            Log.i("tagging","online");
            new ParseTask(artist,holder,holder.artistCover,holder.colorArea).execute();
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
        return artists.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @SuppressLint("StaticFieldLeak")
    class ParseTask extends AsyncTask<Void, Void, String> {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";
        String url;

        Artist artist;
        ViewHolder holder;
        ImageView artistCover;
        LinearLayout colorArea;

        ParseTask(Artist theArtist,ViewHolder theViewHolder,ImageView theArtistCover,LinearLayout theColorArea){
            artist=theArtist;
            holder=theViewHolder;
            artistCover=theArtistCover;
            colorArea=theColorArea;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                URL url = new URL(
                        last_fm_ws_address+last_fm_get_artist_method+artist.getTitle()+last_fm_ending);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                resultJson = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                JSONObject dataJsonObj = new JSONObject(resultJson);
                String array = dataJsonObj.getString("artist");
                JSONObject arrayJsonObj = new JSONObject(array);
                JSONArray jsonArray = arrayJsonObj.getJSONArray("image");
                JSONObject jsonUrl = jsonArray.getJSONObject(3);
                url = jsonUrl.getString("#text");
                Log.d(TAG, url);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return url;
        }

        @Override
        protected void onPostExecute(String url) {
            super.onPostExecute(url);

            Glide.with(holder.artistCover.getContext())
                    .load(url)
                    .apply(new RequestOptions()
                            .override(Target.SIZE_ORIGINAL)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .error(R.drawable.cover)
                    )
                    .listener(GlidePalette.with(url)
                            .use(GlidePalette.Profile.MUTED)
                            .intoBackground(holder.colorArea)
                    )
                    .into(holder.artistCover);
        }
    }
}
