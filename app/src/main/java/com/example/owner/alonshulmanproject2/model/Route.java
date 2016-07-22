package com.example.owner.alonshulmanproject2.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by Owner on 06/04/2016.
 */
public class Route implements Parcelable {
    private String distance, duration, encPolyline;
    private LatLngBounds bounds;

    public Route(String distance, String duration, String encPolyline, LatLngBounds bounds) {
        this.distance = distance;
        this.duration = duration;
        this.encPolyline = encPolyline;
        this.bounds = bounds;
    }

    protected Route(Parcel in) {
        distance = in.readString();
        duration = in.readString();
        encPolyline = in.readString();
        bounds = in.readParcelable(LatLngBounds.class.getClassLoader());
    }

    public static final Creator<Route> CREATOR = new Creator<Route>() {
        @Override
        public Route createFromParcel(Parcel in) {
            return new Route(in);
        }

        @Override
        public Route[] newArray(int size) {
            return new Route[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(distance);
        dest.writeString(duration);
        dest.writeString(encPolyline);
        dest.writeParcelable(bounds, flags);
    }

    public String getDistance() {
        return distance;
    }

    public String getDuration() {
        return duration;
    }

    public String getEncPolyline() {
        return encPolyline;
    }

    public LatLngBounds getBounds() {
        return bounds;
    }
}
