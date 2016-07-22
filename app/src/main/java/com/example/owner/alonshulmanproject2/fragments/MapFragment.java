package com.example.owner.alonshulmanproject2.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import com.example.owner.alonshulmanproject2.R;
import com.example.owner.alonshulmanproject2.activities.MainActivity;
import com.example.owner.alonshulmanproject2.model.Place;
import com.example.owner.alonshulmanproject2.model.Route;
import com.example.owner.alonshulmanproject2.services.JsonFactualService;
import com.example.owner.alonshulmanproject2.static_methods.StaticMethods;
import com.example.owner.alonshulmanproject2.tasks.GetRouteTask;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends SupportMapFragment implements OnMapReadyCallback, GetRouteTask.GetRouteFromTaskListener {
    private boolean needsInit=false;
    private ArrayList<Place> places;
    private LatLng userLocation;
    private GoogleMap mMap;
    private OnRouteReceivedListener listener;
    private Context context;
    private ReceiveTabletPlaceClicked receiveTabletPlaceClicked;
    private ReceivePlacesFromNewSearch receivePlaces;

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        listener = (OnRouteReceivedListener) context;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /** if running for the first time, flag as such */
        if (savedInstanceState == null) {
            needsInit=true;
        }
        Bundle bundle = getArguments();
        /** get places array and user location from getArguments */
        places = bundle.getParcelableArrayList(MainActivity.ARRAY_PLACES);
        userLocation = bundle.getParcelable(MainActivity.USER_LOCATION);
        /** start the map async method */
        getMapAsync(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** register receivers for tablet updates and when a new search arrives */
        receiveTabletPlaceClicked = new ReceiveTabletPlaceClicked();
        IntentFilter onReceiveTabletUpdateFilter = new IntentFilter(MainActivity.ACTION_TABLET_MAP_UPDATE);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receiveTabletPlaceClicked, onReceiveTabletUpdateFilter);

        receivePlaces = new ReceivePlacesFromNewSearch();
        IntentFilter receivePlacesFilter = new IntentFilter(JsonFactualService.ACTION_SERVICE_FACTUAL_RECEIVE);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receivePlaces, receivePlacesFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /** unregister the receivers since the fragment may be replaced / closed */
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiveTabletPlaceClicked);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receivePlaces);

    }

    @Override
    public void onMapReady(final GoogleMap map) {
        /** when map async finishes run the refresh map method to load the new data */
        mMap = map;
        refreshMap(map);
    }

    public void refreshMap(GoogleMap map){
        /** runs and displays the current data in the search or specific place */
        /** disable the google map pop up in map */
        map.getUiSettings().setMapToolbarEnabled(false);
        LatLng center;
        float zoom = 15;
        if (needsInit) {
            /** if running for the first time zoom to current location, if none exists, don't zoom at all and notify the user */
            needsInit = false;
            if(userLocation == null){
                if(places.isEmpty()){
                    Toast.makeText(getContext(), getResources().getString(R.string.toast_no_information), Toast.LENGTH_SHORT).show();
                    center = new LatLng(0,0);
                    zoom = 1;
                } else{
                    center = places.get(0).getLatLng();
                }
            } else{
                center = userLocation;
            }
        } else{
            /** if resumed zoom on user location or if not available, the first place in the array since it can be either a full search or a specific place,
             *  if neither exist, don't zoom at all */
            if(userLocation!= null){
                center = userLocation;
            } else{
                if(!places.isEmpty()){
                    center = places.get(0).getLatLng();
                } else{
                    center = new LatLng(0,0);
                    zoom = 1;
                }
            }
        }
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(center, zoom);
        map.animateCamera(cameraUpdate);
        /** if user location exists draw the user's location in a marker with a circle */
        if(userLocation != null){
            map.addCircle(new CircleOptions().center(userLocation).radius(30).strokeWidth(0).fillColor(0x200000FF));
            map.addMarker(new MarkerOptions().flat(true).anchor(0.5f,0.5f).title(getResources().getString(R.string.me)).position(userLocation)
                    .snippet(getResources().getString(R.string.i_am_here)).icon(BitmapDescriptorFactory.fromResource(R.drawable.circle_marker)));

        }
        for (int i = 0; i < places.size(); i++) {
            /** add markers for all current search places, or a single place if clicked */
            Place place = places.get(i);
            if(userLocation != null && places.size() == 1){
                /** in the case it is only one specific place, get route from google direction API and draw it(in a different method) */
                GetRouteTask getRouteTask = new GetRouteTask(this);
                /** for reasons still unknown to me I needed to save the context... getContext failed... */
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                /** get the current units and transportation mode from shared preferences */
                String units;
                String transportMode;
                if(preferences.getString(PrefsFragment.UNITS,"1").equals("1")){
                    units = "metric";
                } else{
                    units = "imperial";
                }
                if(preferences.getString(PrefsFragment.TRANSPORT_MODE,"1").equals("1")){
                    transportMode = "driving";
                }else{
                    transportMode = "walking";
                }
                /** build the url to query the API and send it */
                String url = String.format("https://maps.googleapis.com/maps/api/directions/json?origin=%1$f,%2$f&destination=%3$f,%4$f&mode=%5$s&units=%6$s",
                        userLocation.latitude, userLocation.longitude,place.getLatLng().latitude,place.getLatLng().longitude,transportMode,units);
                getRouteTask.execute(url);
            }
            map.addMarker(new MarkerOptions().title(place.getName()).position(place.getLatLng()).snippet(place.getAddress() + ", " + place.getPhone()));
        }
    }

    @Override
    public void getRouteFromTask(Route route) {
        /** receiving a new route from google directions API */
        if(route != null && mMap != null){
            ArrayList<LatLng> decPolyline = StaticMethods.decodePolyPoints(route.getEncPolyline());
            mMap.addPolyline(new PolylineOptions().addAll(decPolyline).width(5).color(Color.MAGENTA));
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(route.getBounds(),40));
            String transportMode = PreferenceManager.getDefaultSharedPreferences(context).getString(PrefsFragment.TRANSPORT_MODE,"1");
            listener.onRouteReceived(route.getDistance(),route.getDuration(),transportMode);
        }
    }

    public interface OnRouteReceivedListener{
        /** interface to main activity to display the overlay information of the route */
        void onRouteReceived(String distance, String eta, String transportMode);
    }

    public class ReceiveTabletPlaceClicked extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            /** receiver for when a place is clicked in tablet, since the map is already up and running */
            places = intent.getParcelableArrayListExtra(MainActivity.ARRAY_PLACES);
            userLocation = intent.getParcelableExtra(MainActivity.USER_LOCATION);
            mMap.clear();
            refreshMap(mMap);
        }
    }

    public class ReceivePlacesFromNewSearch extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            /** receiver for when new search result places arrive, load the new places */
            places = intent.getParcelableArrayListExtra(MainActivity.ARRAY_PLACES);
            userLocation = intent.getParcelableExtra(MainActivity.USER_LOCATION);
            mMap.clear();
            refreshMap(mMap);
        }
    }
}

