package com.mayor2k.spark.UI.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jaeger.library.StatusBarUtil;
import com.mayor2k.spark.Models.Album;
import com.mayor2k.spark.Models.Artist;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;
import com.mayor2k.spark.UI.Fragments.AlbumFragment;
import com.mayor2k.spark.UI.Fragments.ArtistFragment;
import com.mayor2k.spark.UI.Fragments.BottomPlayerFragment;
import com.mayor2k.spark.UI.Fragments.PlayerFragment;
import com.mayor2k.spark.UI.Fragments.SongFragment;
import com.mayor2k.spark.Interfaces.Constants;
import com.mayor2k.spark.Adapters.ViewPagerAdapter;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;
import java.util.Objects;

import static com.mayor2k.spark.Adapters.AlbumAdapter.currentAlbum;
import static com.mayor2k.spark.Adapters.SongAdapter.serviceIntent;
import static com.mayor2k.spark.Adapters.SongAdapter.songPosition;
import static com.mayor2k.spark.MusicService.pausePosition;
import static com.mayor2k.spark.MusicService.playSong;
import static com.mayor2k.spark.MusicService.player;
import static com.mayor2k.spark.UI.Fragments.AlbumFragment.albumList;
import static com.mayor2k.spark.UI.Fragments.ArtistFragment.artistList;

public class MainActivity extends AppCompatActivity{
    public static final String TAG = "TAGGING";
    public static ArrayList<Song> playArray;
    public TabLayout tabLayout;
    public ViewPager viewPager;
    public static boolean isPlayerOpen = false;

