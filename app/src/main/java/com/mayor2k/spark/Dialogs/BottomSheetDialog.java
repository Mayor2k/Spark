package com.mayor2k.spark.Dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.mayor2k.spark.Models.Song;
import com.mayor2k.spark.R;
import com.mayor2k.spark.UI.Activities.MainActivity;

import java.io.File;

import static com.mayor2k.spark.Adapters.SongAdapter.isCircle;
import static com.mayor2k.spark.MusicService.isQueue;
import static com.mayor2k.spark.MusicService.queuePosition;
import static com.mayor2k.spark.Adapters.SongAdapter.parentTag;
import static com.mayor2k.spark.UI.Activities.MainActivity.playArray;
import static com.mayor2k.spark.UI.Fragments.SongFragment.songList;
import static com.mayor2k.spark.Helper.CoverUtil.isCover;

public class BottomSheetDialog extends BottomSheetDialogFragment{
    public ImageView cover;
    public TextView title;
    public TextView artist;
    public LinearLayout root;

    public LinearLayout queue;
    public LinearLayout playlist;
    public LinearLayout edit;
    public LinearLayout delete;

    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback
            = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        final Song song =(Song) playArray.get(parentTag);
        View contentView;
        super.setupDialog(dialog, style);
        if (isCover(song,getContext())){
            contentView = View.inflate(getContext(),R.layout.bottom_sheet, null);
        }else{
            contentView = View.inflate(getContext(),R.layout.bottom_sheet_black, null);
        }
        cover = contentView.findViewById(R.id.bottomSheetSongCover);
        title = contentView.findViewById(R.id.bottomSheetSongName);
        artist = contentView.findViewById(R.id.bottomSheetSongArtist);
        root = contentView.findViewById(R.id.bottomSheet);

        queue = contentView.findViewById(R.id.queue);
        playlist = contentView.findViewById(R.id.playlist);
        edit = contentView.findViewById(R.id.edit);
        delete = contentView.findViewById(R.id.delete);

        if (isCover(song,getContext())){
            Glide.with(getContext())
                    .asBitmap()
                    .load(song.getUri())
                    .apply(isCircle?new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL).circleCrop():
                            new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)
                    )
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            cover.setImageBitmap(resource);
                            Palette.from(resource).generate(p -> root.setBackgroundColor(p.getMutedColor(p.getVibrantColor(p.getDominantColor(0)))));
                        }
                    });
        }

        title.setText(song.getTitle());
        artist.setText(song.getArtist());

        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        dialog.setContentView(contentView);

        queue.setOnClickListener(v -> {
            isQueue = true;
            queuePosition = parentTag;
            Toast.makeText(getActivity(),"Song added to queue",Toast.LENGTH_SHORT).show();
            dismiss();
        });

        playlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"playlist",Toast.LENGTH_SHORT).show();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TagEditorDialog tagEditorDialog = new TagEditorDialog();
                tagEditorDialog.show(((FragmentActivity)v.getContext()).getSupportFragmentManager(),
                        tagEditorDialog.getTag());
                dismiss();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Are you really want delete this song?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ContentResolver contentResolver = getActivity().getContentResolver();
                                Uri deleteUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                        songList.get(parentTag).getId());
                                contentResolver.delete(deleteUri, null, null);
                                File f = new File(song.getPath());
                                if (!f.delete()) {
                                    Log.i("TAGGING", "Failed to delete file");
                                }
                                getContext().getContentResolver().notifyChange(Uri.parse("content://media"), null);
                                dismiss();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();
        if( behavior != null && behavior instanceof BottomSheetBehavior ) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
    }
}
