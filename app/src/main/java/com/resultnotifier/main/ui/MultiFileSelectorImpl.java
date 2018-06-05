package com.resultnotifier.main.ui;

import android.content.Context;
import android.content.Intent;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.resultnotifier.main.FileData;
import com.resultnotifier.main.MyAdaptor;
import com.resultnotifier.main.R;

import java.util.ArrayList;

public class MultiFileSelectorImpl implements MultiFileSelector {

    private final MyAdaptor mFilesAdaptor;
    private final Context mContext;
    private final MultiFileSelectorCallback mMultiFileSelectorCallback;

    private ActionMode mGlobalMode;
    private TextView mFeedUpdateCount;
    private ImageView mMenuItemDelete;
    private int mCompletedCount;
    private int mIncompleteCount;

    public MultiFileSelectorImpl(final Context context, final MyAdaptor filesAdaptor,
                                 final MultiFileSelectorCallback multiFileSelectorCallback) {
        mContext = context;
        mFilesAdaptor = filesAdaptor;
        mMultiFileSelectorCallback = multiFileSelectorCallback;
    }

    @Override
    public void exitActionMode() {
        if (mGlobalMode != null) {
            mGlobalMode.finish();
        }

        mCompletedCount = mIncompleteCount = 0;
    }

    @Override
    public void onItemCheckedStateChanged(final ActionMode mode, final int position, final long id,
                                          final boolean checked) {
        final FileData fileData = (FileData) mFilesAdaptor.getItem(position);
        if (fileData.isCompleted()) {
            mCompletedCount += checked ? 1 : -1;
        } else {
            mIncompleteCount += checked ? 1 : -1;
        }

        fileData.setIsSelected(checked);
        mFilesAdaptor.notifyDataSetChanged();
        mFeedUpdateCount.setText(String.valueOf(mCompletedCount + mIncompleteCount));
        if (mIncompleteCount > 0) {
            mMenuItemDelete.setVisibility(View.INVISIBLE);
        } else {
            mMenuItemDelete.setVisibility(View.VISIBLE);
        }
    }

    private String getPresentableSelectedString() {
        final ArrayList<FileData> mItems = mFilesAdaptor.getAdapterItems();
        String msg = "";
        for (FileData fileData : mItems) {
            if (fileData.isSelected()) {
                msg += fileData.getDisplayName() + "\n" + fileData.getUrl() + "\n\n";
            }
        }

        return msg;
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
        mCompletedCount = mIncompleteCount = 0;
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);

        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Animation translation = AnimationUtils.loadAnimation(mContext, R.anim.enter_from_right);
        this.mGlobalMode = mode;

        mMenuItemDelete = (ImageView) layoutInflater.inflate(R.layout.action_bar_item, null);
        mMenuItemDelete.setImageResource(R.drawable.ic_delete_white_18pt_3x);
        MenuItem deleteItem = menu.findItem(R.id.delete);
        deleteItem.setActionView(mMenuItemDelete);

        mMenuItemDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMultiFileSelectorCallback.deleteSelectedFiles();
                exitActionMode();
            }
        });

        ImageView menuItemShare = (ImageView) layoutInflater.inflate(R.layout.action_bar_item,
                null);
        menuItemShare.setImageResource(R.drawable.ic_share_white_18pt_3x);
        MenuItem shareItem = menu.findItem(R.id.share);
        shareItem.setActionView(menuItemShare);
        menuItemShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getPresentableSelectedString());
                sendIntent.setType("text/plain");
                mContext.startActivity(Intent.createChooser(sendIntent, "Share With"));
                exitActionMode();
            }
        });

        mFeedUpdateCount = (TextView) layoutInflater.inflate(R.layout.feed_update_count,
                null);
        mFeedUpdateCount.setText(String.valueOf(mCompletedCount + mIncompleteCount));
        MenuItem selectCountItem = menu.findItem(R.id.selectCount);
        selectCountItem.setActionView(mFeedUpdateCount);

        mMenuItemDelete.startAnimation(translation);
        mFeedUpdateCount.startAnimation(translation);
        menuItemShare.startAnimation(translation);

        return true;
    }

    @Override
    public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                mMultiFileSelectorCallback.deleteSelectedFiles();
                mGlobalMode = mode;
                exitActionMode();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(final ActionMode mode) {
        mMultiFileSelectorCallback.deSelect();
        exitActionMode();
    }
}
