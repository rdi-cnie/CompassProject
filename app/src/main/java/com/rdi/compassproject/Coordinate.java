package com.rdi.compassproject;

/**
 * Created by rudi on 31-12-2016.
 */

public class Coordinate {
    private final double latitude;
    private final double longitude;

    public Coordinate(){
        this(0,0);
    }

    public Coordinate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return "Coordinate{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
