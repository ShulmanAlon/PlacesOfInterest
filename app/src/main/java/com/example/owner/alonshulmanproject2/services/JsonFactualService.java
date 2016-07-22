package com.example.owner.alonshulmanproject2.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.example.owner.alonshulmanproject2.R;
import com.example.owner.alonshulmanproject2.activities.MainActivity;
import com.example.owner.alonshulmanproject2.db.SearchDBHandler;
import com.example.owner.alonshulmanproject2.model.Place;
import com.example.owner.alonshulmanproject2.static_methods.StaticMethods;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class JsonFactualService extends IntentService {

    public static final String ACTION_SERVICE_FACTUAL_RECEIVE = "com.example.owner.alonshulmanproject2.services.ACTION_SERVICE_FACTUAL_RECEIVE";

    public JsonFactualService() {
        super("JsonFactualService");
    }

    /** intent service to get the search results from factual API, based on distance from the user or both distance and other filters
     * the search will continue even if the app is closed, and the search will be presented albeit without the distances(since it will no longer be relevant) */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            /** default radius and category are 2500 meters and all categories */
            String query = intent.getStringExtra(MainActivity.QUERY);
            int radius = intent.getIntExtra(MainActivity.RADIUS, 2500);
            int category = intent.getIntExtra(MainActivity.CATEGORY,-1);
            String nameFilter;
            String categoryFilter;
            /** prepare the query based on whether it exists or not */
            if(query == null || query.equals("")){
                nameFilter = "";
            } else{
                try {
                    nameFilter = String.format("q=%1$s&",
                            URLEncoder.encode(query, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    Log.d("JsonFactualService", e.getMessage());
                    return;
                }
            }
            /** prepare the category based on whether it is all or a specific one */
            if(category == -1){
                categoryFilter = "";
            } else{
                categoryFilter = String.format("filters={\"category_ids\":{\"$includes\":%1$d}}&",
                        category);
            }
            /** prepare the current user location */
            LatLng latLng = intent.getParcelableExtra(MainActivity.USER_LOCATION);
            /** put all the components together to form the final URL */
            String factualUrl = String.format("http://api.v3.factual.com/t/places?%1$s%2$sgeo={\"$circle\":{\"$center\":[%3$f,%4$f],\"$meters\":%5$d}}&sort=$distance&limit=50&KEY=%6$s",
                    nameFilter,categoryFilter, latLng.latitude, latLng.longitude,radius, getResources().getString(R.string.factual_key));
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            StringBuilder builder = new StringBuilder();
            try {
                /** usual JSON protocol */
                URL url = new URL(factualUrl);
                connection = (HttpURLConnection) url.openConnection();
                if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                    Log.d("JsonFactualService", "connection not OK");
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while((line = reader.readLine()) != null){
                    builder.append(line);
                }
                JSONObject root = new JSONObject(builder.toString());
                JSONObject response = root.getJSONObject("response");
                JSONArray data = response.getJSONArray("data");

                ArrayList<Place> places = new ArrayList<>();
                ArrayList<Double> distances = new ArrayList<>();

                String [] category_labels_values = StaticMethods.categoriesValues;
                for (int i = 0; i < data.length(); i++) {
                    JSONObject placeAt = data.getJSONObject(i);
                    String address;
                    if(placeAt.has("address")){
                        address = placeAt.getString("address");
                    } else{
                        address = "";
                    }
                    String factualId = placeAt.getString("factual_id");
                    String categoryLabel = "else";
                    if(placeAt.has("category_labels")){
                        JSONArray category_labels = placeAt.getJSONArray("category_labels");
                        boolean found = false;
                        for (int j = 0; j < category_labels.length(); j++) {
                            JSONArray categoryInnerArray = category_labels.getJSONArray(j);
                            for (int k = 0; k < categoryInnerArray.length() ; k++) {
                                String currentCategory = categoryInnerArray.getString(k);
                                for (String category_labels_value : category_labels_values) {
                                    /** if the place has a matching category from one of the names of the category array values then save it as such, if fail, save as "else" */
                                    if (currentCategory.equals(category_labels_value)) {
                                        found = true;
                                        categoryLabel = currentCategory;
                                        break;
                                    }
                                }
                                if(found){
                                    break;
                                }
                            }
                            if(found){
                                break;
                            }
                        }
                    }
                    String name = placeAt.getString("name");
                    Double lat = placeAt.getDouble("latitude");
                    Double lng = placeAt.getDouble("longitude");
                    String locality;
                    if(placeAt.has("locality")){
                        locality = placeAt.getString("locality");
                    } else{
                        locality = "";
                    }
                    String phone;
                    if(placeAt.has("tel")){
                        phone = placeAt.getString("tel");
                    } else{
                        phone = "";
                    }
                    String website;
                    if(placeAt.has("website")){
                        website = placeAt.getString("website");
                    } else{
                        website = "";
                    }
                    /** add the new place to the array */
                    places.add(new Place(factualId,name,address,locality,categoryLabel,new LatLng(lat,lng),phone,website));
                    Double distance = placeAt.getDouble("$distance");
                    distances.add(distance);
                }
                SearchDBHandler searchDBHandler = new SearchDBHandler(this);
                /** delete old results before adding new ones */
                searchDBHandler.deleteAllSearchResults();
                /** add the new results to the search table in the db */
                searchDBHandler.addResults(places, distances);

                /** send a broadcast that there's new search results, along with the arrays */
                Intent i = new Intent(ACTION_SERVICE_FACTUAL_RECEIVE);
                i.putExtra(MainActivity.ARRAY_PLACES,places);
                i.putExtra(MainActivity.ARRAY_DISTANCES, distances);
                LocalBroadcastManager.getInstance(this).sendBroadcast(i);
            } catch (IOException | JSONException e) {
                Log.d("JsonFactualService", e.getMessage());
            } finally {
                if(connection != null){
                    connection.disconnect();
                } if(reader != null){
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.d("JsonFactualService", e.getMessage());
                    }
                }
            }
        }
    }
}
