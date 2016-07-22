package com.example.owner.alonshulmanproject2.activities;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.owner.alonshulmanproject2.R;
import com.example.owner.alonshulmanproject2.adapters.FavoritesRecyclerAdapter;
import com.example.owner.alonshulmanproject2.adapters.SearchRecyclerAdapter;
import com.example.owner.alonshulmanproject2.db.FavoritesDBHandler;
import com.example.owner.alonshulmanproject2.db.SearchDBHandler;
import com.example.owner.alonshulmanproject2.fragments.FavoritesFragment;
import com.example.owner.alonshulmanproject2.fragments.MapFragment;
import com.example.owner.alonshulmanproject2.fragments.SearchFragment;
import com.example.owner.alonshulmanproject2.model.Place;
import com.example.owner.alonshulmanproject2.receivers.PowerReceiver;
import com.example.owner.alonshulmanproject2.services.JsonFactualService;
import com.example.owner.alonshulmanproject2.static_methods.StaticMethods;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity
        implements  View.OnClickListener, LocationListener,
        SearchFragment.OnRequestClickListener, DialogInterface.OnClickListener,
        SearchRecyclerAdapter.OnSearchResultClickListener, FavoritesRecyclerAdapter.OnFavoriteResultClickListener, MapFragment.OnRouteReceivedListener {
    public static final String ACTION_TABLET_MAP_UPDATE = "com.example.owner.alonshulmanproject2.activities.ACTION_TABLET_MAP_UPDATE";
    public static final String RADIUS = "radius";
    public static final String CATEGORY = "category";
    public static final String ARRAY_PLACES = "places";
    public static final String ARRAY_DISTANCES = "distances";
    public static final String QUERY = "query";
    public static final String USER_LOCATION = "userLocation";
    public static final String IS_TABLET = "isTablet";
    private static final String PROVIDER_NAME = "providerName";
    private static final String GOT_LOCATION = "gotLocation";
    private static final String BTN_LEFT = "btnLeft";
    private static final String BTN_RIGHT = "btnRight";
    private static final int BUTTON_SEARCH_LEFT = 1;
    private static final int BUTTON_FAVS_LEFT = 2;
    private static final int BUTTON_MAP_RIGHT = 3;
    private static final int BUTTON_SEARCH_RIGHT = 4;
    private static final int PERMISSION_REQUEST_NEW_LOCATION = 1;
    private static final int PERMISSION_REQUEST_UNREGISTER = 2;
    private FragmentManager supportFragManager;
    private boolean isTablet,gotLocation;
    private Button btnLeft, btnRight;
    private PowerReceiver powerReceiver;
    private LocationManager locationManager;
    private String providerName,query;
    private Timer timer;
    private SearchDBHandler searchDBHandler;
    private FavoritesDBHandler favoritesDBHandler;
    private LatLng userLocation;
    private int radius,category;
    private RelativeLayout btnLayout, routeLayout;
    private TextView textDistance, textEta;
    private ProgressDialog progressDialog;
    private ImageView imageTransportMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** initialize */
        supportFragManager = getSupportFragmentManager();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        searchDBHandler = new SearchDBHandler(this);
        favoritesDBHandler = new FavoritesDBHandler(this);

        /** register global power receiver */
        powerReceiver = new PowerReceiver();
        IntentFilter powerConnectedFilter = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
        IntentFilter powerDisconnectedFilter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(powerReceiver, powerConnectedFilter);
        registerReceiver(powerReceiver, powerDisconnectedFilter);

        /** register local receiver */
        ReceivePlaces receivePlaces = new ReceivePlaces();
        IntentFilter filter = new IntentFilter(JsonFactualService.ACTION_SERVICE_FACTUAL_RECEIVE);
        LocalBroadcastManager.getInstance(this).registerReceiver(receivePlaces,filter);

        /** bind views to variables */
        btnLeft = (Button) findViewById(R.id.btnLeft);
        imageTransportMode = (ImageView) findViewById(R.id.imageTransportMode);
        btnLayout = (RelativeLayout) findViewById(R.id.btnLayout);
        routeLayout = (RelativeLayout) findViewById(R.id.routeLayout);
        textDistance = (TextView) findViewById(R.id.textDistance);
        textEta = (TextView) findViewById(R.id.textEta);

        btnLeft.setOnClickListener(this);

        if(savedInstanceState == null){ // new instance
            /** delete the distances from the previous search that will be showing up on start as it's not relevant anymore */
            searchDBHandler.deleteSearchDistances();
            query = "";

            /** set left button for the first time */
            setButtons(BUTTON_FAVS_LEFT);

            /** load search fragment to the search container */
            supportFragManager.beginTransaction().add(R.id.containerSearch, new SearchFragment()).commit();

            /** check if tablet or phone and if phone set up the right button for first time */
            if(findViewById(R.id.containerMap)== null){ /** phone */
                btnRight = (Button) findViewById(R.id.btnRight);
                isTablet = false;
                setButtons(BUTTON_MAP_RIGHT);
                btnRight.setOnClickListener(this);
            }
            else{ /** tablet */
                isTablet = true;
                openMapFragTablet(); /** initialize the map fragment for the first time */
            }
            /** run location for the first time, just to get the user's current location for getting route without running a new search */
            getLocation();
        }
        else{   /** resumed activity */
            /** load field variables from savedInstanceState */
            isTablet = savedInstanceState.getBoolean(IS_TABLET);
            providerName = savedInstanceState.getString(PROVIDER_NAME);
            gotLocation = savedInstanceState.getBoolean(GOT_LOCATION);
            query = savedInstanceState.getString(QUERY);
            userLocation = savedInstanceState.getParcelable(USER_LOCATION);
            radius = savedInstanceState.getInt(RADIUS);
            category = savedInstanceState.getInt(CATEGORY);
            setButtons(savedInstanceState.getInt(BTN_LEFT));

            if(!isTablet){  /** load only for phone */
                btnRight = (Button) findViewById(R.id.btnRight);
                setButtons(savedInstanceState.getInt(BTN_RIGHT));
                btnRight.setOnClickListener(this);
            } else{
                /** as the map fragment is always showing on tablets, reload it */
                replaceMapFragTablet();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /** stop the power receiver */
        unregisterReceiver(powerReceiver);
        /** if the user dismisses the progress dialog, make it null on destroy otherwise it will fail on resume */
        if(progressDialog != null){
            progressDialog = null;
        }
    }

    @Override
    public void onBackPressed() {
        /** make a quit message instead of quiting (as search, favorites and the map are all in one activity,
         *  someone might think that going back to the previous "screen" can be accomplished with pressing back) */
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.dialog_exit))
                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        /** save field variables for resuming */
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_TABLET, isTablet);
        outState.putInt(BTN_LEFT, Integer.parseInt(btnLeft.getTag().toString()));
        if(!isTablet){
            outState.putInt(BTN_RIGHT, Integer.parseInt(btnRight.getTag().toString()));
        }
        outState.putString(PROVIDER_NAME, providerName);
        outState.putBoolean(GOT_LOCATION, gotLocation);
        outState.putString(QUERY, query);
        outState.putParcelable(USER_LOCATION, userLocation);
        outState.putInt(RADIUS, radius);
        outState.putInt(CATEGORY, category);
    }

    public void openMapFragTablet(){
        /** tablet only, first time running, open a new fragment on the tablet in the map container */
        MapFragment mapFragment = new MapFragment();
        mapFragment.setArguments(getBundleForTabletMap());
        supportFragManager.beginTransaction().add(R.id.containerMap,mapFragment).commit();
    }
    public void replaceMapFragTablet(){
        /** tablet only, on resuming the app, replace the map fragment on the tablet */
        MapFragment mapFragment = new MapFragment();
        mapFragment.setArguments(getBundleForTabletMap());
        supportFragManager.beginTransaction().replace(R.id.containerMap,mapFragment).commit();
    }

    public Bundle getBundleForTabletMap(){
        /** get all necessary data into the bundle to be sent with the tablet fragment as setArguments */
        Bundle bundle = new Bundle();
        ArrayList<Place> places = searchDBHandler.getAllSearchResultsNoDistances();
        bundle.putParcelableArrayList(ARRAY_PLACES,places);
        bundle.putParcelable(USER_LOCATION, userLocation);
        bundle.putBoolean(IS_TABLET,isTablet);
        return bundle;
    }

    public void openMapFragPhone(Bundle bundle){
        /** phone only, when clicked on the map button or a specific place is search or favorites, replace fragment in search container. sends a bundle with the data */
        MapFragment mapFragment = new MapFragment();
        mapFragment.setArguments(bundle);
        supportFragManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.containerSearch,mapFragment)
                .commit();
    }

    public void openSearchFrag(){
        /** when clicked on the search button, replace the fragment to the search */
        supportFragManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.containerSearch, new SearchFragment())
                .commit();
    }

    public void openFavoritesFrag(){
        /** when clicked on the favorites button, replace the fragment to the favorites */
        supportFragManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.containerSearch, new FavoritesFragment())
                .commit();
    }

    @Override
    public void onSearchResultClick(String factualId) {
        /** interface from search, runs when clicked on a place from the search */
        getResultPlace(factualId, true);
    }

    @Override
    public void onFavoriteResultClick(String factualId) {
        /** interface from favorites, runs when clicked on a saved place from favorites */
        getResultPlace(factualId, false);
    }

    public void getResultPlace(String factualId, boolean isSearchFromSearch){
        /** wrap the place in an array to be compatible with the map fragment, as it can show multiple places from search (without clicking a specific one) */
        ArrayList<Place> places = new ArrayList<>();
        Place place;
        if(isSearchFromSearch){ /** get place from search db */
            place = searchDBHandler.getPlace(factualId);
        } else{ /** get place from favorites db */
            place = favoritesDBHandler.getPlace(factualId);
        }
        places.add(place);
        if(!isTablet) {
            /** phone only, set the bundle data for the fragment replace then run it, and change the buttons */
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList(ARRAY_PLACES, places);
            bundle.putParcelable(USER_LOCATION, userLocation);
            openMapFragPhone(bundle);
            setButtons(BUTTON_SEARCH_RIGHT);
            setButtons(BUTTON_FAVS_LEFT);
        } else{
            /** tablet only, as the map is always up, send the data in a broadcast */
            Intent i = new Intent(ACTION_TABLET_MAP_UPDATE);
            i.putParcelableArrayListExtra(ARRAY_PLACES,places);
            i.putExtra(USER_LOCATION, userLocation);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        }
    }

    public void getLocation(){
        /** try to get gps location for 5 seconds, if it fails, run a network location instead */

        /** get the current status if gps and network location are enabled or not */
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if(!gps && !network){
            /** in case neither are enabled, give a message to the user */
            Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_no_gps_no_internet_location), Toast.LENGTH_SHORT).show();
        } else{
            /** run a permission check for fine location */
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if(permissionCheck == PackageManager.PERMISSION_GRANTED){
                /** set gotLocation to false - a new location search */
                gotLocation = false;
                providerName = LocationManager.GPS_PROVIDER;
                /** set the progress dialog to show the different stages of the location search */
                progressDialog = new ProgressDialog(this);
                progressDialog.setTitle(getResources().getString(R.string.dialog_getting_gps_location));
                progressDialog.show();
                /** request the gps search */
                locationManager.requestLocationUpdates(providerName, 1000, 0, this);

                /** create the timer and run it in a new thread */
                timer = new Timer("provider");
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        if(!gotLocation){
                            int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
                            if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                locationManager.removeUpdates(MainActivity.this);
                                runOnUiThread(new Runnable() {
                                    /** return to ui thread when location was found to run: */
                                    @Override
                                    public void run() {
                                        /** check if there's a network connection active in the device */
                                        if(StaticMethods.checkNetwork(MainActivity.this)){
                                            /** run the location search now on the network provider and check permission */
                                            providerName = LocationManager.NETWORK_PROVIDER;
                                            int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION);
                                            if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                                locationManager.requestLocationUpdates(providerName, 1000, 0, MainActivity.this);
                                                if(progressDialog!= null){
                                                    progressDialog.setTitle(getResources().getString(R.string.dialog_getting_network_location));
                                                }
                                            }
                                        } else{
                                            /** no connection active, give the user a message */
                                            if(progressDialog!= null){
                                                progressDialog.dismiss();
                                                Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_no_internet_after_gps), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                };
                /** schedule the timer to run */
                timer.schedule(task, new Date(System.currentTimeMillis() + 5000));
            } else{
                /** no location permission granted, run the requestPermission method and ask for it */
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_NEW_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /** marshmallow and up, if location permission not allowed yet, run */
        switch (requestCode){
            case PERMISSION_REQUEST_NEW_LOCATION:
                /** in case of a request */
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    /** if granted, run getLocation again */
                    getLocation();
                } else {
                    /** request denied, give the user a message */
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_needs_location_permission), Toast.LENGTH_SHORT).show();
                }
                break;
            case PERMISSION_REQUEST_UNREGISTER:
                /** in case of a user shutting down the permission after running the search in the app and before it unregisters, slim chances... */
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
                    if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
                        locationManager.removeUpdates(this);
                    }
                } else{
                    Toast.makeText(MainActivity.this, getResources().getString(R.string.toast_needs_location_permission), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        /** left and right buttons, since their purpose is different each time, needs a key, which is the tag property. */
        int tag = Integer.parseInt(v.getTag().toString());
        /** run button animation - to make a better visual transition between fragments and text changes in the buttons */
        animateButton();
        /** hide the keyboard after a click */
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        switch (tag){
            case BUTTON_SEARCH_LEFT:
                openSearchFrag();
                setButtons(BUTTON_FAVS_LEFT);
                break;
            case BUTTON_FAVS_LEFT:
                openFavoritesFrag();
                setButtons(BUTTON_SEARCH_LEFT);
                if(!isTablet){
                    /** phone only, remove the route layout since it now opens a new fragment */
                    setButtons(BUTTON_MAP_RIGHT);
                    routeLayout.setVisibility(View.GONE);
                }
                break;
            case BUTTON_MAP_RIGHT:
                /** phone only, get the bundle data ready for the fragment */
                Bundle bundle = new Bundle();
                ArrayList<Place> places = searchDBHandler.getAllSearchResultsNoDistances();
                bundle.putParcelableArrayList(ARRAY_PLACES, places);
                bundle.putParcelable(USER_LOCATION, userLocation);
                bundle.putBoolean(IS_TABLET,isTablet);
                openMapFragPhone(bundle);
                setButtons(BUTTON_SEARCH_RIGHT);
                setButtons(BUTTON_FAVS_LEFT);
                break;
            case BUTTON_SEARCH_RIGHT:
                /** phone only, remove the route layout since it now opens a new fragment */
                routeLayout.setVisibility(View.GONE);
                openSearchFrag();
                setButtons(BUTTON_MAP_RIGHT);
                break;
        }
    }

    private void setButtons(int state){
        /** change the button text and tag according to the now current logic */
        switch (state){
            case BUTTON_SEARCH_LEFT:
                btnLeft.setText(R.string.button_search);
                btnLeft.setTag(BUTTON_SEARCH_LEFT);
                break;
            case BUTTON_FAVS_LEFT:
                btnLeft.setText(R.string.button_favorites);
                btnLeft.setTag(BUTTON_FAVS_LEFT);
                break;
            case BUTTON_MAP_RIGHT:
                btnRight.setText(R.string.button_map);
                btnRight.setTag(BUTTON_MAP_RIGHT);
                break;
            case BUTTON_SEARCH_RIGHT:
                btnRight.setText(R.string.button_search);
                btnRight.setTag(BUTTON_SEARCH_RIGHT);
                break;
        }
    }

    public void animateButton(){
        /** simple animation that fades the buttons in after a click */
        ObjectAnimator animApp = ObjectAnimator.ofFloat(btnLayout,"alpha",0,1);
        animApp.setDuration(1000);
        AnimatorSet anim = new AnimatorSet();
        anim.play(animApp);
        anim.start();
    }

    @Override
    public void onLocationChanged(Location location) {
        /** received a location */
        gotLocation = true;
        /** cancel the timer */
        timer.cancel();
        /** check permission to unregister the updates */
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck == PackageManager.PERMISSION_GRANTED){
            locationManager.removeUpdates(this);
        } else{
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_REQUEST_UNREGISTER);
        }
        if(userLocation == null){ // on app start don't run the search but get user's location for routes
            /** on app start the first location request is automatic and only gets the current user location for routes, therefor don't make a new search */
            userLocation = new LatLng(location.getLatitude(),location.getLongitude());
            if(progressDialog!= null){
                progressDialog.dismiss();
            }
        } else{
            /** on a search, send all relevant data to the service that gets the search results */
            if(progressDialog!= null){
                progressDialog.setTitle(getResources().getString(R.string.dialog_searching_for_places));
            }
            Intent i = new Intent(this,JsonFactualService.class);
            userLocation = new LatLng(location.getLatitude(),location.getLongitude());
            i.putExtra(USER_LOCATION,new LatLng(location.getLatitude(),location.getLongitude()));
            i.putExtra(QUERY, query);
            i.putExtra(RADIUS, radius);
            i.putExtra(CATEGORY, category);

            startService(i);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    @Override
    public void onProviderEnabled(String provider) {
    }
    @Override
    public void onProviderDisabled(String provider) {
    }


    @Override
    public void onRequestSearch(String query, int radius, int category) {
        /** interface from search fragment, gets the data for the search and runs the location service to start the process */
        this.query = query;
        this.radius = radius;
        this.category = category;
        getLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /** options menu for the app */
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.open_prefs:
                /** opens the preferences fragment */
                Intent i = new Intent(this, PrefsActivity.class);
                startActivity(i);
                break;
            case R.id.clear_favorites:
                /** runs an alert dialog to ask if sure */
                AlertDialog dialog = new AlertDialog.Builder(this).create();
                dialog.setTitle(getResources().getString(R.string.are_you_sure));
                dialog.setMessage(getResources().getString(R.string.delete_favorites_message));
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, getResources().getString(R.string.yes), this);
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.no), this);
                dialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onClick(DialogInterface dialog, int which) {
        /** alert dialog for clearing the saved favorites */
        dialog.dismiss();
        switch (which){
            case DialogInterface.BUTTON_POSITIVE:
                favoritesDBHandler.deleteAllFavorites();
                /** after deleting from db, broadcast that the favorites were cleared in case the user is on the favorites fragment */
                Intent in = new Intent(FavoritesRecyclerAdapter.ACTION_FAVS_CLEAR);
                LocalBroadcastManager.getInstance(this).sendBroadcast(in);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                /** do nothing... */
                break;
        }
    }

    public void sharePlace(Place place){
        /** simple text share intent with what data is available of the place */
        Intent i = new Intent(Intent.ACTION_SEND);
        String phone = "";
        if(place.getPhone()!= null && !place.getPhone().equals("")){
            phone = String.format("\n%1$s: %2$s",
                    getResources().getString(R.string.phone),place.getPhone());
        }
        String website = "";
        if(place.getWebsite()!= null && !place.getWebsite().equals("")){
            website = String.format("\n%1$s: %2$s",
                    getResources().getString(R.string.website),place.getWebsite());
        }
        String message = String.format("%1$s:\n%2$s\n%3$s: %4$s%5$s%6$s",
                getResources().getString(R.string.check_this_place),place.getName(),getResources().getString(R.string.address),place.getAddress(),phone,website);
        i.putExtra(Intent.EXTRA_TEXT,message);
        i.setType("text/plain");
        startActivity(i);
    }

    @Override
    public void onRouteReceived(String distance, String eta, String transportMode) {
        /** interface from map fragment, gets the data to show on the route layout */
        routeLayout.setVisibility(View.VISIBLE);
        textDistance.setText(distance);
        textEta.setText(eta);
        if(transportMode.equals("1")){
            imageTransportMode.setImageResource(R.drawable.driving);
        } else{
            imageTransportMode.setImageResource(R.drawable.walking);
        }
    }

    public class ReceivePlaces extends BroadcastReceiver{
        /** receiver that a new search was completed and the places were added to the search fragment, dismiss the progress dialog  */
        @Override
        public void onReceive(Context context, Intent intent) {
            if(progressDialog!= null){
                progressDialog.dismiss();
            }
        }
    }
}
