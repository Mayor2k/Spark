package com.mayor2k.spark.Utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;

import com.mayor2k.spark.Models.Album;
import com.mayor2k.spark.Models.Song;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class AlbumUtil {
    public static Bitmap getPaletteAlbum(Album album,Context context){
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inSampleSize = 32;
        Bitmap songImage;
        ContentResolver res = context.getContentResolver();
        InputStream in;
        try {
            in = res.openInputStream(album.getUri());
            songImage = BitmapFactory.decodeStream(in,null,opt);
        } catch (FileNotFoundException e) {
            songImage = null;
        }
        return songImage;
    }

    public static Boolean isAlbumCover(Album album,Context context){
        Boolean check;
        ContentResolver res = context.getContentResolver();
        try {
            InputStream in = res.openInputStream(album.getUri());
            check = true;
        } catch (FileNotFoundException e) {
            check = false;
        }
        return check;
    }

}
