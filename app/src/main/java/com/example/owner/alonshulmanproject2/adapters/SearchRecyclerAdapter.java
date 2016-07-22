package com.example.owner.alonshulmanproject2.adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.owner.alonshulmanproject2.R;
import com.example.owner.alonshulmanproject2.db.FavoritesDBHandler;
import com.example.owner.alonshulmanproject2.model.Place;
import com.example.owner.alonshulmanproject2.static_methods.StaticMethods;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Owner on 28/03/2016.
 */
public class SearchRecyclerAdapter extends RecyclerView.Adapter<SearchRecyclerAdapter.PlaceHolder> {
    private Context context;
    private ArrayList<Place> places;
    private ArrayList<Double> distances;
    private boolean isMeter;
    private OnSearchResultClickListener listener;
    private static final int VIEW_NORMAL = 1;
    private static final int VIEW_FOOTER = 2;

    public SearchRecyclerAdapter(Context context) {
        this.context = context;
        /** initialize CTor */
        places = new ArrayList<>();
        distances = new ArrayList<>();
        listener = (OnSearchResultClickListener) context;
    }

    public void setPlaces(ArrayList<Place> places, ArrayList<Double> distances){
        /** get places from db and set them to the adapter */
        this.places = places;
        this.distances = distances;
        notifyDataSetChanged();
    }

    public void refreshUnits(boolean isMeter){
        /** called when search fragment resumes */
        this.isMeter = isMeter;
    }

    @Override
    public PlaceHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        /** two view types, normal view, and footer - which is a blank spacer so that the buttons wouldn't block any info from the last item */
        LayoutInflater inflater = LayoutInflater.from(context);
        View v;
        if(viewType == VIEW_NORMAL){
            v = inflater.inflate(R.layout.search_list_item,parent,false);
        } else{
            v = inflater.inflate(R.layout.footer,parent,false);
        }
        return new PlaceHolder(v);
    }

    @Override
    public void onBindViewHolder(PlaceHolder holder, int position) {
        /** if the position is the size of the array (which means +1), it is the footer */
        if(position != places.size()){
            /** if the distance is 0 or lower (old searches), don't display it at all */
            if(distances == null || distances.size() == 0){
                holder.bindPlace(places.get(position),-1);
            }
            holder.bindPlace(places.get(position),distances.get(position));
        } else{
            holder.bindFooter();
        }
    }

    @Override
    public int getItemViewType(int position) {
        /** if the position is the size of the array (which means +1), it is the footer */
        if(position == places.size()){
            return VIEW_FOOTER;
        }
        return VIEW_NORMAL;
    }

    @Override
    public int getItemCount() {
        /** add one to the size of the array for the footer */
        return places.size()+1;
    }

    public class PlaceHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView textItemName,textItemAddress,textItemLocality,textItemDistance, textItemCategory, textItemPhone, textItemWebsite;
        private Place place;
        private ImageView imageItem, imageMore;
        private View layoutMore;

        public PlaceHolder(View itemView) {
            super(itemView);
            textItemName = (TextView) itemView.findViewById(R.id.textItemName);
            textItemAddress = (TextView) itemView.findViewById(R.id.textItemAddress);
            textItemLocality = (TextView) itemView.findViewById(R.id.textItemLocality);
            textItemDistance = (TextView) itemView.findViewById(R.id.textItemDistance);
            imageItem = (ImageView) itemView.findViewById(R.id.imageItem);
            textItemCategory = (TextView) itemView.findViewById(R.id.textItemCategory);
            textItemPhone = (TextView) itemView.findViewById(R.id.textItemPhone);
            textItemWebsite = (TextView) itemView.findViewById(R.id.textItemWebsite);
            imageMore = (ImageView) itemView.findViewById(R.id.imageMore);
            layoutMore = itemView.findViewById(R.id.layoutMore);
        }

        public void bindFooter(){
            /** if the item is the footer, don't do anything */
        }

        public void bindPlace(Place place, double distance){
            this.place = place;
            layoutMore.setVisibility(View.GONE);
            imageMore.setRotation(0);
            textItemName.setText(place.getName());
            textItemAddress.setText(place.getAddress());
            textItemLocality.setText(place.getLocality());
            /** show the distance according to the units (KM / Miles) */
            String dist;
            if(distance > 0){
                if(isMeter){
                    distance = distance*.001;
                    dist = String.format("%1$s %2$s",
                            new DecimalFormat("#.##").format(distance),context.getResources().getString(R.string.km));
                    textItemDistance.setText(dist);
                } else{
                    distance = distance*0.000621371;
                    dist = String.format("%1$s %2$s",
                            new DecimalFormat("#.##").format(distance),context.getResources().getString(R.string.miles));
                    textItemDistance.setText(dist);
                }
            }
            imageItem.setImageResource(StaticMethods.getCategoryIconId(place.getCategoryId()));
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            textItemCategory.setText(place.getCategoryId());
            textItemPhone.setText(place.getPhone());
            textItemWebsite.setText(place.getWebsite());
            textItemPhone.setOnClickListener(this);
            textItemWebsite.setOnClickListener(this);
            imageMore.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.imageMore:
                    /** opens and closes more info in the item view through the arrow image being pressed */
                    if(layoutMore.getVisibility() == View.GONE){
                        layoutMore.setVisibility(View.VISIBLE);
                        imageMore.setRotation(180);
                    }else{
                        layoutMore.setVisibility(View.GONE);
                        imageMore.setRotation(0);
                    }
                    break;
                case R.id.textItemPhone:
                    /** implicit intent that puts the phone number in the operating system's dialer */
                    Intent phone = new Intent(Intent.ACTION_VIEW, Uri.parse("tel:"+place.getPhone()));
                    context.startActivity(phone);
                    break;
                case R.id.textItemWebsite:
                    /** implicit intent that opens the operating system's web browser with the place's website */
                    Intent website = new Intent(Intent.ACTION_VIEW, Uri.parse(place.getWebsite()));
                    context.startActivity(website);
                    break;
                default:
                    /** click on the item view itself and not one of the above, will open the place on the map, with route, if available */
                    listener.onSearchResultClick(place.getFactualId());
            }
        }


        @Override
        public boolean onLongClick(View v) {
            /** long click options */
            final Dialog dialog = new Dialog(context);

            dialog.setContentView(R.layout.dialog_item_search);
            dialog.setTitle(context.getResources().getString(R.string.dialog_item_search_title));
            dialog.findViewById(R.id.btnDialogShare).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /** share button, runs on main activity */
                    dialog.dismiss();
                    listener.sharePlace(place);
                }
            });
            dialog.findViewById(R.id.btnDialogFavorite).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /** save current place to favorites */
                    dialog.dismiss();
                    FavoritesDBHandler handler = new FavoritesDBHandler(context);
                    if (handler.checkFavoriteInDb(place.getFactualId())) {
                        /** check if place is already in the favorites db, if it is, notify the user */
                        Toast.makeText(context, context.getResources().getString(R.string.toast_place_already_in_favorites), Toast.LENGTH_SHORT).show();
                    } else {
                        /** notify the user that the place was saved and add it to the db */
                        handler.addFavorite(place);
                        Toast.makeText(context,  context.getResources().getString(R.string.toast_you_saved)+" "+ place.getName(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
            dialog.show();
            return true;
        }
    }

    public interface OnSearchResultClickListener {
        /** interface to main activity */
        void onSearchResultClick(String factualId);
        void sharePlace(Place place);
    }
}
