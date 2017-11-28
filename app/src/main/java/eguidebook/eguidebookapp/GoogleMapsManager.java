package eguidebook.eguidebookapp;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class GoogleMapsManager {

    public static String getSummedDistanceBetweenPoints(WebAPIManager.Route objRoute) {
        double dTotalDistance = 0.0;

        if(objRoute != null && objRoute.Spots != null && objRoute.Spots.length >= 2) {
            for(int i = 0; i < objRoute.Spots.length - 1; i++) {
                LatLng objLatLng1 = new LatLng(objRoute.Spots[i].CoorX, objRoute.Spots[i].CoorY);
                LatLng objLatLng2 = new LatLng(objRoute.Spots[i + 1].CoorX, objRoute.Spots[i + 1].CoorY);
                dTotalDistance += SphericalUtil.computeDistanceBetween(objLatLng1, objLatLng2);
            }
        }

        return String.format("%.1f", (dTotalDistance / (double) 1000)).replace(".", ",") + " km";
    }

    public static String getCityNameByCoordinates(Context objContext, double dCoorX, double dCoorY) {
        try {
            Geocoder objGeocoder = new Geocoder(objContext, Locale.getDefault());
            List<Address> listAddress = objGeocoder.getFromLocation(dCoorX, dCoorY, 1);
            if (listAddress.size() > 0) {
                return listAddress.get(0).getLocality();
            }
        }
        catch (Exception ex) { }
        return "";
    }

    public class GetDirectionsToPointReply {
        public GoogleRoute[] routes;
    }

    public class GoogleRoute {
        public Leg[] legs;
    }

    public class Leg {
        public Distance distance;
        public Duration duration;
        public Step[] steps;
    }

    public class Distance {
        public String text;
        public int value;
    }

    public class Duration {
        public String text;
    }

    public class Step {
        public Distance distance;
        public Duration duration;
        public EndLocation end_location;
    }

    public class EndLocation {
        public double lat;
        public double lng;
    }

    public static ArrayList<Step> getDirectionsToPoint(double dCoorXFrom, double dCoorYFrom, double dCoorXTo, double dCoorYTo) {
        try {
            String url = "https://maps.googleapis.com/maps/api/directions/json?mode=walking&sensor=false&origin=" + dCoorXFrom + "," + dCoorYFrom + "&destination=" + dCoorXTo + "," + dCoorYTo;

            // sample
            // https://maps.googleapis.com/maps/api/directions/json?sensor=false&origin=51.1127200,17.0606494&destination=51.1189209,16.9895224

            GetDirectionsToPointReply objGetDirectionsToPointReply = new Gson().fromJson(new WebAPIManager().downloadString(url), GetDirectionsToPointReply.class);

            if (objGetDirectionsToPointReply != null &&
                    objGetDirectionsToPointReply.routes.length > 0 &&
                    objGetDirectionsToPointReply.routes[0].legs.length > 0) {
                return new ArrayList<>(Arrays.asList(objGetDirectionsToPointReply.routes[0].legs[0].steps));
            }
        }
        catch (Exception ex) { }
        return new ArrayList<>();
    }

    public static boolean isLocationNearToAnotherLocation(double dCoorXFrom, double dCoorYFrom, double dCoorXTo, double dCoorYTo, int nMaxDistanceBetweenLocationsInMeters) {
        return SphericalUtil.computeDistanceBetween(new LatLng(dCoorXFrom, dCoorYFrom), new LatLng(dCoorXTo, dCoorYTo)) <= nMaxDistanceBetweenLocationsInMeters;
    }
}
