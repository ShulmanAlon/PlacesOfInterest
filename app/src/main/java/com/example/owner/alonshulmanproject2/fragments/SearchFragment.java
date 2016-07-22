package com.example.owner.alonshulmanproject2.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.example.owner.alonshulmanproject2.R;
import com.example.owner.alonshulmanproject2.activities.MainActivity;
import com.example.owner.alonshulmanproject2.adapters.SearchRecyclerAdapter;
import com.example.owner.alonshulmanproject2.db.DBProvider;
import com.example.owner.alonshulmanproject2.db.SearchDBHandler;
import com.example.owner.alonshulmanproject2.model.Place;
import com.example.owner.alonshulmanproject2.services.JsonFactualService;
import com.example.owner.alonshulmanproject2.static_methods.StaticMethods;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment implements View.OnClickListener, SearchView.OnQueryTextListener {
    private SearchRecyclerAdapter adapter;
    private OnRequestClickListener listener;
    private SearchView searchView;
    private SearchDBHandler handler;
    private String currentCategory;
    private String currentRadius;
    private Spinner spinnerCategory;
    private Spinner spinnerRadius;
    private boolean isAdvancedSearch;
    private static final String CATEGORY_SELECTED = "categorySelected";
    private static final String RADIUS_SELECTED = "radiusSelected";
    private ImageView imgAdvancedSearch;
    private View advancedSearchLayout;
    private TextView textSearchMode;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnRequestClickListener) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /** set-up the receiver */
        ReceivePlaces receivePlaces = new ReceivePlaces();
        IntentFilter filter = new IntentFilter(JsonFactualService.ACTION_SERVICE_FACTUAL_RECEIVE);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(receivePlaces, filter);

        handler = new SearchDBHandler(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /** Inflate the layout for this fragment */
        View v = inflater.inflate(R.layout.fragment_search, container, false);

        /** set-up the recycler view and it's adapter */
        RecyclerView recyclerSearch = (RecyclerView) v.findViewById(R.id.recyclerSearch);
        recyclerSearch.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new SearchRecyclerAdapter(getContext());
        recyclerSearch.setAdapter(adapter);

        /** initialize views */
        searchView = (SearchView) v.findViewById(R.id.searchView);
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setFocusable(false);

        imgAdvancedSearch = (ImageView) v.findViewById(R.id.imgAdvancedSearch);
        imgAdvancedSearch.setOnClickListener(this);
        advancedSearchLayout = v.findViewById(R.id.advancedSearch);
        textSearchMode = (TextView) v.findViewById(R.id.textSearchMode);

        spinnerCategory = (Spinner) v.findViewById(R.id.spinnerCategory);
        spinnerRadius = (Spinner) v.findViewById(R.id.spinnerRadius);

        if(savedInstanceState!= null){
            /** on start, select default category and radius for spinners */
            currentCategory = savedInstanceState.getString(CATEGORY_SELECTED);
            currentRadius = savedInstanceState.getString(RADIUS_SELECTED);
        }
        v.findViewById(R.id.btnAround).setOnClickListener(this);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        /** get units from shared prefs and display accordingly  */
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        String units = sp.getString(PrefsFragment.UNITS, PrefsFragment.UNIT_KM);
        boolean isMeter;
        if(units.equals(PrefsFragment.UNIT_KM)){
            isMeter = true;
        } else{
            isMeter = false;
        }
        String[] categoryArray = getResources().getStringArray(R.array.spinner_category_titles);
        /** refresh the recycler view adapter */
        adapter.refreshUnits(isMeter);
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_dropdown_item_1line,categoryArray);
        spinnerCategory.setAdapter(categoryAdapter);
        if(categoryAdapter.getPosition(currentCategory) != -1){
            /** if spinner has a position, set the selection to it */
            spinnerCategory.setSelection(categoryAdapter.getPosition(currentCategory));
        } else{
            /** if spinner adapter is uninitialized, set the default to All categories */
            spinnerCategory.setSelection(0);
        }
        ArrayAdapter<String> radiusAdapter;
        if(isMeter){
            /** load the metric array to the spinner */
            radiusAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_dropdown_item_1line,getResources().getStringArray(R.array.spinner_radius_km));
        } else{
            /** load the imperial array to the spinner */
            radiusAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_dropdown_item_1line,getResources().getStringArray(R.array.spinner_radius_miles));
        }
        spinnerRadius.setAdapter(radiusAdapter);
        if(radiusAdapter.getPosition(currentRadius) != -1){
            /** if spinner has a position, set the selection to it */
            spinnerRadius.setSelection(radiusAdapter.getPosition(currentRadius));
        } else{
            /** if spinner adapter is uninitialized, set the default to position 3 */
            spinnerRadius.setSelection(3);
        }
        /** start loading the search places from db and show it */
        getSearch();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /** save to support orientation change */
        outState.putString(RADIUS_SELECTED,spinnerRadius.getSelectedItem().toString());
        outState.putString(CATEGORY_SELECTED,spinnerCategory.getSelectedItem().toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnAround:
                /** queries all nearest places to the user from the API */
                sendSearch(null);
                break;
            case R.id.imgAdvancedSearch:
                /** open / close the advanced search options, for radius and category filtering */
                isAdvancedSearch = !isAdvancedSearch;
                if(isAdvancedSearch){
                    imgAdvancedSearch.setRotation(180);
                    advancedSearchLayout.setVisibility(View.VISIBLE);
                    textSearchMode.setText(getResources().getString(R.string.advanced_search));
                } else{
                    imgAdvancedSearch.setRotation(0);
                    advancedSearchLayout.setVisibility(View.GONE);
                    textSearchMode.setText(getResources().getString(R.string.regular_search));
                }
                break;
        }
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        /** queries all nearest places to the user from the API using text filter */
        sendSearch(query);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public void sendSearch(String query){
        /** gets the parameters for the new search, and proceed to get them from the internet */
        searchView.clearFocus();
        onGetSearch(query);
    }

    public void onGetSearch(String query){
        /** after getting the user's query and advanced search options (if any), run this logic to proceed to the intent service that will handle getting the results */
        /** check if there's a network connection */
        if(StaticMethods.checkNetwork(getContext())){
            /** there is an internet connection, get the parameters of the query and continue */
            currentRadius = spinnerRadius.getSelectedItem().toString();
            currentCategory = spinnerCategory.getSelectedItem().toString();
            if(isAdvancedSearch){
                /** if query comes from an advanced search */
                int radius;
                String currentUnit = PreferenceManager.getDefaultSharedPreferences(getContext()).getString(PrefsFragment.UNITS,PrefsFragment.UNIT_KM);
                if(currentUnit.equals(PrefsFragment.UNIT_KM)){
                    String radiusS = currentRadius.substring(0,currentRadius.indexOf(getResources().getString(R.string.km)));
                    radius = (int)(1000*(Float.parseFloat(radiusS)));
                } else{
                    String radiusS = currentRadius.substring(0,currentRadius.indexOf(getResources().getString(R.string.miles)));
                    radius = (int)(1609.34*(Float.parseFloat(radiusS)));
                }
                int categoryId = getCategoryIdFromString(currentCategory);

                listener.onRequestSearch(query,radius,categoryId);
            } else{
                /** normal search, proceed using default parameters(2500 meters radius, all categories) */
                listener.onRequestSearch(query,2500,-1);
            }
        } else{
            /** no internet connection available, notify the user */
            Toast.makeText(getContext(), getResources().getString(R.string.toast_no_internet), Toast.LENGTH_SHORT).show();
        }
    }

    public int getCategoryIdFromString(String category){
        /** returns the category ID number for use with the filter field of the factual API */
        String[] categoryNames = getResources().getStringArray(R.array.spinner_category_titles);
        int currentCategoryId = -1;
        for (int i = 0; i < categoryNames.length; i++) {
            if(category.equals(categoryNames[i])){
                currentCategoryId = StaticMethods.categoriesId[i];
                break;
            }
        }
        return currentCategoryId;
    }


    public void getSearch(){
        /** get all data from the search table in the db, including the distance column */
        Cursor c = handler.getAllSearchResultsWithDistances();
        ArrayList<Place> places = new ArrayList<>();
        ArrayList<Double> distances = new ArrayList<>();
        while(c.moveToNext()){
            /** dissect the cursor to two arrays , places and distances, for use of the recycler adapter */
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
            double distance = c.getDouble(c.getColumnIndex(DBProvider.Search.DISTANCE_COLUMN));
            distances.add(distance);
        }
        adapter.setPlaces(places, distances);
    }

    public class ReceivePlaces extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            /** receiver used to get new search results from service and display them in the adapter */
            ArrayList<Place> places = intent.getParcelableArrayListExtra(MainActivity.ARRAY_PLACES);
            ArrayList<Double> distances = (ArrayList<Double>) intent.getSerializableExtra(MainActivity.ARRAY_DISTANCES);
            adapter.setPlaces(places,distances);
        }
    }

    public interface OnRequestClickListener{
        /** interface to main activity */
        void onRequestSearch(String query,int radius,int categoryId);
    }
}
