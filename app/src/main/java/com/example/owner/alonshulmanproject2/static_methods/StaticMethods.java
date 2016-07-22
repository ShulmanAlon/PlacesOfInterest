package com.example.owner.alonshulmanproject2.static_methods;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.example.owner.alonshulmanproject2.R;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

/**
 * Created by Owner on 08/04/2016.
 */
public class StaticMethods {

    /** static methods just for recycling commonly used ones */

    public static final String[] categoriesValues = {"else","Food and Dining","Arts","Automotive","Bars","Businesses and Services","Community and Government","Entertainment"
            ,"Healthcare","Landmarks","Lodging","Retail","Sports and Recreation","Transportation","Zoos, Aquariums and Wildlife Sanctuaries"};
    public static final int[] categoriesIconsId = {R.drawable.unknown,R.drawable.food,R.drawable.arts,R.drawable.automotive,R.drawable.bar,
            R.drawable.services,R.drawable.government,R.drawable.entertainment,R.drawable.healthcare,R.drawable.landmark,R.drawable.hotel,
            R.drawable.retail,R.drawable.sports,R.drawable.transportation, R.drawable.zoo};
    public static final int[] categoriesId = {-1,338,309,2,312,177,20,317,62,107,432,123,372,415,371};

    public static int getCategoryIconId(String category){
        /** returns the ID for the current category icon for the recycler view adapters of search and favorites */
        int currentIconId = R.drawable.unknown;
        for (int i = 0; i < categoriesValues.length; i++) {
            if(category.equals(categoriesValues[i])){
                currentIconId = categoriesIconsId[i];
                break;
            }
        } return currentIconId;
    }

    public static boolean checkNetwork(Context context){
        /** returns whether there is internet connection available or not */
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if(info != null && info.isConnected()){
            return true;
        } else{
            return false;
        }
    }

    public static ArrayList<LatLng> decodePolyPoints(String encodedPath){
        /** decoder for the encoded polyline string the google direction API returns, returns it as an array of LatLng for use with the google map v2 */
        int len = encodedPath.length();

        final ArrayList<LatLng> path = new ArrayList<>();
        int index = 0;
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int result = 1;
            int shift = 0;
            int b;
            do {
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lat += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            result = 1;
            shift = 0;
            do {
                b = encodedPath.charAt(index++) - 63 - 1;
                result += b << shift;
                shift += 5;
            } while (b >= 0x1f);
            lng += (result & 1) != 0 ? ~(result >> 1) : (result >> 1);

            path.add(new LatLng(lat * 1e-5, lng * 1e-5));
        }
        return path;
    }
}
