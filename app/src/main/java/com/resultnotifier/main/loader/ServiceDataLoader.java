package com.resultnotifier.main.loader;

import com.resultnotifier.main.db.DatabaseManager;
import com.resultnotifier.main.FileData;
import com.resultnotifier.main.service.RENServiceClient;

import java.util.List;

abstract class ServiceDataLoader implements DataLoader {
    public RENServiceClient.FetchFilesCallback getFetchFilesCallback(
            final DataLoader.DataLoaderCallback dataLoaderCallback,
            final DatabaseManager databaseManager) {
        return new RENServiceClient.FetchFilesCallback() {
            @Override
            public void onSuccess(final List<FileData> files) {
                updateFiles(files, databaseManager);
                dataLoaderCallback.onSuccess(files);
            }

            @Override
            public void onError(final int error) {
                dataLoaderCallback.onError(error);
            }
        };
    }

    private void updateFiles(final List<FileData> files, final DatabaseManager databaseManager) {
        for (final FileData fileData : files) {
            fileData.setIsCompleted(databaseManager.isFilePresent(fileData.getFileId()));
            if (fileData.isCompleted()) {
                databaseManager.updateViews(fileData);
            }
        }
    }
}
