package com.resultnotifier.main.ui.main;

import android.content.Context;

import com.resultnotifier.main.AppState;
import com.resultnotifier.main.loader.DataLoader;

public class TopmostFragment extends MainFragment {
    public TopmostFragment() {
    }

    @Override
    public DataLoader getDataLoader(final Context context) {
        return AppState.getTopMostDataLoader(context);
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
