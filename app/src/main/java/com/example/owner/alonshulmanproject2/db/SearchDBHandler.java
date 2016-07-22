package com.example.owner.alonshulmanproject2.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.example.owner.alonshulmanproject2.model.Place;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

/**
 * Created by Owner on 29/03/2016.
 */
public class SearchDBHandler {
    private Context context;

    public SearchDBHandler(Context context) {
        this.context = context;
    }

    public Cursor getAllSearchResultsWithDistances(){
        /** returns a cursor with the places and distances in it */
        return context.getContentResolver().query(DBProvider.Search.CONTENT_URI,null,null,null,null);
    }

    public ArrayList<Place> getAllSearchResultsNoDistances() {
        /** returns just the places */
        Cursor c = context.getContentResolver().query(DBProvider.Search.CONTENT_URI,null,null,null,null);
        ArrayList<Place> places = new ArrayList<>();
        while (c != null && c.moveToNext()) {
            String factualId = c.getString(c.getColumnIndex(DBProvider.Search.FACTUAL_ID_COLUMN));
            String name = c.getString(c.getColumnIndex(DBProvider.Search.NAME_COLUMN));
            String address = c.getString(c.getColumnIndex(DBProvider.Search.ADDRESS_COLUMN));
            String locality = c.getString(c.getColumnIndex(DBProvider.Search.LOCALITY_COLUMN));
            String categoryId = c.getString(c.getColumnIndex(DBProvider.Search.CATEGORY_ID_COLUMN));
            LatLng latLng = new LatLng(c.getDouble(c.getColumnIndex(DBProvider.Search.LAT_COLUMN)),
                    c.getDouble(c.getColumnIndex(DBProvider.Search.LNG_COLUMN)));
            String phone = c.getString(c.getColumnIndex(DBProvider.Search.PHONE_COLUMN));
            String website = c.getString(c.getColumnIndex(DBProvider.Search.WEBSITE_COLUMN));
            places.add(new Place(factualId,name,address,locality,categoryId,latLng,phone,website));
        }
        if(c != null){
            c.close();
        }
        return places;
    }

    public void deleteSearchDistances(){
        /** delete just the distances from the table, as they are obsolete when the search is old */
        ContentValues values = new ContentValues();
        values.put(DBProvider.Search.DISTANCE_COLUMN,"");
        context.getContentResolver().update(DBProvider.Search.CONTENT_URI,values,null,null);
    }

    public void deleteAllSearchResults(){
        /** delete all search results, just before a new one enters */
        context.getContentResolver().delete(DBProvider.Search.CONTENT_URI,null,null);
    }

    public void addResults(ArrayList<Place> places, ArrayList<Double> distances){
        /** add the new search results into the table */
        for (int i = 0; i < places.size(); i++) {
            ContentValues values = new ContentValues();
            Place place = places.get(i);
            values.put(DBProvider.Search.FACTUAL_ID_COLUMN,place.getFactualId());
            values.put(DBProvider.Search.NAME_COLUMN,place.getName());
            values.put(DBProvider.Search.ADDRESS_COLUMN,place.getAddress());
            values.put(DBProvider.Search.LOCALITY_COLUMN,place.getLocality());
            values.put(DBProvider.Search.CATEGORY_ID_COLUMN,place.getCategoryId());
            values.put(DBProvider.Search.LAT_COLUMN,place.getLatLng().latitude);
            values.put(DBProvider.Search.LNG_COLUMN,place.getLatLng().longitude);
            values.put(DBProvider.Search.PHONE_COLUMN,place.getPhone());
            values.put(DBProvider.Search.WEBSITE_COLUMN,place.getWebsite());
            values.put(DBProvider.Search.DISTANCE_COLUMN,distances.get(i));
            context.getContentResolver().insert(DBProvider.Search.CONTENT_URI,values);
        }
    }

    public Place getPlace(String factualId){
        /** returns the specific place */
        Place place = null;
        String where = DBProvider.Search.FACTUAL_ID_COLUMN+" = ?";
        String[] whereArgs = {factualId};
        Cursor c = context.getContentResolver().query(DBProvider.Search.CONTENT_URI,null,where,whereArgs,null);
        while(c != null && c.moveToNext()){
            String name = c.getString(c.getColumnIndex(DBProvider.Search.NAME_COLUMN));
            String address = c.getString(c.getColumnIndex(DBProvider.Search.ADDRESS_COLUMN));
            String locality = c.getString(c.getColumnIndex(DBProvider.Search.LOCALITY_COLUMN));
            String categoryId = c.getString(c.getColumnIndex(DBProvider.Search.CATEGORY_ID_COLUMN));
            LatLng latLng = new LatLng(c.getDouble(c.getColumnIndex(DBProvider.Search.LAT_COLUMN)),
                    c.getDouble(c.getColumnIndex(DBProvider.Search.LNG_COLUMN)));
            String phone = c.getString(c.getColumnIndex(DBProvider.Search.PHONE_COLUMN));
            String website = c.getString(c.getColumnIndex(DBProvider.Search.WEBSITE_COLUMN));
            place =  new Place(factualId,name,address,locality,categoryId,latLng,phone,website);
        }
        if(c!= null){
            c.close();
        }
        return place;
    }
}
