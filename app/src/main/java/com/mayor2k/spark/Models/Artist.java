package com.mayor2k.spark.Models;


public class Artist {
    private long id;
    private String title;

    public Artist(long ArtistId, String titleArtist) {
        id = ArtistId;
        title= titleArtist;
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

    public void setTitle(String title) {
        this.title = title;
    }
}
