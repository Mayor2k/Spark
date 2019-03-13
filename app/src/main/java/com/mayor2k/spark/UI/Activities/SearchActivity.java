package com.mayor2k.spark.UI.Activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.jaeger.library.StatusBarUtil;
import com.mayor2k.spark.Adapters.SearchAdapter;
import com.mayor2k.spark.Models.Album;
import com.mayor2k.spark.Models.Artist;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;
import com.mayor2k.spark.Helper.WrappedAsyncTaskLoader;

import java.util.ArrayList;
import java.util.Objects;

import static com.mayor2k.spark.UI.Fragments.AlbumFragment.albumList;
import static com.mayor2k.spark.UI.Fragments.ArtistFragment.artistList;
import static com.mayor2k.spark.UI.Fragments.SongFragment.songList;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener,
        LoaderManager.LoaderCallbacks<ArrayList<Object>> {

    public String query = "QUERY";
    private SearchView searchView;
    public static ArrayList<Object> searchList;
    public SearchAdapter searchAdapter;
    private TextView noResults;
    public static ArrayList<Song> searchSong = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setLightMode(SearchActivity.this);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = findViewById(R.id.searchActivityToolBar);
        RecyclerView searchRecyclerView = findViewById(R.id.searchableList);
        noResults = findViewById(R.id.emptyTextView);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        searchList = new ArrayList<>();
        searchAdapter = new SearchAdapter(searchList);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchRecyclerView.setAdapter(searchAdapter);
        getSupportLoaderManager().initLoader(0, null, this);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_icon, menu);

        final MenuItem searchItem = menu.findItem(R.id.searchIconToolBar);
        searchView = (SearchView)MenuItemCompat.getActionView(searchItem);
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Search music");
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.post(() -> searchView.setOnQueryTextListener(SearchActivity.this));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(SearchActivity.this,"submit",Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        search(newText);
        return false;
    }

    private void search(@NonNull String query) {
        this.query = query;
        getSupportLoaderManager().restartLoader(0, null, this);
    }

    @NonNull
    @Override
    public Loader<ArrayList<Object>> onCreateLoader(int id, @Nullable Bundle args) {
        return new AsyncSearchResultLoader(this, query);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<Object>> loader, ArrayList<Object> data) {
        noResults.setVisibility(searchList.size()==0 ? View.VISIBLE : View.INVISIBLE);
        searchAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<Object>> loader) {
    }

    private static class AsyncSearchResultLoader extends WrappedAsyncTaskLoader<ArrayList<Object>>{
        private final String query;

        AsyncSearchResultLoader(Context context, String query) {
            super(context);
            this.query = query;
        }

        @SuppressLint("ResourceType")
        @Override
        public ArrayList<Object> loadInBackground() {
            if (Objects.equals(query, "")){
                searchList.clear();
                return searchList;
            }
            ArrayList<Object> albums = new ArrayList<>();
            ArrayList<Object> artists = new ArrayList<>();

            searchList.clear();
            
            for (int i=0;songList.size()>i;i++){
                Song song = songList.get(i);
                if (containsIgnoreCase(song.getTitle(),query))
                    searchSong.add(song);
            }
            if (searchSong.size()!=0){
                searchList.add(0,"SONG_HEADER");
                searchList.addAll(searchSong);
            }

            for (int i=0;albumList.size()>i;i++){
                Album album = albumList.get(i);
                if (containsIgnoreCase(album.getTitle(),query))
                    albums.add(album);
            }
            if (albums.size()!=0){
                albums.add(0,"ALBUM_HEADER");
                searchList.addAll(albums);
            }

            for (int i=0;artistList.size()>i;i++){
                Artist artist = artistList.get(i);
                if (containsIgnoreCase(artist.getTitle(),query))
                    artists.add(artist);
            }
            if (artists.size()!=0){
                artists.add(0,"ARTIST_HEADER");
                searchList.addAll(artists);
            }

            Log.i("TAGGING",""+searchList);

            return searchList;
        }


        boolean containsIgnoreCase(String str, String searchStr)     {
            if(str == null || searchStr == null) return false;

            final int length = searchStr.length();
            if (length == 0)
                return true;

            for (int i = str.length() - length; i >= 0; i--) {
                if (str.regionMatches(true, i, searchStr, 0, length))
                    return true;
            }
            return false;
        }
    }
}