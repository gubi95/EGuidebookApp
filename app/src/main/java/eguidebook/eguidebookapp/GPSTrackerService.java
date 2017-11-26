package eguidebook.eguidebookapp;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;

public class GPSTrackerService extends Service implements LocationListener {
    private final Context _objContext;

    boolean _bIsGPSEnabled = false;
    boolean _bIsNetworkEnabled = false;
    boolean _bCanGetLocation = false;

    Location _objLocation;
    double _dLatitude;
    double _dLongitude;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

    protected LocationManager _objLocationManager;

    public GPSTrackerService(Context context) {
        this._objContext = context;
        getLocation();
    }

    public float calculateDistanceBetweenTwoPoints(Location objLocation1, Location objLocation2) {
        if(objLocation1 != null && objLocation2 != null) {
            return objLocation1.distanceTo(objLocation2);
        }
        else {
            return 0.0f;
        }
    }

    public String getDistanceBetweenTwoPointsAsString(Location objLocation1, Location objLocation2) {
        float fDistance = this.calculateDistanceBetweenTwoPoints(objLocation1, objLocation2);
        int nKilometers = (int) fDistance / 1000;
        int nMeters = (int) fDistance % 1000;
        return  nKilometers + "," + (nMeters / 100) + " km";
    }

    public Location getLocation() {
        if(_objContext.getResources().getBoolean(R.bool.isTestEnvironment)) {
            TypedValue outValue = new TypedValue();
            _objContext.getResources().getValue(R.dimen.defCoorX, outValue, true);
            double defCoorX = outValue.getFloat();
            _objContext.getResources().getValue(R.dimen.defCoorY, outValue, true);
            double defCoorY = outValue.getFloat();
            Location objLocation = new Location("dummy");
            objLocation.setLatitude(defCoorX);
            objLocation.setLongitude(defCoorY);
            this._bCanGetLocation = true;
            return objLocation;
        }

        try {
            _objLocationManager = (LocationManager) _objContext.getSystemService(LOCATION_SERVICE);
            _bIsGPSEnabled = _objLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            _bIsNetworkEnabled = _objLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (_bIsGPSEnabled || _bIsNetworkEnabled) {
                this._bCanGetLocation = true;

                if (_bIsNetworkEnabled &&
                        ActivityCompat.checkSelfPermission(_objContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(_objContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    _objLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (_objLocationManager != null) {
                        _objLocation = _objLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (_objLocation != null) {
                            _dLatitude = _objLocation.getLatitude();
                            _dLongitude = _objLocation.getLongitude();
                        }
                    }
                }

                if (_bIsGPSEnabled && _objLocation == null) {
                    _objLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (_objLocationManager != null) {
                        _objLocation = _objLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (_objLocation != null) {
                            _dLatitude = _objLocation.getLatitude();
                            _dLongitude = _objLocation.getLongitude();
                        }
                    }
                }
            }
            else {
                this.showSettingsAlert();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return _objLocation;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public boolean canGetLocation() {
        return this._bCanGetLocation;
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(_objContext);

        alertDialog.setTitle("Ustawienia GPS");
        alertDialog.setMessage("Usługa GPS nie jest włączona. Czy chcesz przejść do widoku ustawień?");

        alertDialog.setPositiveButton("Ustawienia", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                _objContext.startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }
}
