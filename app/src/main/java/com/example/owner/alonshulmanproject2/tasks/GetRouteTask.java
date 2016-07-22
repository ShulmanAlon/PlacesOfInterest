package com.example.owner.alonshulmanproject2.tasks;

import android.os.AsyncTask;
import android.util.Log;
import com.example.owner.alonshulmanproject2.model.Route;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Owner on 06/04/2016.
 */
public class GetRouteTask extends AsyncTask<String,Void,Route> {
    private GetRouteFromTaskListener listener;

    public GetRouteTask(GetRouteFromTaskListener listener) {
        this.listener = listener;
    }
    /** asynctask for getting route from google directions API, along with zoom bounds, distance with either a car or walking, and duration */

    @Override
    protected Route doInBackground(String... params) {
        String address = params[0];
        HttpsURLConnection connection = null;
        BufferedReader reader = null;
        URL url;
        StringBuilder builder = new StringBuilder();
        try {
            url = new URL(address);
            connection = (HttpsURLConnection) url.openConnection();
            if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                return null;
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                try {
                    JSONObject root = new JSONObject(builder.toString());
                    if(root.getString("status").equals("OK")){
                        JSONArray results = root.getJSONArray("routes");
                        JSONObject result = results.getJSONObject(0);
                        JSONObject polyObject = result.getJSONObject("overview_polyline");
                        String encPolyline = polyObject.getString("points");

                        JSONArray legs = result.getJSONArray("legs");
                        JSONObject legsObj = legs.getJSONObject(0);
                        JSONObject distanceObj = legsObj.getJSONObject("distance");
                        String distance = distanceObj.getString("text");
                        JSONObject durationObj = legsObj.getJSONObject("duration");
                        String duration = durationObj.getString("text");
                        JSONObject boundsObj = result.getJSONObject("bounds");

                        JSONObject northEastObj = boundsObj.getJSONObject("northeast");
                        double northEastLat = northEastObj.getDouble("lat");
                        double northEastLng = northEastObj.getDouble("lng");

                        JSONObject southWestObj = boundsObj.getJSONObject("southwest");
                        double southWestLat = southWestObj.getDouble("lat");
                        double southWestLng = southWestObj.getDouble("lng");

                        LatLngBounds bounds = new LatLngBounds(new LatLng(southWestLat,southWestLng),new LatLng(northEastLat,northEastLng));

                        return new Route(distance,duration,encPolyline,bounds);
                    }
                } catch (JSONException e) {
                    Log.e("Json Handler",e.getMessage());
                }
            }
        } catch (MalformedURLException e) {
            Log.e("GetRouteTask", e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e("GetRouteTask", e.getMessage());
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e("GetRouteTask", e.getMessage());
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Route route) {
        /** returns the information to the map fragment */
        listener.getRouteFromTask(route);
    }

    public interface GetRouteFromTaskListener{
        void getRouteFromTask(Route route);
    }
}
