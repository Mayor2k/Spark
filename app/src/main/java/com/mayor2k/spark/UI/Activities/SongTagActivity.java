package com.mayor2k.spark.UI.Activities;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;
import com.mayor2k.spark.UI.Fragments.SongFragment;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;

public class SongTagActivity extends AppCompatActivity {
    Song song = SongFragment.songList.get(MainActivity.parentTag);
    private EditText songTitle;
    private EditText albumTitle;
    private EditText artistTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_tag);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

        }
        getSupportActionBar().setTitle(R.string.tag_editor);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    saveStrings();
                } catch (TagException | CannotWriteException | ReadOnlyFileException
                        | InvalidAudioFrameException | CannotReadException | IOException e) {
                    finish();
                }
            }
        });

        songTitle = findViewById(R.id.songTitle);
        albumTitle = findViewById(R.id.albumTitle);
        artistTitle = findViewById(R.id.artistTitle);
        setStrings();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void setStrings(){
        songTitle.setText(song.getTitle());
        albumTitle.setText(song.getAlbum());
        artistTitle.setText(song.getArtist());
    }

    private void saveStrings() throws TagException, CannotWriteException, ReadOnlyFileException,
            CannotReadException, InvalidAudioFrameException, IOException {
        File mp3 = new File(song.getPath());
        AudioFile f = AudioFileIO.read(mp3);
        Tag tag = f.getTag();
        tag.setField(FieldKey.TITLE,songTitle.getText().toString());
        tag.setField(FieldKey.ALBUM,albumTitle.getText().toString());
        tag.setField(FieldKey.ARTIST,artistTitle.getText().toString());
        f.commit();
        //getApplicationContext().getContentResolver().notifyChange(Uri.parse("content://media"), null);
        finish();
    }
}
