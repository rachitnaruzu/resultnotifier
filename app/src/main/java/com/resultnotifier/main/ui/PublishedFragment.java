package com.resultnotifier.main.ui;

import android.content.Context;

import com.resultnotifier.main.AppState;
import com.resultnotifier.main.loader.DataLoader;

public class PublishedFragment extends MainFragment {
    public PublishedFragment() {
    }

    @Override
    public DataLoader getDataLoader(final Context context) {
        return AppState.getPublishedDataloader(context);
    }

    @Override
    public boolean shouldHonourOffset() {
        return true;
    }
}
