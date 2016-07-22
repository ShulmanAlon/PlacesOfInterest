package com.example.owner.alonshulmanproject2.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.owner.alonshulmanproject2.R;
import com.example.owner.alonshulmanproject2.adapters.FavoritesRecyclerAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoritesFragment extends Fragment {

    public FavoritesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_favorites, container, false);

        /** set-up the recycler and it's adapter */
        RecyclerView recyclerFavorites = (RecyclerView) v.findViewById(R.id.recyclerFavorites);
        recyclerFavorites.setLayoutManager(new LinearLayoutManager(getContext()));
        FavoritesRecyclerAdapter adapter = new FavoritesRecyclerAdapter(getContext());
        recyclerFavorites.setAdapter(adapter);

        return v;
    }

}
