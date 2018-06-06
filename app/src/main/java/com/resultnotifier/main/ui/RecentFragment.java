package com.resultnotifier.main.ui;

import android.content.Context;

import com.resultnotifier.main.AppState;
import com.resultnotifier.main.loader.DataLoader;

public class RecentFragment extends MainFragment {
    public RecentFragment() {
    }

    @Override
    public DataLoader getDataLoader(final Context context) {
        return AppState.getRecentDataloader(context);
    }

    @Override
    public boolean shouldHonourOffset() {
        return true;
    }
}
