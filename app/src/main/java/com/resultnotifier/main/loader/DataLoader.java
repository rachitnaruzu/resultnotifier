package com.resultnotifier.main.loader;

import com.resultnotifier.main.FileData;

import java.util.List;

public interface DataLoader {
    void fetchData(final int offset, final String dataTypes,
                   final DataLoaderCallback dataLoaderCallback);

    interface DataLoaderCallback {
        void onSuccess(final List<FileData> files);
        void onError(final int error);
    }
}
