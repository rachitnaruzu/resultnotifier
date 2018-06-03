package com.resultnotifier.main;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class MyAdaptor extends BaseAdapter {

    private final static String TAG = "REN_MyAdaptor";
    private final ArrayList<FileData> mItems;
    private final Activity mainActivity;
    private LayoutInflater mInflater;

    public MyAdaptor(Activity mainActivity) {
        //this.context = context;
        this.mItems = new ArrayList<>();
        this.mainActivity = mainActivity;
        //(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void clear() {
        mItems.clear();
    }

    public void add_items(FileData item) {
        mItems.add(item);
    }

    public ArrayList<FileData> getAdapterItems() {
        return mItems;
    }


    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FileData mItem = (FileData) getItem(position);
        View vi = null;
        //if(!mItem.getInProcess()) vi = convertView;
        if (mInflater == null)
            mInflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (vi == null)
            vi = mInflater.inflate(R.layout.list_item, null);


        ProgressBar progressBar = (ProgressBar) vi.findViewById(R.id.progressbar);
        mItem.setProgressBar(progressBar);
        TextView text = (TextView) vi.findViewById(R.id.text);
        TextView viewsView = (TextView) vi.findViewById(R.id.views);
        TextView views_textView = (TextView) vi.findViewById(R.id.views_text);
        TextView dateView = (TextView) vi.findViewById(R.id.date);
        ImageView checkIcon = (ImageView) vi.findViewById(R.id.checkIcon);
        TextView dataTypeIcon = (TextView) vi.findViewById(R.id.dataTypeIcon);


        text.setText(mItem.getDisplayName());
        viewsView.setText(getPrintableViews(Integer.parseInt(mItem.getViews())));
        String printableDate = getPrintableDate(mItem.getDateCreated());
        dateView.setText(printableDate);
        dataTypeIcon.setText((mItem.getDataType().charAt(0) + "").toUpperCase());
        GradientDrawable bgShape = (GradientDrawable) dataTypeIcon.getBackground();
        bgShape.setColor(mItem.getColor());

        if (mItem.isCompleted()) {
            //progressBar.setProgress(100);
            vi.setBackgroundColor(ContextCompat.getColor(vi.getContext(), R.color.main_list_bg_download_complete));

            text.setTypeface(null, Typeface.NORMAL);
            text.setTextColor(ContextCompat.getColor(vi.getContext(), R.color.main_list_text_color_download_complete));
            views_textView.setTypeface(null, Typeface.NORMAL);
            views_textView.setTextColor(ContextCompat.getColor(vi.getContext(), R.color.main_list_views_text_color_download_complete));
            dateView.setTypeface(null, Typeface.NORMAL);
            dateView.setTextColor(ContextCompat.getColor(vi.getContext(), R.color.main_list_datecreated_color_download_complete));
        } else {
            //progressBar.setProgress(0);
            vi.setBackgroundColor(ContextCompat.getColor(vi.getContext(), R.color.list_background));

            text.setTypeface(null, Typeface.BOLD);
            text.setTextColor(ContextCompat.getColor(vi.getContext(), R.color.main_list_text_color_download_complete));
            views_textView.setTypeface(null, Typeface.BOLD);
            views_textView.setTextColor(ContextCompat.getColor(vi.getContext(), R.color.main_list_views_text_color_download_complete));
            dateView.setTypeface(null, Typeface.BOLD);
            dateView.setTextColor(ContextCompat.getColor(vi.getContext(), R.color.main_list_datecreated_color_download_complete));
        }
        handleCheck(dataTypeIcon, checkIcon, mItem);

        if (!mItem.isInProcess()) {
            progressBar.setVisibility(View.INVISIBLE);
        }
        return vi;
    }

    String getMonthForInt(int num) {
        String month = "wrong";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (num >= 0 && num <= 11) {
            month = months[num];
        }
        return month;
    }

    private String getPrintableDate(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date1;
        try {
            date1 = sdf.parse(dateString);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
            return dateString;
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        cal1.setTimeZone(TimeZone.getDefault());
        Calendar cal2 = Calendar.getInstance();
        cal2.setTimeZone(TimeZone.getDefault());
        if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)) {
            if (cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)) {
                if (cal1.get(Calendar.AM_PM) == Calendar.AM) {
                    return cal1.get(Calendar.HOUR_OF_DAY) + ":" + cal1.get(Calendar.MINUTE) + " AM";
                } else {
                    return (cal1.get(Calendar.HOUR_OF_DAY) - 12) + ":" + cal1.get(Calendar.MINUTE) + " PM";
                }
            } else if (cal1.get(Calendar.DAY_OF_YEAR) + 1 == cal2.get(Calendar.DAY_OF_YEAR)) {
                return "yesterday";
            }
        } else if (cal1.get(Calendar.YEAR) + 1 == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal1.getActualMaximum(Calendar.DAY_OF_YEAR)) {
            return "yesterday";
        }
        return cal1.get(Calendar.DAY_OF_MONTH) + " " + getMonthForInt(cal1.get(Calendar.MONTH)) + ", " + cal1.get(Calendar.YEAR);
    }

    private String getPrintableViews(int views) {
        if (views < 10) return views + "";
        if (views < 1000) return views / 10 + "0+";
        int next2 = (views % 1000) / 10;
        return views / 1000 + "." + next2 + "k";
    }

    private void handleCheck(View dataTypeIcon, View checkIcon, FileData mItem) {
        if (mItem.isSelected()) {
            if (mItem.isDisplaySelected()) {
                //Log.e(TAG, "displaySelected is true!!!");
                dataTypeIcon.setVisibility(View.INVISIBLE);
                checkIcon.setVisibility(View.VISIBLE);
                checkIcon.setAlpha(1);
            } else {
                AnimatorSet card_flip_left_out = (AnimatorSet) AnimatorInflater.loadAnimator(mainActivity, R.animator.card_flip_left_out);
                card_flip_left_out.setTarget(dataTypeIcon);
                card_flip_left_out.start();
                card_flip_left_out.addListener(new SimpleAnimatorListener(dataTypeIcon, View.INVISIBLE));
                //dataTypeIcon.setVisibility(View.INVISIBLE);
                checkIcon.setVisibility(View.VISIBLE);
                checkIcon.setAlpha(1);
                AnimatorSet card_flip_right_in = (AnimatorSet) AnimatorInflater.loadAnimator(mainActivity, R.animator.card_flip_right_in);
                card_flip_right_in.setTarget(checkIcon);
                card_flip_right_in.start();
                mItem.setIsDisplaySelected(true);
            }
        } else {
            if (mItem.isDisplaySelected()) {
                AnimatorSet card_flip_right_out = (AnimatorSet) AnimatorInflater.loadAnimator(mainActivity, R.animator.card_flip_right_out);
                card_flip_right_out.setTarget(checkIcon);
                card_flip_right_out.start();
                card_flip_right_out.addListener(new SimpleAnimatorListener(checkIcon, View.INVISIBLE));
                //checkIcon.setVisibility(View.INVISIBLE);
                dataTypeIcon.setVisibility(View.VISIBLE);
                dataTypeIcon.setAlpha(1);
                AnimatorSet card_flip_left_in = (AnimatorSet) AnimatorInflater.loadAnimator(mainActivity, R.animator.card_flip_left_in);
                card_flip_left_in.setTarget(dataTypeIcon);
                card_flip_left_in.start();
                mItem.setIsDisplaySelected(false);
            } else {
                checkIcon.setVisibility(View.INVISIBLE);
                dataTypeIcon.setVisibility(View.VISIBLE);
                dataTypeIcon.setAlpha(1);
            }
        }
    }

    class SimpleAnimatorListener implements Animator.AnimatorListener {

        View view;
        int visibility;

        public SimpleAnimatorListener(View view, int visibility) {
            super();
            this.view = view;
            this.visibility = visibility;
        }

        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            view.setVisibility(visibility);
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }


}