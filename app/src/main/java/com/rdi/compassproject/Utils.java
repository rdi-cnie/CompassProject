package com.rdi.compassproject;

public final class Utils {

    public static double calculateBearing(Coordinate src, Coordinate dst) {
        double srcLat = Math.toRadians(src.getLatitude());
        double dstLat = Math.toRadians(dst.getLatitude());
        double dLng = Math.toRadians(dst.getLongitude() - src.getLongitude());
        return Math.toDegrees( Math.atan2(Math.sin(dLng) * Math.cos(dstLat), Math.cos(srcLat) * Math.sin(dstLat) - Math.sin(srcLat) * Math.cos(dstLat) * Math.cos(dLng)) );
    }

}
