package eguidebook.eguidebookapp;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;
import java.util.Arrays;

public class ActivityRouteDetails extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private GoogleMap _objGoogleMap;
    private WebAPIManager.Route _objRoute = null;
    private ArrayList<WebAPIManager.GoogleMapSpot> _listGoogleMapSpot = null;
    private EnumMode _eMode = EnumMode.VIEW;

    private enum EnumMode {
        VIEW,
        EDIT,
        TRAVEL
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_details);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        _objRoute = (WebAPIManager.Route) getIntent().getSerializableExtra("Route");

        this.setFloatingActionButtons();
    }

    @Override
    public void onMapReady(GoogleMap objGoogleMap) {
        _objGoogleMap = objGoogleMap;
        this.loadMap(EnumMode.VIEW);
    }

    public void showHideProgressBar(boolean bShow) {
        findViewById(R.id.progressBar).setVisibility(bShow ? View.VISIBLE : View.GONE);
    }

    private void loadRoute(GoogleMap objGoogleMap, WebAPIManager.Route objRoute, boolean bClearMap) {
        if(bClearMap) {
            objGoogleMap.clear();
        }

        PolylineOptions objPolylineOptions = new PolylineOptions();
        objPolylineOptions.color(Color.RED);

        for(int i = 0; i < objRoute.Spots.length; i++) {
            LatLng objLatLng = new LatLng(objRoute.Spots[i].CoorX, objRoute.Spots[i].CoorY);
            objGoogleMap.addMarker(new MarkerOptions().position(objLatLng).title(objRoute.Spots[i].Name));
            if(i == 0) {
                objGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(objLatLng));
            }
            objPolylineOptions.add(objLatLng);
        }

        objGoogleMap.addPolyline(objPolylineOptions);
        objGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(13.0f));
    }

    private void setFloatingActionButtons() {
        findViewById(R.id.fab_edit_route).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(_eMode == EnumMode.VIEW) {
                    _eMode = EnumMode.EDIT;
                }
                else {
                    _eMode = EnumMode.VIEW;
                }

                loadMap(_eMode);
            }
        });
    }

    public void loadMap(EnumMode eMode) {
        if(eMode == EnumMode.VIEW) {
            ((TextView) findViewById(R.id.tv_route_mode_name)).setText("Podgląd");
            ((FloatingActionButton) findViewById(R.id.fab_edit_route)).setImageResource(R.drawable.ic_edit_black_24dp);
            this.loadRoute(_objGoogleMap, _objRoute, true);
        }
        else if(eMode == EnumMode.EDIT) {
            ((TextView) findViewById(R.id.tv_route_mode_name)).setText("Edycja");
            ((FloatingActionButton) findViewById(R.id.fab_edit_route)).setImageResource(R.drawable.ic_check_black_24dp);
            loadMapForEditMode();
        }
        else {
            ((TextView)findViewById(R.id.tv_route_mode_name)).setText("Zwiedzanie");
        }
    }

    private void loadMapForEditMode() {
        if(_listGoogleMapSpot == null || _listGoogleMapSpot.size() == 0) {
            showHideProgressBar(true);
            new AsyncTask<Void, Void, WebAPIManager.GetForGoogleMapReply>() {
                @Override
                protected WebAPIManager.GetForGoogleMapReply doInBackground(Void... voids) {
                    return new WebAPIManager().getForGoogleMap();
                }

                @Override
                protected void onPostExecute(WebAPIManager.GetForGoogleMapReply objGetForGoogleMapReply) {
                    if(objGetForGoogleMapReply != null && objGetForGoogleMapReply.isSuccess() && objGetForGoogleMapReply.Spots != null) {
                        _listGoogleMapSpot = new ArrayList<>(Arrays.asList(objGetForGoogleMapReply.Spots));
                    }
                    else {
                        _listGoogleMapSpot = new ArrayList<>();
                    }
                    showHideProgressBar(false);
                    loadAllSpotsInGoogleMap(_objGoogleMap, _objRoute);
                }
            }.execute();
        }
        else {
            loadAllSpotsInGoogleMap(_objGoogleMap, _objRoute);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private void loadAllSpotsInGoogleMap(GoogleMap objGoogleMap, WebAPIManager.Route objRoute) {
        objGoogleMap.clear();

        for(WebAPIManager.GoogleMapSpot objGoogleMapSpot : _listGoogleMapSpot) {
            boolean bSpotFound = false;
            for(WebAPIManager.RouteSpot objRouteSpot : objRoute.Spots) {
                if(objRouteSpot.SpotID.equals(objGoogleMapSpot.SpotID)) {
                    bSpotFound = true;
                    break;
                }
            }

            if(!bSpotFound) {
                LatLng objLatLng = new LatLng(objGoogleMapSpot.CoorX, objGoogleMapSpot.CoorY);
                objGoogleMap.addMarker(new MarkerOptions().position(objLatLng).title(objGoogleMapSpot.Name));
            }
        }

        this.loadRoute(objGoogleMap, objRoute, false);
    }
}
