package com.example.owner.alonshulmanproject2.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.example.owner.alonshulmanproject2.model.Place;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

/**
 * Created by Owner on 30/03/2016.
 */
public class FavoritesDBHandler {
    private Context context;

    public FavoritesDBHandler(Context context){
        this.context = context;
    }

    public ArrayList<Place> getAllFavorites(){
        /** returns an array list of all favorites */
        Cursor c = context.getContentResolver().query(DBProvider.Favorites.CONTENT_URI,null,null,null,null);
        ArrayList<Place> places = new ArrayList<>();
        while (c!= null && c.moveToNext()){
            String factualId = c.getString(c.getColumnIndex(DBProvider.Favorites.FACTUAL_ID_COLUMN));
            String name = c.getString(c.getColumnIndex(DBProvider.Favorites.NAME_COLUMN));
            String address = c.getString(c.getColumnIndex(DBProvider.Favorites.ADDRESS_COLUMN));
            String locality = c.getString(c.getColumnIndex(DBProvider.Favorites.LOCALITY_COLUMN));
            String categoryId = c.getString(c.getColumnIndex(DBProvider.Favorites.CATEGORY_ID_COLUMN));
            LatLng latLng = new LatLng(c.getDouble(c.getColumnIndex(DBProvider.Favorites.LAT_COLUMN)),
                    c.getDouble(c.getColumnIndex(DBProvider.Favorites.LNG_COLUMN)));
            String phone = c.getString(c.getColumnIndex(DBProvider.Favorites.PHONE_COLUMN));
            String website = c.getString(c.getColumnIndex(DBProvider.Favorites.WEBSITE_COLUMN));
            places.add(new Place(factualId,name,address,locality,categoryId,latLng,phone,website));
        }
        if(c!= null){
            c.close();
        }
        return places;
    }

    public void deleteAllFavorites(){
        /** deletes all favorites */
        context.getContentResolver().delete(DBProvider.Favorites.CONTENT_URI,null,null);
    }

    public void addFavorite(Place place){
        /** add a new place to favorites */
        ContentValues values = new ContentValues();
        values.put(DBProvider.Favorites.FACTUAL_ID_COLUMN,place.getFactualId());
        values.put(DBProvider.Favorites.NAME_COLUMN,place.getName());
        values.put(DBProvider.Favorites.ADDRESS_COLUMN,place.getAddress());
        values.put(DBProvider.Favorites.LOCALITY_COLUMN,place.getLocality());
        values.put(DBProvider.Favorites.CATEGORY_ID_COLUMN,place.getCategoryId());
        values.put(DBProvider.Favorites.LAT_COLUMN,place.getLatLng().latitude);
        values.put(DBProvider.Favorites.LNG_COLUMN,place.getLatLng().longitude);
        values.put(DBProvider.Favorites.PHONE_COLUMN,place.getPhone());
        values.put(DBProvider.Favorites.WEBSITE_COLUMN,place.getWebsite());
        context.getContentResolver().insert(DBProvider.Favorites.CONTENT_URI,values);
    }

    public void deleteFavorite(String factualId){
        /** delete a specific place */
        String where = String.format("%1$s = ?",
                DBProvider.Favorites.FACTUAL_ID_COLUMN);
        String[] whereArgs = {factualId};
        context.getContentResolver().delete(DBProvider.Favorites.CONTENT_URI,where,whereArgs);

    }

    public boolean checkFavoriteInDb(String factualId){
        /** returns true/ false if the factualId is in the table */
        String[] projection = {DBProvider.Favorites.FACTUAL_ID_COLUMN};
        String where = DBProvider.Favorites.FACTUAL_ID_COLUMN +"= ?";
        String[] whereArgs = {factualId};
        Cursor cursor = context.getContentResolver().query(DBProvider.Favorites.CONTENT_URI,projection,where,whereArgs,null);
        if(cursor != null && cursor.moveToFirst()) {
            cursor.close();
            return true;
        }
        return false;
    }

    public Place getPlace(String factualId) {
        /** returns a specific place from table */
        Place place = null;
        String where = DBProvider.Favorites.FACTUAL_ID_COLUMN +"= ?";
        String[] whereArgs = {factualId};
        Cursor c = context.getContentResolver().query(DBProvider.Favorites.CONTENT_URI,null,where, whereArgs, null);
        if (c != null && c.moveToNext()) {
            String name = c.getString(c.getColumnIndex(DBProvider.Favorites.NAME_COLUMN));
            String address = c.getString(c.getColumnIndex(DBProvider.Favorites.ADDRESS_COLUMN));
            String locality = c.getString(c.getColumnIndex(DBProvider.Favorites.LOCALITY_COLUMN));
            String categoryId = c.getString(c.getColumnIndex(DBProvider.Favorites.CATEGORY_ID_COLUMN));
            LatLng latLng = new LatLng(c.getDouble(c.getColumnIndex(DBProvider.Favorites.LAT_COLUMN)),
                    c.getDouble(c.getColumnIndex(DBProvider.Favorites.LNG_COLUMN)));
            String phone = c.getString(c.getColumnIndex(DBProvider.Favorites.PHONE_COLUMN));
            String website = c.getString(c.getColumnIndex(DBProvider.Favorites.WEBSITE_COLUMN));
            place = new Place(factualId,name,address,locality,categoryId,latLng,phone,website);
        }
        if(c != null){
            c.close();
        }
        return place;
    }
}
