
package com.mayor2k.spark.Models.LastFmModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mayor2k.spark.Models.LastFmModels.Link;

public class Links {

    @SerializedName("link")
    @Expose
    private Link link;

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

}
