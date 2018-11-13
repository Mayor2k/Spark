package com.mayor2k.spark.Helper;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public abstract class WrappedAsyncTaskLoader<D> extends AsyncTaskLoader<D> {

    private D mData;

    public WrappedAsyncTaskLoader(Context context) {
        super(context);
    }

    @Override
    public void deliverResult(D data) {
        if (!isReset()) {
            this.mData = data;
            super.deliverResult(data);
        } else {
            // An asynchronous query came in while the loader is stopped
        }
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (this.mData != null) {
            deliverResult(this.mData);
        } else if (takeContentChanged() || this.mData == null) {
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        super.onStopLoading();
        // Attempt to cancel the current load task if possible
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();
        // Ensure the loader is stopped
        onStopLoading();
        this.mData = null;
    }
}