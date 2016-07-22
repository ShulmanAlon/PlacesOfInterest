package com.example.owner.alonshulmanproject2.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Owner on 29/03/2016.
 */
public class PlacesDBHelper extends SQLiteOpenHelper{

    public PlacesDBHelper(Context context) {
        super(context, "places.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /** create the two tables, the search table also has the distance column */
        String sql = String.format("CREATE TABLE %1$s(%2$s TEXT PRIMARY KEY, %3$s TEXT, %4$s TEXT, %5$s TEXT, %6$s TEXT, %7$s REAL, %8$s REAL, %9$s TEXT, %10$s TEXT, %11$s REAL)",
                DBProvider.Search.TABLE_NAME, DBProvider.Search.FACTUAL_ID_COLUMN, DBProvider.Search.NAME_COLUMN, DBProvider.Search.ADDRESS_COLUMN,
                DBProvider.Search.LOCALITY_COLUMN, DBProvider.Search.CATEGORY_ID_COLUMN, DBProvider.Search.LAT_COLUMN, DBProvider.Search.LNG_COLUMN,
                DBProvider.Search.PHONE_COLUMN, DBProvider.Search.WEBSITE_COLUMN, DBProvider.Search.DISTANCE_COLUMN);
        db.execSQL(sql);
        sql = String.format("CREATE TABLE %1$s(%2$s TEXT PRIMARY KEY, %3$s TEXT, %4$s TEXT, %5$s TEXT, %6$s TEXT, %7$s REAL, %8$s REAL, %9$s TEXT, %10$s TEXT)",
                DBProvider.Favorites.TABLE_NAME, DBProvider.Favorites.FACTUAL_ID_COLUMN, DBProvider.Favorites.NAME_COLUMN, DBProvider.Favorites.ADDRESS_COLUMN,
                DBProvider.Favorites.LOCALITY_COLUMN, DBProvider.Favorites.CATEGORY_ID_COLUMN, DBProvider.Favorites.LAT_COLUMN, DBProvider.Favorites.LNG_COLUMN,
                DBProvider.Favorites.PHONE_COLUMN, DBProvider.Favorites.WEBSITE_COLUMN);
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}
