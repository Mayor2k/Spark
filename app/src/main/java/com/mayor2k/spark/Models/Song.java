package com.mayor2k.spark.Models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class Song implements Parcelable {

    private long id;
    private String album;
    private String title;
    private String artist;
    private String path;
    private String uri;
    private int track;
    private int duration;

    public Song(long id, String title, String artist, String songAlbum,
                String path, String uri, int track, int duration) {
        this.id=id;
        this.uri=uri;
        this.title=title;
        this.album=songAlbum;
        this.artist=artist;
        this.path=path;
        this.track=track;
        this.duration=duration;
    }

    public Song(Parcel in) {
        id = in.readLong();
        album = in.readString();
        title = in.readString();
        artist = in.readString();
        path = in.readString();
        uri = in.readParcelable(Uri.class.getClassLoader());
        track = in.readInt();
        duration = in.readInt();
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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flag) {
        dest.writeLong(id);
        dest.writeString(album);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(path);
        dest.writeString(uri.toString());
        dest.writeInt(track);
        dest.writeInt(duration);
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel in) {
            return new Song(in);
        }

        @Override
        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}
