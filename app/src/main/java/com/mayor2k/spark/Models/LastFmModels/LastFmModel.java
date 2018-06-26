
package com.mayor2k.spark.Models.LastFmModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mayor2k.spark.Models.Artist;

public class LastFmModel {

    @SerializedName("artist")
    @Expose
    private LastFmArtist artist;

    public LastFmArtist getArtist() {
        return artist;
    }

    public void setArtist(LastFmArtist artist) {
        this.artist = artist;
    }

}
