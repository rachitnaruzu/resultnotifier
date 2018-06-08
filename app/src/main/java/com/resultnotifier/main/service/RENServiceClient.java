package com.resultnotifier.main.service;

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

    void incrementViewsByOne(final String fileId,
                             final IncrementViewsCallback incrementViewsCallback);

    void fetchDataTypes(final FetchDataTypesCallback fetchDataTypesCallback);

    void updateToken(final String token, final UpdateTokenCallback updateTokenCallback);

    interface FetchFilesCallback {
        void onSuccess(final List<FileData> files);
        void onError(final int error);
    }

    interface Callback {
        void onSuccess();
        void onError(final int error);
    }

    interface IncrementViewsCallback extends Callback { }

    interface FetchDataTypesCallback {
        void onSuccess(final List<String> dataTypes);
        void onError(final int error);
    }

    interface UpdateTokenCallback extends Callback { }
}
