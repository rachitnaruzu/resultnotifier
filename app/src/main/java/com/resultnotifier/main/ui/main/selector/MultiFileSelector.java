package com.resultnotifier.main.ui.main.selector;

import android.widget.AbsListView;

public interface MultiFileSelector extends AbsListView.MultiChoiceModeListener {
    void exitActionMode();

    interface MultiFileSelectorCallback {
        void deleteSelectedFiles();
        void deSelect();
    }
}
