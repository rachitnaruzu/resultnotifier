package com.resultnotifier.main.ui.main;

import android.content.Context;

import com.resultnotifier.main.AppState;
import com.resultnotifier.main.loader.DataLoader;

public class SavedFragment extends MainFragment {
    public SavedFragment() {
    }

    @Override
    public DataLoader getDataLoader(final Context context) {
        return AppState.getSavedDataLoader(context);
    }

    @Override
    public boolean shouldHonourOffset() {
        return false;
    }
}
