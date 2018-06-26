package com.mayor2k.spark.Models;


public class Artist {
    private long id;
    private String title;
    private String url;

    public Artist(long ArtistId, String titleArtist, String theUrl) {
        id = ArtistId;
        title= titleArtist;
        url=theUrl;
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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
