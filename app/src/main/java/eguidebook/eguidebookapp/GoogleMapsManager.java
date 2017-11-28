package eguidebook.eguidebookapp;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

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
}
