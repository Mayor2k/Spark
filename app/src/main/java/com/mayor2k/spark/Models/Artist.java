package com.mayor2k.spark.Models;


public class Artist {
    private long id;
    private String title;
    private String url;
    private int songInfo;
    private int albumInfo;

    public Artist(long ArtistId, String titleArtist, String theUrl, int theSongInfo, int theAlbumInfo) {
        id = ArtistId;
        title= titleArtist;
        url=theUrl;
        songInfo=theSongInfo;
        albumInfo=theAlbumInfo;
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

    public int getSongInfo() {
        return songInfo;
    }

    public void setSongInfo(int songInfo) {
        this.songInfo = songInfo;
    }

    public int getAlbumInfo() {
        return albumInfo;
    }

    public void setAlbumInfo(int albumInfo) {
        this.albumInfo = albumInfo;
    }
}
