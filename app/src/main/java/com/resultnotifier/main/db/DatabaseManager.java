package com.resultnotifier.main.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.resultnotifier.main.Config;
import com.resultnotifier.main.FileData;
import com.resultnotifier.main.ui.filter.FilterItem;

import java.util.ArrayList;

public class DatabaseManager extends SQLiteOpenHelper {
    private static final String TAG = "REN_DatabaseManager";

    private static final String NAME = Config.APP_NAME;
    private static final int VERSION = 1;
    private static final int CHECKED = 1;

    private static final String TABLE_FILES = "files";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_DISPLAY_NAME = "displayname";
    private static final String COLUMN_FILE_ID = "fileid";
    private static final String COLUMN_URL = "url";
    private static final String COLUMN_FILE_TYPE = "filetype";
    private static final String COLUMN_DATA_TYPE = "datatype";
    private static final String COLUMN_DATE_CREATED = "datecreated";
    private static final String COLUMN_VIEWS = "views";
    private static final String COLUMN_SELF_VIEWS = "selfviews";
    private static final String TABLE_DATA_TYPES = "datatypes";
    private static final String IS_CHECKED = "ischecked";

    private static final String CREATE_CMD =
            "CREATE TABLE " + TABLE_FILES + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_DISPLAY_NAME + " TEXT NOT NULL, "
                    + COLUMN_FILE_ID + " TEXT NOT NULL, "
                    + COLUMN_URL + " TEXT NOT NULL, "
                    + COLUMN_FILE_TYPE + " TEXT NOT NULL, "
                    + COLUMN_DATA_TYPE + " TEXT NOT NULL, "
                    + COLUMN_DATE_CREATED + " TEXT NOT NULL, "
                    + COLUMN_VIEWS + " INTEGER, "
                    + COLUMN_SELF_VIEWS + " INTEGER"
                    + ")";

    private static final String GET_ALL_FILES_CMD =
            "SELECT "
                    + COLUMN_DISPLAY_NAME + ","
                    + COLUMN_FILE_ID + ","
                    + COLUMN_URL + ","
                    + COLUMN_FILE_TYPE + ","
                    + COLUMN_DATA_TYPE + ","
                    + COLUMN_DATE_CREATED + ","
                    + COLUMN_VIEWS + ", "
                    + COLUMN_SELF_VIEWS + " "
                    + "FROM " + TABLE_FILES + " ORDER BY date(" + COLUMN_DATE_CREATED + ") DESC, " + COLUMN_DISPLAY_NAME + ";";

    private static final String FILE_PRESENCE_CMD =
            "SELECT " + COLUMN_FILE_ID + " FROM "
                    + TABLE_FILES + " WHERE "
                    + COLUMN_FILE_ID + " = ? LIMIT 1";

    private static final String CREATE_TABLE_DATA_TYPES_CMD =
            "CREATE TABLE " + TABLE_DATA_TYPES + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_DATA_TYPE + " TEXT NOT NULL, "
                    + IS_CHECKED + " INTEGER"
                    + ")";

    private static final String GET_ALL_FILTER_ITEMS_CMD =
            "SELECT " + COLUMN_DATA_TYPE + ","
                    + IS_CHECKED + " "
                    + "FROM " + TABLE_DATA_TYPES;

    private static final String UPDATE_DATA_TYPE_CHECK_CMD =
            "UPDATE " + TABLE_DATA_TYPES + " "
                    + "SET " + IS_CHECKED + " = ? "
                    + "WHERE " + COLUMN_DATA_TYPE + " = ?";

    private static final String DATA_TYPE_PRESENCE_CMD =
            "SELECT " + COLUMN_DATA_TYPE + " FROM "
                    + TABLE_DATA_TYPES + " WHERE "
                    + COLUMN_DATA_TYPE + " = ? LIMIT 1";

    private static final String GET_ALL_CHECKED_FILTER_ITEMS =
            "SELECT " + COLUMN_DATA_TYPE + " FROM "
                    + TABLE_DATA_TYPES + " WHERE "
                    + IS_CHECKED + " = 1";

    private static final String DELETE_FILE_CMD =
            "DELETE FROM " + TABLE_FILES + " "
                    + "WHERE " + COLUMN_FILE_ID + " = ?";

    private static final String UPDATE_VIEWS_CMD =
            "UPDATE " + TABLE_FILES + " "
                    + "SET " + COLUMN_VIEWS + " = ?, "
                    + COLUMN_SELF_VIEWS + " = ? "
                    + "WHERE " + COLUMN_FILE_ID + " = ?";

