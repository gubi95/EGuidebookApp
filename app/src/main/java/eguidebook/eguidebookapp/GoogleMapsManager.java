package eguidebook.eguidebookapp;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

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
}
