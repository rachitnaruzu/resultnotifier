package com.resultnotifier.main.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.WorkerThread;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.resultnotifier.main.AppState;
import com.resultnotifier.main.db.DatabaseManager;
import com.resultnotifier.main.FileData;
import com.resultnotifier.main.R;
import com.resultnotifier.main.downloader.FileDownloader;
import com.resultnotifier.main.loader.DataLoader;
import com.resultnotifier.main.service.RENServiceClient;
import com.resultnotifier.main.ui.main.selector.MultiFileSelector;
import com.resultnotifier.main.ui.main.selector.MultiFileSelectorImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class MainFragment extends Fragment {
    private static final String TAG = "REN_MainFragment";
    private static final String BACKGROUND_THREAD_NAME = "file_downloader_thread";
    private ListView mListView;
    private MainActivity mMainActivity;
    private MyAdaptor mFilesAdaptor;
    private DatabaseManager mDatabaseManager;
    private Snackbar mSnackBar;
    private ImageView mNoNetworkIcon;
    private TextView mNoNetworkText;
    private TextView mNoContent;
    private View mFragmentView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private MultiFileSelector mMultiFileSelector;
    private int mLastOffsetReceived;
    private boolean mIsFetchingInProgress;
    private RENServiceClient mRenServiceClient;
    private FileDownloader mFileDownloader;
    private Handler mBackgroundHandler;
    private DataLoader mDataLoader;
    private Handler mMainHandler;

    public MainFragment() {
    }

    public void inflateArrayList(final Integer offset) {
        Log.i(TAG, "Fetching files with offset=" + offset);
        mIsFetchingInProgress = true;
        mDataLoader.fetchData(offset, mDatabaseManager.getCheckedDataTypes(),
                new DataLoader.DataLoaderCallback() {
            @Override
            public void onSuccess(final List<FileData> files) {
                Log.i(TAG, "Received files. count=" + files.size());
                mLastOffsetReceived = files.size();

                for (final FileData fileData : files) {
                    mFilesAdaptor.add_items(fileData);
                }

                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mIsFetchingInProgress = false;
                        showPopulatedFragmentState();
                    }
                });

                mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refresh();
                    }
                });
            }

            @Override
            public void onError(final int error) {
                Log.i(TAG, "Unable to fetch files. error=" + error);

                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mIsFetchingInProgress = false;
                        showNoNetworkFragmentState();
                    }
                });

                mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refresh();
                    }
                });
            }
        });
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainActivity = (MainActivity) getActivity();
        mMainHandler = new Handler(mMainActivity.getMainLooper());

        final Context applicationContext = mMainActivity.getApplicationContext();
        mRenServiceClient = AppState.getRenServiceClient(applicationContext);
        mFileDownloader = AppState.getFileDownloader();
        mDataLoader = getDataLoader(applicationContext);

        final HandlerThread handlerThread = new HandlerThread(BACKGROUND_THREAD_NAME);
        handlerThread.start();
        mBackgroundHandler = new Handler(handlerThread.getLooper());

        mDatabaseManager = AppState.getDatabaseManager(mMainActivity.getApplicationContext());
        Log.i(TAG, "Oncreate");
    }

    public abstract DataLoader getDataLoader(final Context context);

    public abstract boolean shouldHonourOffset();

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        Log.i(TAG, "Oncreateview");
        View vi = inflater.inflate(R.layout.result, container, false);
        mFragmentView = vi;
        mSnackBar = Snackbar.make(vi.findViewById(R.id.myCoordinatorLayout),
                R.string.no_network_message, Snackbar.LENGTH_LONG);

        mListView = (ListView) vi.findViewById(R.id.listview);
        mFilesAdaptor = new MyAdaptor(mMainActivity);
        mListView.setAdapter(mFilesAdaptor);

        mMultiFileSelector = new MultiFileSelectorImpl(mMainActivity, mFilesAdaptor,
                new MultiFileSelector.MultiFileSelectorCallback() {
                    @Override
                    public void deleteSelectedFiles() {
                        MainFragment.this.deleteSelectedFiles();
                    }

                    @Override
                    public void deSelect() {
                        MainFragment.this.deSelect();
                        showPopulatedFragmentState();
                    }
                });

        mNoContent = vi.findViewById(R.id.no_content);
        mNoNetworkIcon = vi.findViewById(R.id.no_network_icon);
        mNoNetworkText = vi.findViewById(R.id.no_network_text);

        mSwipeRefreshLayout = vi.findViewById(R.id.swiperefresh);
        showLoading(false);
        showNoNetwork(false);
        showNoContent(false);
        setMultiChoice(mListView);
        MainActivity.setmSnackbar(mSnackBar);

        mRenServiceClient.updateSelfViews(mDatabaseManager.getAllFiles(false));
        setFileClickListeners();
        refresh();

        return vi;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMultiFileSelector.exitActionMode();
        mBackgroundHandler.getLooper().quitSafely();
        mBackgroundHandler = null;
        mMainHandler = null;
    }

    public void refresh() {
        Log.i(TAG, "Refreshing fragment");
        clearFragmentState();
        showLoadingFragmentState();
        inflateArrayList(shouldHonourOffset() ? 0 : null);
    }

    private void clearFragmentState() {
        Log.i(TAG, "Clearing fragment state");
        mLastOffsetReceived = 0;
        mIsFetchingInProgress = false;
        mFilesAdaptor.clear();
        showNoNetwork(false);
        showLoading(false);
        showNoContent(false);
        mFilesAdaptor.notifyDataSetChanged();
    }

    public void showLoadingFragmentState() {
        Log.i(TAG, "Showing loading fragment");
        showNoNetwork(false);
        showLoading(true);
        showNoContent(false);
        mFilesAdaptor.notifyDataSetChanged();
    }

    public void showNoNetworkFragmentState() {
        Log.i(TAG, "Showing no network fragment");
        showLoading(false);
        showNoContent(false);
        mFilesAdaptor.clear();
        mFilesAdaptor.notifyDataSetChanged();
        showNoNetwork(true);
        mSnackBar.setText("No Network");
        mSnackBar.show();
    }

    public void showPopulatedFragmentState() {
        Log.i(TAG, "Showing populated fragment");
        showNoNetwork(false);
        showLoading(false);
        mFilesAdaptor.notifyDataSetChanged();
        showNoContent(mFilesAdaptor.isEmpty());
    }

    public abstract boolean shouldDisplayNonSavedFiles();

    public void deleteSelectedFiles() {
        Log.i(TAG, "Deleting selected files");
        final ArrayList<FileData> files = new ArrayList<>(mFilesAdaptor.getAdapterItems());
        for (final FileData fileData : files) {
            if (fileData.isSelected()) {
                mDatabaseManager.deleteFile(fileData);
            }
        }

        final boolean shouldDisplayNonSavedFiles = shouldDisplayNonSavedFiles();
        if (shouldDisplayNonSavedFiles) {
            markSelectedFilesAsNonSaved(files);
        } else {
            populateNonSelectedFiles(files);
        }
    }

    private void markSelectedFilesAsNonSaved(final List<FileData> files) {
        for (final FileData file : files) {
            if (file.isSelected()) {
               file.setIsCompleted(false);
            }
        }
    }

    private void populateNonSelectedFiles(final List<FileData> files) {
        mFilesAdaptor.clear();
        for (final FileData file : files) {
            if (!file.isSelected()) {
                mFilesAdaptor.add_items(file);
            }
        }
    }

    private void deSelect() {
        Log.i(TAG, "Deselecting files");
        final ArrayList<FileData> files = mFilesAdaptor.getAdapterItems();
        for (final FileData file : files) {
            if (file.isSelected()) {
                file.setIsSelected(false);
            }
        }
    }

    public void incrementViewsByOne(final String fileId) {
        Log.i(TAG, "Requesting to increment views by one. file ID=" + fileId);
        mRenServiceClient.incrementViewsByOne(fileId,
                new RENServiceClient.IncrementViewsCallback() {
                    @Override
                    public void onSuccess() {
                        // no-op
                    }

                    @Override
                    public void onError(final int error) {
                        mDatabaseManager.incrementSelfViewsByOne(fileId);
                    }
                });
    }

    public void openFile(final FileData fileData) {
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File(root.getAbsolutePath() + "/xmls");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, fileData.getFileId() + "." + fileData.getFileType());
        if (!file.exists()) {

            Snackbar snackbar = Snackbar
                    .make(mFragmentView.findViewById(R.id.myCoordinatorLayout),
                            "File does not exists", Snackbar.LENGTH_INDEFINITE)
                    .setAction("REDOWNLOAD", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mBackgroundHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    downloadFile(fileData);
                                }
                            });
                        }
                    });

            snackbar.show();
            return;
        }
        MimeTypeMap myMime = MimeTypeMap.getSingleton();
        String mimeType = myMime.getMimeTypeFromExtension(fileData.getFileType());

        Uri apkURI = FileProvider.getUriForFile(
                mMainActivity, mMainActivity.getPackageName() + ".provider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkURI, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        grantAllUriPermissions(mMainActivity, intent, apkURI);
        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException ex) {
            mSnackBar.setText("No app found to open the file");
            mSnackBar.show();
        }
    }

    private void grantAllUriPermissions(Context context, Intent intent, Uri uri) {
        List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    public void setMultiChoice(ListView lv) {
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lv.setMultiChoiceModeListener(mMultiFileSelector);
    }

    private void setFileClickListeners() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent,
                                    final View view,
                                    final int position,
                                    final long id) {
                final FileData file = (FileData) mFilesAdaptor.getItem(position);
                if (file.isCompleted()) {
                    openFile(file);
                    incrementViewsByOne(file.getFileId());
                    return;
                }

                mBackgroundHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        downloadFile(file);
                    }
                });
            }
        });

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(final AbsListView view,
                                 final int firstVisibleItem,
                                 final int visibleItemCount,
                                 final int totalItemCount) {
                if (shouldHonourOffset() && !mIsFetchingInProgress && totalItemCount > 0
                        && mLastOffsetReceived > visibleItemCount
                        && firstVisibleItem + visibleItemCount == totalItemCount) {
                    inflateArrayList(totalItemCount);
                }
            }
        });
    }

    @WorkerThread
    private void downloadFile(final FileData file) {
        final String fileName = file.getFileId() + ".pdf";
        if (file.isDownloadInProcess()) {
            Log.i(TAG, "File download already in process. file name=" + fileName);
            return;
        }

        file.setDownloadInProcess(true);
        mFileDownloader.downloadFile(file.getUrl(), fileName,
                new FileDownloader.DownloadFileCallback() {
            @Override
            public void onDownloadStart() {
                Log.i(TAG, "File download started. file ID=" + file.getFileId());
                file.setProgress(0);
            }

            @Override
            public void onProgressUpdate(final int progress) {
                Log.i(TAG, "File download progress received. file ID="
                        + file.getFileId() + "; progress=" + progress);
                file.setProgress(progress);
            }

            @Override
            public void onDownloadComplete() {
                Log.i(TAG, "Download complete. file ID=" + file.getFileId());
                file.setProgress(100);
                file.setIsCompleted(true);
                file.setDownloadInProcess(false);
                if (!mDatabaseManager.isFilePresent(file.getFileId())) {
                    mDatabaseManager.addFileData(file);
                }

                incrementViewsByOne(file.getFileId());
                showPopulatedFragmentState();
            }

            @Override
            public void onDownloadFail(final int error) {
                Log.e(TAG, "File download failed. file ID="
                        + file.getFileId() + "; error=" + error);
                MainActivity.getmSnackbar().setText("File Download Error");
                MainActivity.getmSnackbar().show();
                file.setProgress(0);
                file.setDownloadInProcess(false);
                mFilesAdaptor.notifyDataSetChanged();
            }
        });
    }

    public void showLoading(final boolean show) {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(show);
            }
        });
    }

    public void showNoNetwork(boolean show) {
        if (show) {
            mNoNetworkIcon.setVisibility(View.VISIBLE);
            mNoNetworkText.setVisibility(View.VISIBLE);
        } else {
            mNoNetworkIcon.setVisibility(View.INVISIBLE);
            mNoNetworkText.setVisibility(View.INVISIBLE);
        }
    }

    public void showNoContent(boolean show) {
        if (show) {
            mNoContent.setVisibility(View.VISIBLE);
        } else {
            mNoContent.setVisibility(View.INVISIBLE);
        }
    }
}