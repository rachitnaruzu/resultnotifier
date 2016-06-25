package com.resultnotifier.main;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseUtility extends SQLiteOpenHelper {

    final private Context mContext;
    final private static String NAME = CommonUtility.APP_NAME;
    final private static Integer VERSION = 1;
    private static DatabaseUtility dbutil_instance = null;
    //private SQLiteDatabase db;


    final static String TABLE_FILES = "files";
    final static String _ID = "_id";
    final static String DISPLAY_NAME = "displayname";
    final static String FILE_ID = "fileid";
    final static String URL = "url";
    final static String FILE_TYPE = "filetype";
    final static String DATA_TYPE = "datatype";
    final static String DATE_CREATED = "datecreated";
    final static String VIEWS = "views";
    final static String SELF_VIEWS = "selfviews";
    //final static String[] columns = {_ID,DISPLAY_NAME,FILE_ID,URL,FILE_TYPE,DATA_TYPE,DATE_CREATED, VIEWS};

    final private static String CREATE_CMD =
            "CREATE TABLE " + TABLE_FILES  + " ("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DISPLAY_NAME + " TEXT NOT NULL, "
            + FILE_ID + " TEXT NOT NULL, "
            + URL + " TEXT NOT NULL, "
            + FILE_TYPE + " TEXT NOT NULL, "
            + DATA_TYPE + " TEXT NOT NULL, "
            + DATE_CREATED + " TEXT NOT NULL, "
            + VIEWS + " INTEGER, "
            + SELF_VIEWS + " INTEGER"
            + ")";

    final private static String GET_ALL_FILES_CMD =
            "SELECT "
            + DISPLAY_NAME + ","
            + FILE_ID + ","
            + URL + ","
            + FILE_TYPE + ","
            + DATA_TYPE + ","
            + DATE_CREATED + ","
            + VIEWS + ", "
            + SELF_VIEWS + " "
            + "FROM " + TABLE_FILES + " ORDER BY date(" + DATE_CREATED +") DESC, " + DISPLAY_NAME + ";";

    final private static String FILE_PRESENCE_CMD =
            "SELECT " + FILE_ID + " FROM "
                            + TABLE_FILES + " WHERE "
                            + FILE_ID + " = ? LIMIT 1";

    final static String TABLE_DATA_TYPES = "datatypes";
    final static String IS_CHECKED = "ischecked";

    final static String CREATE_TABLE_DATA_TYPES_CMD =
            "CREATE TABLE " + TABLE_DATA_TYPES  + " ("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DATA_TYPE + " TEXT NOT NULL, "
            + IS_CHECKED + " INTEGER"
            + ")";

    final private static String GET_ALL_FILTER_ITEMS_CMD =
            "SELECT "
            + DATA_TYPE + ","
            + IS_CHECKED + " "
            + "FROM " + TABLE_DATA_TYPES;

    final private static int CHECKED = 1;

    final private static String UPDATE_DATA_TYPE_CHECK_CMD =
            "UPDATE " + TABLE_DATA_TYPES + " "
            + "SET " + IS_CHECKED + " = ? "
            + "WHERE " + DATA_TYPE + " = ?";

    final private static String DATA_TYPE_PRESENCE_CMD =
            "SELECT " + DATA_TYPE + " FROM "
                    + TABLE_DATA_TYPES + " WHERE "
                    + DATA_TYPE + " = ? LIMIT 1";

    final private static String GET_ALL_CHECKED_FILTER_ITEMS =
            "SELECT " + DATA_TYPE + " FROM "
                    + TABLE_DATA_TYPES + " WHERE "
                    + IS_CHECKED + " = 1";

    final private static String DELETE_FILE_CMD =
            "DELETE FROM " + TABLE_FILES + " "
            + "WHERE " + FILE_ID + " = ?";

    final private static String UPDATE_VIEWS_CMD =
            "UPDATE " + TABLE_FILES + " "
            + "SET " + VIEWS + " = ?, "
            + SELF_VIEWS + " = ? "
            + "WHERE " + FILE_ID + " = ?";

    final private static String INCREMENT_VIEWS_BY_ONE_CMD =
            "UPDATE " + TABLE_FILES + " "
            + "SET " + VIEWS + " = " + VIEWS + " + 1 "
            + "WHERE " + FILE_ID + " = ?";

    final private static String INCREMENT_SELF_VIEWS_BY_ONE_CMD =
            "UPDATE " + TABLE_FILES + " "
                    + "SET " + SELF_VIEWS + " = " + SELF_VIEWS + " + 1 "
                    + "WHERE " + FILE_ID + " = ?";

    final private static String GET_ALL_FILES_FILTERED_CMD_PREFIX =
            "SELECT "
            + DISPLAY_NAME + ","
            + FILE_ID + ","
            + URL + ","
            + FILE_TYPE + ","
            + DATA_TYPE + ","
            + DATE_CREATED + ","
            + VIEWS + ", "
            + SELF_VIEWS + " "
            + "FROM " + TABLE_FILES + " "
            + "WHERE " + DATA_TYPE + " IN ";

    final private static String GET_ALL_FILES_FILTERED_CMD_SUFFIX =
            " ORDER BY date(" + DATE_CREATED +") DESC, " + DISPLAY_NAME + ";";

    final static String TABLE_SETTINGS = "settings";
    final static String VARIABLE = "variable";
    final static String CREATE_TABLE_SETTINGS_CMD =
            "CREATE TABLE " + TABLE_SETTINGS  + " ("
                    + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + VARIABLE + " TEXT NOT NULL, "
                    + IS_CHECKED + " INTEGER"
                    + ")";

    final static String UPDATE_FILTER_SAVED =
            "UPDATE " + TABLE_SETTINGS + " "
            + "SET " + IS_CHECKED + " = ? "
            + "WHERE " + VARIABLE + " = 'filter_saved';";

    final static String GET_FILTER_SAVED_CMD =
            "SELECT " + VARIABLE + "," + IS_CHECKED + " "
            + "FROM " + TABLE_SETTINGS + " "
            + "WHERE " + VARIABLE + " = 'filter_saved';";

    public boolean getFilerSavedCheck(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(GET_FILTER_SAVED_CMD, null);
        boolean result = false;
        if (cursor.moveToFirst()) {
            result =  cursor.getInt(cursor.getColumnIndex(IS_CHECKED)) == CHECKED;
        }
        //db.close();
        return result;
    }

    public void incrementViewsByOne(String fileid){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(INCREMENT_VIEWS_BY_ONE_CMD, new String[] { fileid } );
        //db.close();
    }

    public void incrementSelfViewsByOne(String fileid){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(INCREMENT_SELF_VIEWS_BY_ONE_CMD, new String[] { fileid } );
        //db.close();
    }

    public void updateViews(FileData fileData){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(UPDATE_VIEWS_CMD, new String[] {fileData.views, "0", fileData.fileid } );
        //db.close();
    }

    public void deleteFile(FileData fileData){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(DELETE_FILE_CMD, new String[] { fileData.fileid });
        //db.close();
    }

    public boolean isDataTypeAvailable(String datatype){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(DATA_TYPE_PRESENCE_CMD, new String[]{datatype});
        boolean result = cursor.moveToFirst();
        //db.close();
        return result;
    }

    public String getCheckedDataTypes(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(GET_ALL_CHECKED_FILTER_ITEMS, null);
        StringBuilder datatypes = new StringBuilder("(");
        if (cursor.moveToFirst()) {
            do {
                String datatype = cursor.getString(cursor.getColumnIndex(DATA_TYPE));
                datatypes.append("'"  + datatype + "'" + ",");
            } while (cursor.moveToNext());
        } else return "()";
        datatypes.replace(datatypes.length() - 1, datatypes.length(), ")");
        //db.close();
        return datatypes.toString();
    }

    public void addDataType(FilterItem filterItem){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        String is_checked = "0";
        if(filterItem.is_checked) is_checked = "1";
        values.put(DATA_TYPE, filterItem.datatype);
        values.put(IS_CHECKED, is_checked);
        db.insert(TABLE_DATA_TYPES, null, values);
        //db.close();
    }

    public DatabaseUtility(Context context) {
        super(context, NAME, null, VERSION);
        this.mContext = context;
    }

    public static DatabaseUtility getInstance(Context context){
        if(dbutil_instance == null)
            dbutil_instance = new DatabaseUtility(context);
        return dbutil_instance;
    }

    public void updateDatatypeCheck(FilterItem filterItem){
        SQLiteDatabase db = this.getWritableDatabase();
        String is_checked = "0";
        if(filterItem.is_checked) is_checked = "1";
        db.execSQL(UPDATE_DATA_TYPE_CHECK_CMD, new String[] {is_checked, filterItem.datatype});
        //db.close();
    }

    public ArrayList<FilterItem> getAllFilterItems() {
        ArrayList<FilterItem> filterItems = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(GET_ALL_FILTER_ITEMS_CMD, null);
        if (cursor.moveToFirst()) {
            do {
                FilterItem filterItem = new FilterItem();
                filterItem.datatype = cursor.getString(cursor.getColumnIndex(DATA_TYPE));
                filterItem.is_checked = cursor.getInt(cursor.getColumnIndex(IS_CHECKED)) == CHECKED;
                filterItems.add(filterItem);
            } while (cursor.moveToNext());
        }
        //db.close();
        return filterItems;
    }


    public void addFileData(FileData filedata) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DISPLAY_NAME, filedata.displayname);
        values.put(FILE_ID, filedata.fileid);
        values.put(URL, filedata.url);
        values.put(FILE_TYPE, filedata.filetype);
        values.put(DATA_TYPE, filedata.datatype);
        values.put(DATE_CREATED, filedata.datecreated);
        values.put(VIEWS, filedata.views);
        db.insert(TABLE_FILES, null, values);
        //db.close();
    }

    public boolean isFilePresent(String fileid){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(FILE_PRESENCE_CMD, new String[]{fileid});
        boolean result = cursor.moveToFirst();
        //db.close();
        return result;
    }

    public ArrayList<FileData> getAllFiles(boolean filter) {
        ArrayList<FileData> mItems =  new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        if(filter){
            String datatypes = getCheckedDataTypes();
            if(datatypes.equals("()")){
                cursor = db.rawQuery(GET_ALL_FILES_CMD, null);
            }
            else
                cursor = db.rawQuery(GET_ALL_FILES_FILTERED_CMD_PREFIX + datatypes + GET_ALL_FILES_FILTERED_CMD_SUFFIX, null);
        }
        else
            cursor = db.rawQuery(GET_ALL_FILES_CMD, null);
        if (cursor.moveToFirst()) {
            do {
                FileData filedata = new FileData();
                filedata.displayname =  cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
                filedata.filetype = cursor.getString(cursor.getColumnIndex(FILE_TYPE));
                filedata.fileid = cursor.getString(cursor.getColumnIndex(FILE_ID));
                filedata.datecreated = cursor.getString(cursor.getColumnIndex(DATE_CREATED));
                filedata.datatype = cursor.getString(cursor.getColumnIndex(DATA_TYPE));
                filedata.url = cursor.getString(cursor.getColumnIndex(URL));
                filedata.views = "" + cursor.getInt(cursor.getColumnIndex(VIEWS));
                filedata.selfViews = "" + cursor.getInt(cursor.getColumnIndex(SELF_VIEWS));
                mItems.add(filedata);
            } while (cursor.moveToNext());
        }
        //db.close();
        return mItems;
    }

    public void updateFilterSaved(boolean checked){
        SQLiteDatabase db = this.getWritableDatabase();
        String is_checked = "0";
        if(checked) is_checked = "1";
        db.execSQL(UPDATE_FILTER_SAVED, new String[] { is_checked });
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

    void deleteDatabase() {
        mContext.deleteDatabase(NAME);
    }
}
