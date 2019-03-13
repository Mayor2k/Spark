package com.mayor2k.spark.Helper;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import com.mayor2k.spark.Models.Song;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class CoverUtil {
    public static Bitmap getCoverBitmap(Song song, Context context) {
        Bitmap songImage;
        ContentResolver res = context.getContentResolver();
        InputStream in;
        try {
            in = res.openInputStream(Uri.parse(song.getUri()));
            songImage = BitmapFactory.decodeStream(in);
        } catch (FileNotFoundException e) {
            songImage = null;
        }
        return songImage;
    }

    public static Bitmap getPaletteBitmap(Song song) {
        MediaMetadataRetriever metaRetriever = new MediaMetadataRetriever();
        metaRetriever.setDataSource(song.getPath());
        Bitmap songImage;
        try {
            byte[] art = metaRetriever.getEmbeddedPicture();
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = 32;
            songImage = BitmapFactory.decodeByteArray(art, 0, art.length, opt);
        } catch (Exception e) {
            songImage = null;
        }
        return songImage;
    }

    public static Boolean isCover(Song song, Context context) {
        Boolean check;
        ContentResolver res = context.getContentResolver();
        try {
            InputStream in = res.openInputStream(Uri.parse(song.getUri()));
            check = true;
        } catch (FileNotFoundException e) {
            check = false;
        }
        return check;
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
