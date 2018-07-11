package com.mayor2k.spark.UI.Activities;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.mayor2k.spark.Adapters.SearchAdapter;
import com.mayor2k.spark.Adapters.SongAdapter;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;

import java.util.ArrayList;

import static com.mayor2k.spark.UI.Fragments.SongFragment.songList;

public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private RecyclerView searchRecyclerView;
    private SearchView searchView;
    public static ArrayList<Object> searchList;
    private SearchAdapter searchAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = findViewById(R.id.searchActivityToolBar);
        searchRecyclerView = findViewById(R.id.searchableList);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        songList = new ArrayList<>();
        searchAdapter = new SearchAdapter(searchList);
        searchRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        searchRecyclerView.setAdapter(searchAdapter);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_icon, menu);

        final MenuItem searchItem = menu.findItem(R.id.searchIconToolBar);
        searchView = (SearchView)MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("Search music");
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchView.setQuery("query",false);
        searchView.post(() -> searchView.setOnQueryTextListener(SearchActivity.this));
        /*searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(SearchActivity.this,"submit",Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                for (int i=0;songList.size()>i;i++){
                    Song song = songList.get(i);
                    if (song.getTitle().contains(newText)){
                        searchList.add(song);
                    }
                }

                songList = new ArrayList<>();
                searchAdapter = new SearchAdapter(searchList);
                searchRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                searchRecyclerView.setAdapter(searchAdapter);
                return false;
            }
        });*/

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
        for (int i=0;songList.size()>i;i++){
            Song song = songList.get(i);
            if (song.getTitle().contains(newText)){
                searchList.add(song);
            }
        }
        return false;
    }
}