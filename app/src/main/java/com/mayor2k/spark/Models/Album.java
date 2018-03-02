package com.mayor2k.spark.Models;

import android.net.Uri;

public class Album {
    private long Id;
    private String title;
    private String artist;
    private Uri uri;

    public Album(long idAlbum, String titleAlbum, String artistAlbum, Uri albumUri) {
        Id = idAlbum;
        title = titleAlbum;
        artist = artistAlbum;
        uri = albumUri;
    }

    public long getId() {return Id;}

    public void setId(long id) {Id = id;}

    public String getTitle() {return title;}

    public void setTitle(String title) {this.title = title;}

    public String getArtist() {return artist;}

    public void setArtist(String artist) {this.artist = artist;}

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }
}