    FrameLayout bottomPlayerFragmentView;
    public static boolean isFirstClick;
    public  ViewGroup.MarginLayoutParams lp;
    public BottomPlayerFragment bottomPlayerFragment;
    PlayerFragment playerFragment;
    SlidingUpPanelLayout slidingUpPanelLayout;
    Toolbar toolbar;

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new SongFragment(), "Tracks");
        adapter.addFragment(new AlbumFragment(), "Albums");
        adapter.addFragment(new ArtistFragment(), "Artists");
        viewPager.setAdapter(adapter);
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarUtil.setLightMode(MainActivity.this);
        bottomPlayerFragment = new BottomPlayerFragment();
        playerFragment = new PlayerFragment();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        viewPager = findViewById(R.id.content);
        setupViewPager(viewPager);
        lp = (ViewGroup.MarginLayoutParams) viewPager.getLayoutParams();
        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        bottomPlayerFragmentView = findViewById(R.id.bottom_player_container);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.player_container, playerFragment)
                .commit();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.bottom_player_container, bottomPlayerFragment)
                .commit();

        slidingUpPanelLayout = findViewById(R.id.container);
        isStoragePermissionGranted();
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onStart() {
        super.onStart();
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                bottomPlayerFragmentView.setAlpha(1-slideOffset);
                if (slideOffset==1){
                    StatusBarUtil.setDarkMode(MainActivity.this);
                    Toolbar toolbar1 = playerFragment.getView().findViewById(R.id.playerToolbar);
                    isPlayerOpen = true;
                    setSupportActionBar(toolbar1);
                    toolbar1.setTitle(" ");
                    toolbar1.setNavigationIcon(getResources().getDrawable(R.drawable.ic_close_24dp));
                    toolbar1.setNavigationOnClickListener(v -> {
                        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                    });
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        getWindow().setStatusBarColor(ContextCompat.getColor(MainActivity.this,R.color.transparent));
                }else{
                    StatusBarUtil.setLightMode(MainActivity.this);
                    isPlayerOpen = false;
                    setSupportActionBar(toolbar);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        getWindow().setStatusBarColor(ContextCompat.getColor(MainActivity.this,R.color.white));
                }
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
            }
        });
        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        String jsonArray = sPref.getString("LastPlayArray", "");
        isFirstClick = jsonArray.equals("");
        if (!isFirstClick) {
            playArray = new Gson().fromJson(jsonArray,new TypeToken<ArrayList<Song>>(){}.getType());
            songPosition = sPref.getInt("LastSong",-1);
            if (playSong==null){
                pausePosition = sPref.getInt("LastPosition",-1);
                playSong = playArray.get(songPosition);
            }
            //bottom player fragment
            TextView songTitle = bottomPlayerFragment.getView().findViewById(R.id.bottom_player_song_title);
            songTitle.setText(playSong.getTitle());
            bottomPlayerFragment.progressListener();
            //player fragment
            playerFragment.updateView();
            playerFragment.progressListener();

            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            int bottom = (int) (getResources().getDimension(R.dimen.bottom_margin)/getResources().getDisplayMetrics().density);
            lp.setMargins(0, 0, 0, bottom);
            viewPager.requestLayout();
        }else{
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            lp.setMargins(0, 0, 0, 0);
            viewPager.requestLayout();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (playSong!=null) {
            SharedPreferences sPref = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor ed = sPref.edit();
            ed.putInt("LastPosition",player.getCurrentPosition());
            ed.apply();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        menu.clear();
        if(isPlayerOpen){
            inflater.inflate(R.menu.player_navigation, menu);
        }else {
            inflater.inflate(R.menu.search, menu);
            inflater.inflate(R.menu.navigation, menu);
            SubMenu subMenu = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, "Grid size");
            subMenu.add(Menu.NONE, 1, Menu.NONE, "1");
            subMenu.add(Menu.NONE, 2, Menu.NONE, "2");
            subMenu.add(Menu.NONE, 3, Menu.NONE, "3");
            subMenu.add(Menu.NONE, 4, Menu.NONE, "4");
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.searchToolBar):
                Intent intent1 = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent1);
                return true;
            case (R.id.navigation_setting):
                Intent intent2 = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent2);
                return true;
            case (R.id.findLyrics):
                Toast.makeText(MainActivity.this, "lyrics", Toast.LENGTH_SHORT).show();
                return true;
            case (R.id.addToPlaylist):
                Toast.makeText(MainActivity.this, "playlist", Toast.LENGTH_SHORT).show();
                return true;
            case (R.id.goToQueue):
                startActivity(new Intent(MainActivity.this, QueueActivity.class));
                return true;
            case (R.id.goToAlbum):
                for (int i = 0; albumList.size() > i; i++) {
                    Album album = albumList.get(i);
                    if (Objects.equals(album.getTitle(), playSong.getAlbum())) {
                        currentAlbum = albumList.indexOf(album);
                        break;
                    }
                }
                startActivity(new Intent(MainActivity.this, AlbumActivity.class));
                return true;
            case (R.id.goToArtist):
                Intent intent = new Intent(MainActivity.this, ArtistActivity.class);
                for (int i = 0; artistList.size() > i; i++) {
                    Artist artist = artistList.get(i);
                    if (Objects.equals(artist.getTitle(), playSong.getArtist())) {
                        intent.putExtra("currentArtist", artistList.indexOf(artist));
                        break;
                    }
                }
                startActivity(intent);
                return true;
        }
        return false;
    }

    public void isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }
        else {
            Log.v(TAG,"Permission is granted");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        serviceIntent.setAction(Constants.STOPFOREGROUND_ACTION);
        stopService(serviceIntent);
        if (playSong!=null) {
            SharedPreferences sPref = getPreferences(MODE_PRIVATE);
            @SuppressLint("CommitPrefEdits") SharedPreferences.Editor ed = sPref.edit();
            ed.putInt("LastSong",playArray.indexOf(playSong));
            ed.putInt("LastPosition",player.getCurrentPosition());
            ed.putString("LastPlayArray", new Gson().toJson(playArray));
            ed.apply();
        }
    }

    public static float getScreenWidth(Context context){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return displayMetrics.widthPixels / displayMetrics.density;
    }

    public void addPlayerFragment(){
        slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        int bottom = (int) (getResources().getDimension(R.dimen.bottom_margin)/getResources().getDisplayMetrics().density);
        lp.setMargins(0, 0, 0, bottom);
        viewPager.requestLayout();
    }

    public BottomPlayerFragment getBottomPlayerFragment(){
        return bottomPlayerFragment;
    }

    public PlayerFragment getPlayerFragment(){return playerFragment;}
}