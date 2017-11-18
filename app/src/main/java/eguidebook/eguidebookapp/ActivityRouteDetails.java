package eguidebook.eguidebookapp;

import android.graphics.Color;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class ActivityRouteDetails extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap _objGoogleMap;
    private WebAPIManager.Route _objRoute = null;
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

    private void loadRoute(GoogleMap objGoogleMap, WebAPIManager.Route objRoute) {
        objGoogleMap.clear();

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
            this.loadRoute(_objGoogleMap, _objRoute);
        }
        else if(eMode == EnumMode.EDIT) {
            ((TextView) findViewById(R.id.tv_route_mode_name)).setText("Edycja");
            ((FloatingActionButton) findViewById(R.id.fab_edit_route)).setImageResource(R.drawable.ic_check_black_24dp);
        }
        else {
            ((TextView)findViewById(R.id.tv_route_mode_name)).setText("Zwiedzanie");
        }
    }
}