    private static final String INCREMENT_VIEWS_BY_ONE_CMD =
            "UPDATE " + TABLE_FILES + " "
                    + "SET " + COLUMN_VIEWS + " = " + COLUMN_VIEWS + " + 1 "
                    + "WHERE " + COLUMN_FILE_ID + " = ?";

    private static final String INCREMENT_SELF_VIEWS_BY_ONE_CMD =
            "UPDATE " + TABLE_FILES + " "
                    + "SET " + COLUMN_SELF_VIEWS + " = " + COLUMN_SELF_VIEWS + " + 1 "
                    + "WHERE " + COLUMN_FILE_ID + " = ?";

    private static final String GET_ALL_FILES_FILTERED_CMD_PREFIX =
            "SELECT "
                    + COLUMN_DISPLAY_NAME + ","
                    + COLUMN_FILE_ID + ","
                    + COLUMN_URL + ","
                    + COLUMN_FILE_TYPE + ","
                    + COLUMN_DATA_TYPE + ","
                    + COLUMN_DATE_CREATED + ","
                    + COLUMN_VIEWS + ", "
                    + COLUMN_SELF_VIEWS + " "
                    + "FROM " + TABLE_FILES + " "
                    + "WHERE " + COLUMN_DATA_TYPE + " IN ";

    private static final String GET_ALL_FILES_FILTERED_CMD_SUFFIX =
            " ORDER BY date(" + COLUMN_DATE_CREATED + ") DESC, " + COLUMN_DISPLAY_NAME + ";";

    private static final String TABLE_SETTINGS = "settings";
    private static final String VARIABLE = "variable";
    private static final String CREATE_TABLE_SETTINGS_CMD =
            "CREATE TABLE " + TABLE_SETTINGS + " ("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + VARIABLE + " TEXT NOT NULL, "
                    + IS_CHECKED + " INTEGER"
                    + ")";

    private static final String UPDATE_FILTER_SAVED =
            "UPDATE " + TABLE_SETTINGS + " "
                    + "SET " + IS_CHECKED + " = ? "
                    + "WHERE " + VARIABLE + " = 'filter_saved';";

    private static final String GET_FILTER_SAVED_CMD =
            "SELECT " + VARIABLE + "," + IS_CHECKED + " "
                    + "FROM " + TABLE_SETTINGS + " "
                    + "WHERE " + VARIABLE + " = 'filter_saved';";

    private final Context mContext;

    public DatabaseManager(final Context context) {
        super(context, NAME, null, VERSION);
        this.mContext = context;
    }

