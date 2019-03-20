package com.mayor2k.spark.Adapters;


import android.content.Context;
import android.database.Cursor;
import android.view.View;

import com.androidessence.recyclerviewcursoradapter.RecyclerViewCursorAdapter;
import com.androidessence.recyclerviewcursoradapter.RecyclerViewCursorViewHolder;

public abstract class AbsAdapter extends RecyclerViewCursorAdapter<AbsAdapter.ViewHolder> {
    AbsAdapter(Context context) {
        super(context);
    }

    public abstract class ViewHolder extends RecyclerViewCursorViewHolder {
        public ViewHolder(View view) {
            super(view);
        }

        @Override
        public void bindCursor(Cursor cursor) {

        }
    }
}
