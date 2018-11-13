package com.mayor2k.spark.Models;

import android.net.Uri;

public class Song {

    private long id;
    private String album;
    private String title;
    private String artist;
    private String path;
    private Uri uri;
    private int track;
    private int duration;
    private int position;

    public Song(long songID, String songTitle, String songArtist, String songAlbum,
                String songPath, Uri songUri, int songTrack, int songDuration, int position) {
        id=songID;
        uri=songUri;
        title=songTitle;
        album=songAlbum;
        artist=songArtist;
        path=songPath;
        track=songTrack;
        duration=songDuration;
        this.position = position;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {this.title = title;}

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {return album;}

    public void setAlbum(String album) {this.album = album;}

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getTrack() {
        return track;
    }

    public void setTrack(int track) {
        this.track = track;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