    public boolean getFilterSavedCheck() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(GET_FILTER_SAVED_CMD, null);
        boolean result = false;
        if (cursor.moveToFirst()) {
            result = cursor.getInt(cursor.getColumnIndex(IS_CHECKED)) == CHECKED;
        }
        //db.close();
        return result;
    }

    public void incrementViewsByOne(String fileid) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(INCREMENT_VIEWS_BY_ONE_CMD, new String[]{fileid});
        //db.close();
    }

    public void incrementSelfViewsByOne(String fileid) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(INCREMENT_SELF_VIEWS_BY_ONE_CMD, new String[]{fileid});
        //db.close();
    }

    public void updateViews(FileData fileData) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(UPDATE_VIEWS_CMD, new String[]{fileData.getViews(), "0", fileData.getFileId()});
        //db.close();
    }

    public void deleteFile(FileData fileData) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(DELETE_FILE_CMD, new String[]{fileData.getFileId()});
        //db.close();
    }

    public boolean isDataTypeAvailable(String datatype) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(DATA_TYPE_PRESENCE_CMD, new String[]{datatype});
        boolean result = cursor.moveToFirst();
        //db.close();
        return result;
    }

    public String getCheckedDataTypes() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(GET_ALL_CHECKED_FILTER_ITEMS, null);
        StringBuilder datatypes = new StringBuilder("(");
        if (cursor.moveToFirst()) {
            do {
                String datatype = cursor.getString(cursor.getColumnIndex(COLUMN_DATA_TYPE));
                datatypes.append("'" + datatype + "'" + ",");
            } while (cursor.moveToNext());
        } else return "()";
        datatypes.replace(datatypes.length() - 1, datatypes.length(), ")");
        //db.close();
        return datatypes.toString();
    }

    public void addDataType(FilterItem filterItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        String is_checked = "0";
        if (filterItem.isChecked()) is_checked = "1";
        values.put(COLUMN_DATA_TYPE, filterItem.getDataType());
        values.put(IS_CHECKED, is_checked);
        db.insert(TABLE_DATA_TYPES, null, values);
        //db.close();
    }

    public void updateDatatypeCheck(FilterItem filterItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        String is_checked = "0";
        if (filterItem.isChecked()) is_checked = "1";
        db.execSQL(UPDATE_DATA_TYPE_CHECK_CMD, new String[]{is_checked, filterItem.getDataType()});
    }

    public ArrayList<FilterItem> getAllFilterItems() {
        ArrayList<FilterItem> filterItems = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(GET_ALL_FILTER_ITEMS_CMD, null);
        if (cursor.moveToFirst()) {
            do {
                final String dataType = cursor.getString(cursor.getColumnIndex(COLUMN_DATA_TYPE));
                final boolean isChecked =
                        cursor.getInt(cursor.getColumnIndex(IS_CHECKED)) == CHECKED;
                final FilterItem filterItem = new FilterItem(dataType, isChecked);
                filterItems.add(filterItem);
            } while (cursor.moveToNext());
        }
        //db.close();
        Log.i(TAG, "Returning filterItems:" + filterItems);
        return filterItems;
    }


    public void addFileData(FileData filedata) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DISPLAY_NAME, filedata.getDisplayName());
        values.put(COLUMN_FILE_ID, filedata.getFileId());
        values.put(COLUMN_URL, filedata.getUrl());
        values.put(COLUMN_FILE_TYPE, filedata.getFileType());
        values.put(COLUMN_DATA_TYPE, filedata.getDataType());
        values.put(COLUMN_DATE_CREATED, filedata.getDateCreated());
        values.put(COLUMN_VIEWS, filedata.getViews());
        db.insert(TABLE_FILES, null, values);
        //db.close();
    }

    public boolean isFilePresent(String fileid) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(FILE_PRESENCE_CMD, new String[]{fileid});
        boolean result = cursor.moveToFirst();
        //db.close();
        return result;
    }

    public ArrayList<FileData> getAllFiles(boolean filter) {
        ArrayList<FileData> mItems = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        if (filter) {
            String datatypes = getCheckedDataTypes();
            if (datatypes.equals("()")) {
                cursor = db.rawQuery(GET_ALL_FILES_CMD, null);
            } else
                cursor = db.rawQuery(GET_ALL_FILES_FILTERED_CMD_PREFIX + datatypes + GET_ALL_FILES_FILTERED_CMD_SUFFIX, null);
        } else
            cursor = db.rawQuery(GET_ALL_FILES_CMD, null);
        if (cursor.moveToFirst()) {
            do {
                FileData filedata = new FileData();
                filedata.setDisplayName(cursor.getString(cursor.getColumnIndex(COLUMN_DISPLAY_NAME)));
                filedata.setFileType(cursor.getString(cursor.getColumnIndex(COLUMN_FILE_TYPE)));
                filedata.setFileId(cursor.getString(cursor.getColumnIndex(COLUMN_FILE_ID)));
                filedata.setDateCreated(cursor.getString(cursor.getColumnIndex(COLUMN_DATE_CREATED)));
                filedata.setDataType(cursor.getString(cursor.getColumnIndex(COLUMN_DATA_TYPE)));
                filedata.setUrl(cursor.getString(cursor.getColumnIndex(COLUMN_URL)));
                filedata.setViews("" + cursor.getInt(cursor.getColumnIndex(COLUMN_VIEWS)));
                filedata.setSelfViews("" + cursor.getInt(cursor.getColumnIndex(COLUMN_SELF_VIEWS)));
                mItems.add(filedata);
            } while (cursor.moveToNext());
        }
        //db.close();
        return mItems;
    }

    public void updateFilterSaved(boolean checked) {
        SQLiteDatabase db = this.getWritableDatabase();
        String is_checked = "0";
        if (checked) is_checked = "1";
        db.execSQL(UPDATE_FILTER_SAVED, new String[]{is_checked});
        //db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_CMD);
        db.execSQL(CREATE_TABLE_DATA_TYPES_CMD);
        db.execSQL(CREATE_TABLE_SETTINGS_CMD);
        ContentValues values = new ContentValues();
        values.put(VARIABLE, "filter_saved");
        values.put(IS_CHECKED, "0");
        db.insert(TABLE_SETTINGS, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //NA
    }

    public void deleteDatabase() {
        mContext.deleteDatabase(NAME);
    }
}
