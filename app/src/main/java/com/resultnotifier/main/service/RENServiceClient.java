package com.resultnotifier.main.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.resultnotifier.main.FileData;

import java.util.ArrayList;
import java.util.List;

public interface RENServiceClient {
    void updateSelfViews(final ArrayList<FileData> fileDataItems);

    void fetchPublishedFiles(final int offset, final String dataType,
                             final FetchFilesCallback fetchFilesCallback);
    void fetchRecentFiles(final int offset, final String dataType,
                          final FetchFilesCallback fetchFilesCallback);
    void fetchTopMostFiles(final int offset, final String dataType,
                           final FetchFilesCallback fetchFilesCallback);

    void incrementViewsByOne(@NonNull final String fileId,
                             @Nullable final IncrementViewsCallback incrementViewsCallback);

    void fetchDataTypes(final FetchDataTypesCallback fetchDataTypesCallback);

    interface FetchFilesCallback {
        void onSuccess(final List<FileData> files);
        void onError(final int error);
    }

    interface IncrementViewsCallback {
        void onSuccess();
        void onError(final int error);
    }

    interface FetchDataTypesCallback {
        void onSuccess(final List<String> dataTypes);
        void onError(final int error);
    }
}
