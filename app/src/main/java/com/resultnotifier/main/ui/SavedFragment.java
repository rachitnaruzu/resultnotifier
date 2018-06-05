package com.resultnotifier.main.ui;

import android.content.Context;

import com.resultnotifier.main.AppState;
import com.resultnotifier.main.loader.DataLoader;
import com.resultnotifier.main.ui.MainFragment;


public class SavedFragment extends MainFragment {

    public SavedFragment() {
    }

    @Override
    public DataLoader getDataLoader(final Context context) {
        return AppState.getSavedDataloader(context);
    }
}
