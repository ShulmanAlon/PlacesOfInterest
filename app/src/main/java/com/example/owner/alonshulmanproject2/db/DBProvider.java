package com.example.owner.alonshulmanproject2.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by Owner on 30/05/2016.
 */
public class DBProvider extends ContentProvider {
    public static final String AUTHORITY = "com.example.owner.alonshulmanproject2.db.DBProvider";
    PlacesDBHelper helper;

    public static class Search{
        public static final String TABLE_NAME = "search";
        public static final String FACTUAL_ID_COLUMN = "factualId";
        public static final String NAME_COLUMN = "name";
        public static final String ADDRESS_COLUMN = "address";
        public static final String LOCALITY_COLUMN = "locality";
        public static final String CATEGORY_ID_COLUMN = "categoryId";
        public static final String LAT_COLUMN = "lat";
        public static final String LNG_COLUMN = "lng";
        public static final String PHONE_COLUMN = "phone";
        public static final String WEBSITE_COLUMN = "website";
        public static final String DISTANCE_COLUMN = "distance";
        public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+ TABLE_NAME);
    }

    public static class Favorites{
        public static final String TABLE_NAME = "favorites";
        public static final String FACTUAL_ID_COLUMN = "factualId";
        public static final String NAME_COLUMN = "name";
        public static final String ADDRESS_COLUMN = "address";
        public static final String LOCALITY_COLUMN = "locality";
        public static final String CATEGORY_ID_COLUMN = "categoryId";
        public static final String LAT_COLUMN = "lat";
        public static final String LNG_COLUMN = "lng";
        public static final String PHONE_COLUMN = "phone";
        public static final String WEBSITE_COLUMN = "website";
        public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+ TABLE_NAME);
    }

    @Override
    public boolean onCreate() {
        helper = new PlacesDBHelper(getContext());
        if(helper != null){
            return true;
        }
        return false;
    }

    public String getTableName(Uri uri){
        List<String> pathSegment = uri.getPathSegments();
        return pathSegment.get(0);
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(getTableName(uri),projection,selection,selectionArgs,null,null,sortOrder);
        return c;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long id = db.insert(getTableName(uri),null,values);
        if(id != -1){
            return ContentUris.withAppendedId(uri,id);
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int count = db.delete(getTableName(uri),selection,selectionArgs);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int count = db.update(getTableName(uri),values,selection,selectionArgs);
        return count;
    }
}
