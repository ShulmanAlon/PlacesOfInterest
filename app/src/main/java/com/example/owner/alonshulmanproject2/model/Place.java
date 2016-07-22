package com.example.owner.alonshulmanproject2.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Owner on 27/03/2016.
 */
public class Place implements Parcelable {
    private String name, address, locality, factualId, phone, website, categoryId;
    private LatLng latLng;

    public Place(String factualId, String name, String address, String locality, String categoryId, LatLng latLng, String phone, String website) {
        this.factualId = factualId;
        this.name = name;
        this.address = address;
        this.locality = locality;
        this.categoryId = categoryId;
        this.latLng = latLng;
        this.phone = phone;
        this.website = website;
    }

    protected Place(Parcel in) {
        name = in.readString();
        address = in.readString();
        locality = in.readString();
        factualId = in.readString();
        phone = in.readString();
        website = in.readString();
        categoryId = in.readString();
        latLng = in.readParcelable(LatLng.class.getClassLoader());
    }

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(locality);
        dest.writeString(factualId);
        dest.writeString(phone);
        dest.writeString(website);
        dest.writeString(categoryId);
        dest.writeParcelable(latLng, flags);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getFactualId() {
        return factualId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
}
