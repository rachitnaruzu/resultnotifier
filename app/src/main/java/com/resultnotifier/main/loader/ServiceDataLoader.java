package com.resultnotifier.main.loader;

import com.resultnotifier.main.DatabaseUtility;
import com.resultnotifier.main.FileData;
import com.resultnotifier.main.service.RENServiceClient;

import java.util.List;

abstract class ServiceDataLoader implements DataLoader {
    public RENServiceClient.FetchFilesCallback getFetchFilesCallback(
            final DataLoader.DataLoaderCallback dataLoaderCallback,
            final DatabaseUtility databaseUtility) {
        return new RENServiceClient.FetchFilesCallback() {
            @Override
            public void onSuccess(final List<FileData> files) {
                updateFiles(files, databaseUtility);
                dataLoaderCallback.onSuccess(files);
            }

            @Override
            public void onError(final int error) {
                dataLoaderCallback.onError(error);
            }
        };
    }

    private void updateFiles(final List<FileData> files, final DatabaseUtility databaseUtility) {
        for (final FileData fileData : files) {
            fileData.setIsCompleted(databaseUtility.isFilePresent(fileData.getFileId()));
            if (fileData.isCompleted()) {
                databaseUtility.updateViews(fileData);
            }
        }
    }
}
