package com.mayor2k.spark.UI.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;
import com.mayor2k.spark.UI.Fragments.AlbumFragment;
import com.mayor2k.spark.UI.Fragments.ArtistFragment;
import com.mayor2k.spark.UI.Fragments.SongFragment;
import com.mayor2k.spark.Interfaces.Constants;
import com.mayor2k.spark.UI.Fragments.ViewPagerAdapter;

import java.util.ArrayList;

import static com.mayor2k.spark.Adapters.SongAdapter.serviceIntent;

public class MainActivity extends AppCompatActivity{
    public static final String TAG = "TAGGING";
    public static int currentAlbum;
    public static ArrayList<Song> playArray;
    public TabLayout tabLayout;
    public ViewPager viewPager;
    public Toolbar toolbar;


    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new SongFragment(), "Tracks");
        adapter.addFragment(new AlbumFragment(), "Albums");
        adapter.addFragment(new ArtistFragment(), "Artists");
        viewPager.setAdapter(adapter);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        viewPager = findViewById(R.id.content);
        setupViewPager(viewPager);
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        isStoragePermissionGranted();
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        inflater.inflate(R.menu.navigation, menu);
        return true;
    }

    public void onAlbumClick(View v){
        currentAlbum = (Integer)v.getTag();
        Intent intent = new Intent(MainActivity.this, AlbumActivity.class);
        startActivity(intent);
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else {
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        serviceIntent.setAction(Constants.STOPFOREGROUND_ACTION);
        stopService(serviceIntent);
    }

    public void startAnotherActivity(MenuItem item) {
        Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
        startActivity(intent);
    }
}