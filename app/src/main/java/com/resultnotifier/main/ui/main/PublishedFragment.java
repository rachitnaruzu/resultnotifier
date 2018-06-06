package com.resultnotifier.main.ui.main;

import android.content.Context;

import com.resultnotifier.main.AppState;
import com.resultnotifier.main.loader.DataLoader;

public class PublishedFragment extends MainFragment {
    public PublishedFragment() {
    }

    @Override
    public DataLoader getDataLoader(final Context context) {
        return AppState.getPublishedDataLoader(context);
    }

    @Override
    public boolean shouldHonourOffset() {
        return true;
    }

    @Override
    public boolean shouldDisplayNonSavedFiles() {
        return true;
    }
}
