package com.mayor2k.spark.UI.Activities;

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
import android.widget.Toast;

import com.mayor2k.spark.Adapters.SearchAdapter;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;
import com.mayor2k.spark.Utils.WrappedAsyncTaskLoader;

import java.util.ArrayList;

import static com.mayor2k.spark.UI.Fragments.SongFragment.songList;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener,LoaderManager.LoaderCallbacks<ArrayList<Object>> {

    public String query = "QUERY";
    private SearchView searchView;
    public static ArrayList<Object> searchList;
    SearchAdapter searchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = findViewById(R.id.searchActivityToolBar);
        RecyclerView searchRecyclerView = findViewById(R.id.searchableList);
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
        searchView.setQueryHint("Search music");
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setQuery(query,false);
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

        @Override
        public ArrayList<Object> loadInBackground() {
            for (int i=0;songList.size()>i;i++){
                Song song = songList.get(i);
                if (song.getTitle().contains(query)){
                    Log.i(MainActivity.TAG,"YES");
                    searchList.add(song);
                }
            }
            return searchList;
        }
    }
}