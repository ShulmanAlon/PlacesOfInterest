package com.example.owner.alonshulmanproject2.adapters;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
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
import java.util.ArrayList;

/**
 * Created by Owner on 30/03/2016.
 */
public class FavoritesRecyclerAdapter extends RecyclerView.Adapter<FavoritesRecyclerAdapter.FavoriteHolder>{
    private Context context;
    private ArrayList<Place> places;
    private FavoritesDBHandler handler;
    public static final String ACTION_FAVS_CLEAR = "com.example.owner.alonshulmanproject2.adapters.ACTION_FAVS_CLEAR";
    private OnFavoriteResultClickListener listener;
    private static final int VIEW_NORMAL = 1;
    private static final int VIEW_FOOTER = 2;

    public FavoritesRecyclerAdapter(Context context) {
        this.context = context;
        /** initialize CTor */
        listener = (OnFavoriteResultClickListener) context;
        places = new ArrayList<>();
        handler = new FavoritesDBHandler(context);
        /** register receiver for favorites cleared */
        FavoritesCleared receiver = new FavoritesCleared();
        IntentFilter filter = new IntentFilter(ACTION_FAVS_CLEAR);
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver,filter);
        /** start loading the favorites */
        setFavorites();
    }

    public class FavoritesCleared extends BroadcastReceiver{
        /** when receiving that the favorites were cleared, load the favorites again (obviously empty) */
        @Override
        public void onReceive(Context context, Intent intent) {
            setFavorites();
        }
    }

    public void setFavorites(){
        /** get favorites from db and set them to the adapter */
        places.clear();
        places = handler.getAllFavorites();
        notifyDataSetChanged();
    }

    public void deleteFavorite(String factualId, int position){
        /** delete a specific place from favorites, then notify the adapter */
        handler.deleteFavorite(factualId);
        places.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public FavoriteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        /** two view types, normal view, and footer - which is a blank spacer so that the buttons wouldn't block any info from the last item */
        LayoutInflater inflater = LayoutInflater.from(context);
        View v;
        if(viewType == VIEW_NORMAL){
            v = inflater.inflate(R.layout.search_list_item,parent,false);
        } else{
            v = inflater.inflate(R.layout.footer,parent,false);
        }
        return new FavoriteHolder(v);
    }

    @Override
    public void onBindViewHolder(FavoriteHolder holder, int position) {
        /** if the position is the size of the array (which means +1), it is the footer */
        if(position != places.size()){
            holder.bindFavorite(places.get(position));
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

    public class FavoriteHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView textItemName,textItemAddress,textItemLocality, textItemCategory, textItemPhone, textItemWebsite;
        private Place place;
        private ImageView imageItem, imageMore;
        private View layoutMore;

        public FavoriteHolder(View itemView) {
            super(itemView);
            textItemName = (TextView) itemView.findViewById(R.id.textItemName);
            textItemAddress = (TextView) itemView.findViewById(R.id.textItemAddress);
            textItemLocality = (TextView) itemView.findViewById(R.id.textItemLocality);
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

        public void bindFavorite(Place place){
            this.place = place;
            imageMore.setRotation(0);
            textItemName.setText(place.getName());
            textItemAddress.setText(place.getAddress());
            textItemLocality.setText(place.getLocality());
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
                    listener.onFavoriteResultClick(place.getFactualId());
            }
        }

        public boolean onLongClick(View v) {
            /** long click options */
            final Dialog dialog = new Dialog(context);

            dialog.setContentView(R.layout.dialog_item_favorites);
            dialog.setTitle(context.getResources().getString(R.string.dialog_item_search_title));
            dialog.findViewById(R.id.btnDialogShare).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /** share button, runs on main activity */
                    dialog.dismiss();
                    listener.sharePlace(place);
                }
            });
            dialog.findViewById(R.id.btnDialogDelete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /** delete current place from favorites, notify the user and update the adapter */
                    dialog.dismiss();
                    Toast.makeText(context, context.getResources().getString(R.string.toast_you_removed)+" "+place.getName(), Toast.LENGTH_SHORT).show();
                    deleteFavorite(place.getFactualId(),getAdapterPosition());
                }
            });
            dialog.show();
            return true;
        }
    }

    public interface OnFavoriteResultClickListener {
        /** interface to main activity */
        void onFavoriteResultClick(String factualId);
        void sharePlace(Place place);
    }
}
