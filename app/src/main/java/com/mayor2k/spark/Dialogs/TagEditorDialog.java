package com.mayor2k.spark.Dialogs;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import static com.mayor2k.spark.Adapters.SongAdapter.parentTag;

public class TagEditorDialog extends DialogFragment{
    Song song = SongFragment.songList.get(parentTag);
    private EditText songTitle,albumTitle,artistTitle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = View.inflate(getContext(), R.layout.content_song_tag, null);
        songTitle = contentView.findViewById(R.id.songTitle);
        albumTitle = contentView.findViewById(R.id.albumTitle);
        artistTitle = contentView.findViewById(R.id.artistTitle);
        Button cancelButton = contentView.findViewById(R.id.cancel_action);
        cancelButton.setOnClickListener(cancelOnClickListener);
        Button saveButton = contentView.findViewById(R.id.save_action);
        saveButton.setOnClickListener(saveOnClickListener);
        setStrings();
        return contentView;
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
        getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(mp3)));
        dismiss();
    }

    private final View.OnClickListener cancelOnClickListener = v -> dismiss();

    private final View.OnClickListener saveOnClickListener = v ->{
        try {
            saveStrings();
        } catch (TagException | CannotReadException | ReadOnlyFileException |
                CannotWriteException | IOException | InvalidAudioFrameException e) {
            dismiss();
        }
    };
}
