package eguidebook.eguidebookapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import mehdi.sakout.fancybuttons.FancyButton;

public class ActivityRouteDetails extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private GoogleMap _objGoogleMap;
    private WebAPIManager.Route _objRoute = null;
    private ArrayList<WebAPIManager.GoogleMapSpot> _listGoogleMapSpot = null;
    private HashMap<String, Marker> _dictSpotIDToMapMarker = null;
    private ArrayList<Marker> _listMarkerSearched = null;
    private EnumMode _eMode = EnumMode.VIEW;
    private boolean _bIsSearchBarVisible = false;
    private boolean _bIsSpotListVisible = false;
    private Polyline _objPolyline = null;
    private Polyline _objPolylineTravelToNextPoint = null;
    private Handler _objHandlerTravelling = null;
    private Marker _objMarkerCurrentLocation = null;
    private FragmentSpotDetails _objFragmentSpotDetails = null;

    private enum EnumMode {
        VIEW,
        CREATE,
        EDIT,
        TRAVEL
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_details);

        try {
            this._objRoute = (WebAPIManager.Route) getIntent().getSerializableExtra("Route");
        }
        catch (Exception ex) {
            _objRoute = null;
        }

        if(this._objRoute == null) {
            this._objRoute = new WebAPIManager.Route();
            this._objRoute.Spots = new WebAPIManager.RouteSpot[]{ };
            this._objRoute.Name = "";
            this._objRoute.Description = "";
            this._objRoute.IsSystemRoute = false;
            this._eMode = EnumMode.CREATE;
        }
        else {
            this._eMode = EnumMode.VIEW;
        }

        findViewById(R.id.fab_delete_route).setVisibility(this._objRoute.IsSystemRoute ? View.GONE : View.VISIBLE);
        findViewById(R.id.fab_edit_route).setVisibility(this._objRoute.IsSystemRoute ? View.GONE : View.VISIBLE);

        this._dictSpotIDToMapMarker = new HashMap<>();
        this._listMarkerSearched = new ArrayList<>();

        this.setViewSpotListButton();
        this.setSearchBarButton();
        this.setFloatingActionButtons();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onBackPressed() {
        if(this._bIsSearchBarVisible) {
            this.hideSearchBar();
        }
        else if(this._bIsSpotListVisible) {
            this.hideSpotList();
        }
        else if(this._objFragmentSpotDetails != null && this._objFragmentSpotDetails.isVisible()) {
            findViewById(R.id.main_content).setVisibility(View.GONE);
            findViewById(R.id.fab_delete_route).setVisibility(View.VISIBLE);
            findViewById(R.id.fab_start_travel).setVisibility(View.VISIBLE);
            this._bIsTravellingPaused = false;
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public void onMapReady(GoogleMap objGoogleMap) {
        _objGoogleMap = objGoogleMap;
        _objGoogleMap.setOnMarkerClickListener(this);
        this.loadMap(_eMode);
    }

    public void showHideProgressBar(boolean bShow) {
        try {
            findViewById(R.id.progressBar).setVisibility(bShow ? View.VISIBLE : View.GONE);
        }
        catch (Exception ex) { }
    }

    private void loadRoute(GoogleMap objGoogleMap, WebAPIManager.Route objRoute, boolean bClearMap) {
        if(bClearMap) {
            _dictSpotIDToMapMarker.clear();
            objGoogleMap.clear();
        }

        PolylineOptions objPolylineOptions = new PolylineOptions();
        objPolylineOptions.color(Color.RED);

        for(int i = 0; i < objRoute.Spots.length; i++) {
            LatLng objLatLng = new LatLng(objRoute.Spots[i].CoorX, objRoute.Spots[i].CoorY);
            Marker objMarker = objGoogleMap.addMarker(new MarkerOptions().position(objLatLng).title(objRoute.Spots[i].Name));
            if(i == 0) {
                objGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(objLatLng));
            }
            objPolylineOptions.add(objLatLng);

            _dictSpotIDToMapMarker.put(objRoute.Spots[i].SpotID, objMarker);
        }

        _objPolyline = objGoogleMap.addPolyline(objPolylineOptions);
        objGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(13.0f));
    }

    private void addRouteSpotToRoute(WebAPIManager.Route objRoute, WebAPIManager.RouteSpot objRouteSpotNew) {
        boolean bCanAdd = true;

        for(WebAPIManager.RouteSpot objRouteSpot : objRoute.Spots) {
            if(objRouteSpot.SpotID.equals(objRouteSpotNew.SpotID)) {
                bCanAdd = false;
            }
        }

        if(bCanAdd) {
            ArrayList<WebAPIManager.RouteSpot> listRouteSpot = new ArrayList<>(Arrays.asList(objRoute.Spots));
            listRouteSpot.add(objRouteSpotNew);
            objRoute.Spots = listRouteSpot.toArray(new WebAPIManager.RouteSpot[listRouteSpot.size()]);
            List<LatLng> listLatLng = _objPolyline.getPoints();
            listLatLng.add(new LatLng(objRouteSpotNew.CoorX, objRouteSpotNew.CoorY));
            _objPolyline.setPoints(listLatLng);
        }
    }

    private void removeRouteSpotFromRoute(WebAPIManager.Route objRoute, int nPolylineSpotIndex, String strSpotID) {
        int nRouteSpotIndexInRoute = -1;

        int nIndex = 0;
        for(WebAPIManager.RouteSpot objRouteSpot : objRoute.Spots) {
            if(objRouteSpot.SpotID.equals(strSpotID)) {
                nRouteSpotIndexInRoute = nIndex;
                break;
            }
            nIndex++;
        }

        if(nRouteSpotIndexInRoute != -1) {
            List<WebAPIManager.RouteSpot> listRouteSpot = new ArrayList<>(Arrays.asList(objRoute.Spots));
            listRouteSpot.remove(nRouteSpotIndexInRoute);
            objRoute.Spots = listRouteSpot.toArray(new WebAPIManager.RouteSpot[listRouteSpot.size()]);
        }

        List<LatLng> listLatLng = _objPolyline.getPoints();
        listLatLng.remove(nPolylineSpotIndex);
        _objPolyline.setPoints(listLatLng);
    }

    private int getSpotPolylineIndex(double dLat, double dLng) {
        int nIndex = 0;
        for(LatLng objLatLng : this._objPolyline.getPoints()) {
            if(objLatLng.latitude == dLat && objLatLng.longitude == dLng) {
                return nIndex;
            }
            nIndex++;
        }
        return -1;
    }

    private void setFloatingActionButtons() {
        findViewById(R.id.fab_edit_route).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean bLoadMap = false;

                if(_eMode == EnumMode.VIEW) {
                    _eMode = EnumMode.EDIT;
                    bLoadMap = true;
                }
                else if(_eMode == EnumMode.CREATE || _eMode == EnumMode.EDIT) {
                    openRouteNameDialog(new IOnAcceptRouteName() {
                        @Override
                        public void doAction(String strRouteName) {
                            _objRoute.Name = strRouteName;
                            saveRoute(_objRoute, _eMode == EnumMode.CREATE, new ISaveRouteSuccessCallback() {
                                @Override
                                public void doAction() {
                                    _eMode = EnumMode.VIEW;
                                    loadMap(_eMode);
                                }
                            });

                        }
                    });
                }

                if(bLoadMap) {
                    loadMap(_eMode);
                }
            }
        });

        findViewById(R.id.fab_delete_route).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDeleteRouteDialog(new IDeleteRouteAcceptCallback() {
                    @Override
                    public void doAction() {
                        deleteRoute(_objRoute.RouteID);
                    }
                });
            }
        });

        findViewById(R.id.fab_start_travel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(_eMode == EnumMode.TRAVEL) {
                    _eMode = EnumMode.VIEW;
                    stopTravelling();
                }
                else {
                    _eMode = EnumMode.TRAVEL;
                }
                loadMap(_eMode);
            }
        });
    }

    private void setSearchBarButton() {
        findViewById(R.id.iv_route_details_search_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 if(_bIsSearchBarVisible) {
                     hideSearchBar();
                 }
                 else {
                     hideSpotList();
                     showSearchBar();
                 }
            }
        });

        ((EditText) findViewById(R.id.et_route_search)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int nActionID, KeyEvent keyEvent) {
                if(nActionID == EditorInfo.IME_ACTION_SEARCH) {
                    filterSpots(textView.getText().toString(), _objGoogleMap);
                    return true;
                }
                return false;
            }
        });
    }

    private void setViewSpotListButton() {
        findViewById(R.id.iv_route_details_view_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(_bIsSpotListVisible) {
                    hideSpotList();
                }
                else {
                    hideSearchBar();
                    showSpotList();
                }
            }
        });
    }

    private void showSearchBar() {
        _bIsSearchBarVisible = true;
        findViewById(R.id.ll_route_search_panel).setVisibility(View.VISIBLE);
        EditText etSearchSpot = findViewById(R.id.et_route_search);
        etSearchSpot.requestFocus();
        try {
            InputMethodManager objInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            objInputMethodManager.showSoftInput(etSearchSpot, InputMethodManager.SHOW_IMPLICIT);
        }
        catch (Exception ex) { }
        this.unsearchMarkers(_listMarkerSearched);
    }

    private void hideSearchBar() {
        _bIsSearchBarVisible = false;
        PLHelpers.hideKeyboard(this);
        findViewById(R.id.ll_route_search_panel).setVisibility(View.GONE);
        ((EditText) findViewById(R.id.et_route_search)).setText("");
        findViewById(R.id.et_route_search).clearFocus();
        this.unsearchMarkers(_listMarkerSearched);
    }

    private void showSpotList() {
        _bIsSpotListVisible = true;
        this.loadRouteSpotList();
        findViewById(R.id.ll_route_spot_list_panel).setVisibility(View.VISIBLE);
        findViewById(R.id.fab_edit_route).setVisibility(_eMode == EnumMode.VIEW || _objRoute.IsSystemRoute ? View.GONE : View.VISIBLE);
    }

    private void hideSpotList() {
        _bIsSpotListVisible = false;
        findViewById(R.id.ll_route_spot_list_panel).setVisibility(View.GONE);
        findViewById(R.id.fab_edit_route).setVisibility(_objRoute.IsSystemRoute ? View.GONE : View.VISIBLE);
    }

    public void loadMap(EnumMode eMode) {
        this.hideSearchBar();
        findViewById(R.id.fab_edit_route).setEnabled(true);
        ((FloatingActionButton) findViewById(R.id.fab_start_travel)).setImageResource(R.drawable.ic_directions_run_black_24dp);
        if(eMode == EnumMode.VIEW) {
            ((TextView) findViewById(R.id.tv_route_mode_name)).setText("Podgląd");
            findViewById(R.id.iv_route_details_search_icon).setVisibility(View.GONE);
            ((FloatingActionButton) findViewById(R.id.fab_edit_route)).setImageResource(R.drawable.ic_edit_black_24dp);
            this.loadRoute(_objGoogleMap, _objRoute, true);
        }
        else if(eMode == EnumMode.CREATE) {
            ((TextView) findViewById(R.id.tv_route_mode_name)).setText("Nowa trasa");
            findViewById(R.id.iv_route_details_search_icon).setVisibility(View.VISIBLE);
            ((FloatingActionButton) findViewById(R.id.fab_edit_route)).setImageResource(R.drawable.ic_check_black_24dp);
            this.loadMapForEditMode();
            Location objCurrentLocation = EGuidebookApplication.mGPSTrackerService.getLocation();
            _objGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(objCurrentLocation.getLatitude(), objCurrentLocation.getLongitude())));

        }
        else if(eMode == EnumMode.EDIT) {
            ((TextView) findViewById(R.id.tv_route_mode_name)).setText("Edycja");
            findViewById(R.id.iv_route_details_search_icon).setVisibility(View.VISIBLE);
            ((FloatingActionButton) findViewById(R.id.fab_edit_route)).setImageResource(R.drawable.ic_check_black_24dp);
            this.loadMapForEditMode();
        }
        else if(eMode == EnumMode.TRAVEL) {
            ((TextView)findViewById(R.id.tv_route_mode_name)).setText("Zwiedzanie");
            findViewById(R.id.iv_route_details_search_icon).setVisibility(View.GONE);
            ((FloatingActionButton) findViewById(R.id.fab_start_travel)).setImageResource(R.drawable.ic_location_disabled_black_24dp);
            findViewById(R.id.fab_edit_route).setEnabled(false);
            startTraveling();
        }
    }

    @SuppressLint("StaticFieldLeak")
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
    public boolean onMarkerClick(Marker objMarker) {
        if(_eMode == EnumMode.CREATE || _eMode == EnumMode.EDIT) {
            for (Map.Entry<String, Marker> objMapEntry : this._dictSpotIDToMapMarker.entrySet()) {
                if (objMapEntry.getValue().equals(objMarker)) {
                    String strSpotID = objMapEntry.getKey();
                    for (WebAPIManager.GoogleMapSpot objGoogleMapSpot : this._listGoogleMapSpot) {
                        if (objGoogleMapSpot.SpotID.equals(strSpotID)) {
                            int nPolylineIndex = this.getSpotPolylineIndex(objGoogleMapSpot.CoorX, objGoogleMapSpot.CoorY);
                            if(nPolylineIndex == -1) {
                                this.addRouteSpotToRoute(_objRoute, new WebAPIManager.RouteSpot(objGoogleMapSpot));
                            }
                            else {
                                this.removeRouteSpotFromRoute(_objRoute, nPolylineIndex, strSpotID);
                            }
                            break;
                        }
                    }
                }
            }
        }

        return false;
    }

    private void loadAllSpotsInGoogleMap(GoogleMap objGoogleMap, WebAPIManager.Route objRoute) {
        objGoogleMap.clear();
        _dictSpotIDToMapMarker.clear();

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
                Marker objMarker = objGoogleMap.addMarker(new MarkerOptions().position(objLatLng).title(objGoogleMapSpot.Name));
                _dictSpotIDToMapMarker.put(objGoogleMapSpot.SpotID, objMarker);
            }
        }

        this.loadRoute(objGoogleMap, objRoute, false);
    }

    private void loadRouteSpotList() {
        RecyclerView objRecyclerView = findViewById(R.id.rv_route_details_route_list);
        objRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        objRecyclerView.setAdapter(new SpotListRecyclerViewAdapter(_objRoute));
    }

    private class SpotListRecyclerViewAdapter extends RecyclerView.Adapter<SpotListRecyclerViewAdapter.SpotViewHolder> {
        private ArrayList<String> _listSpotIDsToDelete = null;


        public SpotListRecyclerViewAdapter(WebAPIManager.Route objRoute) {
            this._listSpotIDsToDelete = new ArrayList<>();
        }

        public class SpotViewHolder extends RecyclerView.ViewHolder {
            private TextView _tvSpotName = null;
            private ImageView _ivDeleteSpot = null;

            public TextView getSpotNameTextView() {
                return this._tvSpotName;
            }

            public ImageView getSpotDeleteImageView() {
                return this._ivDeleteSpot;
            }

            public SpotViewHolder(View objView) {
                super(objView);
                this._tvSpotName = objView.findViewById(R.id.tv_route_activity_spot_list_item_template_spot_name);
                this._ivDeleteSpot = objView.findViewById(R.id.iv_route_activity_spot_list_item_template_spot_delete);
            }
        }

        @Override
        public SpotViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new SpotViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_route_spot_list_item_template, parent, false));
        }

        @Override
        public void onBindViewHolder(final SpotViewHolder objSpotViewHolder, int position) {
            final WebAPIManager.RouteSpot objRouteSpot = _objRoute.Spots[position];

            objSpotViewHolder.getSpotNameTextView().setText(String.valueOf(position + 1) + ". " + objRouteSpot.Name);

            if(_eMode == EnumMode.VIEW) {
                objSpotViewHolder.getSpotDeleteImageView().setVisibility(View.GONE);
            }
            else {
                objSpotViewHolder.getSpotDeleteImageView().setVisibility(View.VISIBLE);
                objSpotViewHolder.getSpotDeleteImageView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int nPolylineIndex = getSpotPolylineIndex(objRouteSpot.CoorX, objRouteSpot.CoorY);
                        if(nPolylineIndex != -1) {
                            removeRouteSpotFromRoute(_objRoute, nPolylineIndex, objRouteSpot.SpotID);
                        }

                        notifyDataSetChanged();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return _objRoute.Spots.length;
        }
    }

    private boolean isRouteValid(WebAPIManager.Route objRoute, boolean bMakeToasts) {
        if(PLHelpers.stringIsNullOrEmpty(objRoute.Name)) {
            if(bMakeToasts) {
                Toast.makeText(getApplicationContext(), "Proszę wpisać nazwę miejsca", Toast.LENGTH_LONG).show();
            }
            return false;
        }

        if(objRoute.Spots == null || objRoute.Spots.length < 2) {
            if(bMakeToasts) {
                Toast.makeText(getApplicationContext(), "Trasa musi zawierać conajmniej 2 miejsca", Toast.LENGTH_LONG).show();
            }
            return false;
        }

        return true;
    }

    private interface ISaveRouteSuccessCallback {
        void doAction();
    }

    @SuppressLint("StaticFieldLeak")
    private void saveRoute(final WebAPIManager.Route objRoute, final boolean bCreateNewRoute, final ISaveRouteSuccessCallback objISaveRouteSuccessCallback) {
        if(!this.isRouteValid(objRoute, true)) {
            return;
        }

        if(this._bIsSearchBarVisible) {
            this.hideSearchBar();
        }

        if (this._bIsSpotListVisible) {
            this.hideSpotList();
        }

        this.showHideProgressBar(true);
        new AsyncTask<Void, Void, WebAPIManager.WebAPIReply>() {
            @Override
            protected WebAPIManager.WebAPIReply doInBackground(Void... voids) {
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) { }

                try {
                    ArrayList<String> listSpotIDs = new ArrayList<>();

                    for (WebAPIManager.RouteSpot objRouteSpot : _objRoute.Spots) {
                        listSpotIDs.add(objRouteSpot.SpotID);
                    }

                    String[] arrSpotIDs = listSpotIDs.toArray(new String[listSpotIDs.size()]);

                    if (bCreateNewRoute) {
                        return new WebAPIManager().createRoute(new WebAPIManager.CreateRoutePostData(_objRoute.Name, _objRoute.Description, arrSpotIDs));
                    } else {
                        return new WebAPIManager().updateRoute(new WebAPIManager.EditRoutePostData(_objRoute.RouteID, _objRoute.Name, _objRoute.Description, arrSpotIDs));
                    }
                }
                catch (Exception ex) { }
                return null;
            }

            @Override
            protected void onPostExecute(WebAPIManager.WebAPIReply objWebAPIReply) {
                if(objWebAPIReply != null && objWebAPIReply.isSuccess()) {
                    if(objWebAPIReply instanceof WebAPIManager.CreateRouteReply) {
                        _objRoute.RouteID = ((WebAPIManager.CreateRouteReply) objWebAPIReply).RouteID;
                    }

                    Toast.makeText(getApplicationContext(), "Trasa została zapisana!", Toast.LENGTH_LONG).show();

                    if(objISaveRouteSuccessCallback != null) {
                        objISaveRouteSuccessCallback.doAction();
                    }
                }
                showHideProgressBar(false);
            }
        }.execute();
    }

    private interface IOnAcceptRouteName {
        void doAction(String strRouteName);
    }

    private void openRouteNameDialog(final IOnAcceptRouteName objIOnAcceptRouteName) {
        AlertDialog.Builder objAlertDialogBuilder = new AlertDialog.Builder(this);

        final EditText etRouteName = new EditText(getApplicationContext());
        etRouteName.setText(_objRoute.Name);
        etRouteName.setSingleLine();

        FrameLayout objFrameLayout = new FrameLayout(this);
        FrameLayout.LayoutParams objLayoutParams = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        objLayoutParams.leftMargin = PLHelpers.DPtoPX(20, getApplicationContext());
        objLayoutParams.rightMargin = PLHelpers.DPtoPX(20, getApplicationContext());
        etRouteName.setLayoutParams(objLayoutParams);
        objFrameLayout.addView(etRouteName);

        objAlertDialogBuilder.setTitle("Nazwa trasy");
        objAlertDialogBuilder.setView(objFrameLayout);

        objAlertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) { }
        });

        objAlertDialogBuilder.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) { }
        });

        final AlertDialog objAlertDialog = objAlertDialogBuilder.show();

        objAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strRouteName = etRouteName.getText().toString();

                if(PLHelpers.stringIsNullOrEmpty(strRouteName)) {
                    Toast.makeText(getApplicationContext(), "Proszę wpisać nazwę miejsca", Toast.LENGTH_LONG).show();
                }
                else if(objIOnAcceptRouteName != null) {
                    objAlertDialog.cancel();
                    objIOnAcceptRouteName.doAction(strRouteName);
                }
            }
        });
    }

    private void unsearchMarkers(ArrayList<Marker> listMarker) {
        for(int i = 0; i < listMarker.size(); i++) {
            listMarker.get(i).setIcon(BitmapDescriptorFactory.defaultMarker());
        }
        listMarker.clear();
    }

    private void filterSpots(String strName, GoogleMap objGoogleMap) {
        this.unsearchMarkers(_listMarkerSearched);

        String strNameFormatted = strName != null ? strName.toLowerCase().trim() : "";
        BitmapDescriptor objBitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.search_google_map_pin);

        boolean bFountAtLeastOne = false;
        for (WebAPIManager.GoogleMapSpot objGoogleMapSpot : _listGoogleMapSpot) {
            if(objGoogleMapSpot.Name.trim().toLowerCase().contains(strNameFormatted)) {
                Marker objMarkerToSelect = this._dictSpotIDToMapMarker.get(objGoogleMapSpot.SpotID);
                if(objMarkerToSelect != null) {
                    objMarkerToSelect.setIcon(objBitmapDescriptor);

                    if(!bFountAtLeastOne) {
                        objGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(objGoogleMapSpot.CoorX, objGoogleMapSpot.CoorY)));
                        objMarkerToSelect.showInfoWindow();
                    }

                    bFountAtLeastOne = true;

                    _listMarkerSearched.add(objMarkerToSelect);
                }
            }
        }

        if(!bFountAtLeastOne) {
            Toast.makeText(getApplicationContext(), "Brak miejsc o zadanych kryteriach", Toast.LENGTH_LONG).show();
        }
        else {
            PLHelpers.hideKeyboard(this);
        }
    }

    private interface IDeleteRouteAcceptCallback {
        void doAction();
    }

    private void openDeleteRouteDialog(final IDeleteRouteAcceptCallback objIDeleteRouteAcceptCallback) {
        new AlertDialog.Builder(this)
            .setMessage("Czy na pewno chcesz usunąć trasę?")
            .setPositiveButton("Tak", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (objIDeleteRouteAcceptCallback != null) {
                        objIDeleteRouteAcceptCallback.doAction();
                    }
                }
            })
            .setNegativeButton("Nie", null)
            .show();
    }

    @SuppressLint("StaticFieldLeak")
    private void deleteRoute(final String strRouteID) {
        this.showHideProgressBar(true);

        if(this._bIsSearchBarVisible) {
            this.hideSearchBar();
        }

        if(this._bIsSpotListVisible) {
            this.hideSpotList();
        }

        new AsyncTask<Void, Void, WebAPIManager.WebAPIReply>() {
            @Override
            protected WebAPIManager.WebAPIReply doInBackground(Void... voids) {
                try {
                    return new WebAPIManager().deleteRoute(strRouteID);
                }
                catch (Exception ex) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(WebAPIManager.WebAPIReply objWebAPIReply) {
                showHideProgressBar(false);
                if(objWebAPIReply != null && objWebAPIReply.isSuccess()) {
                    onBackPressed();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Wystąpił błąd podczas usuwanie trasy. Proszę spróbować ponownie", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void setNewTravelPointToPolyline(final GoogleMap objGoogleMap, final double dCoorXFrom, final double dCoorYFrom, final double dCoorXTo, final double dCoorYTo, boolean bShowProgress) {
        this.showHideProgressBar(true);
        new AsyncTask<Void, Void, ArrayList<GoogleMapsManager.Step>>() {
            @Override
            protected ArrayList<GoogleMapsManager.Step> doInBackground(Void... voids) {
                return GoogleMapsManager.getDirectionsToPoint(dCoorXFrom, dCoorYFrom, dCoorXTo, dCoorYTo);
            }

            @Override
            protected void onPostExecute(ArrayList<GoogleMapsManager.Step> listStep) {
                if(_objPolylineTravelToNextPoint != null) {
                    _objPolylineTravelToNextPoint.remove();
                }

                PolylineOptions objPolylineOptions = new PolylineOptions();
                objPolylineOptions.color(Color.BLUE);

                objPolylineOptions.add(new LatLng(dCoorXFrom, dCoorYFrom));

                for(GoogleMapsManager.Step objStep : listStep) {
                    objPolylineOptions.add(new LatLng(objStep.end_location.lat, objStep.end_location.lng));
                }

                _objPolylineTravelToNextPoint = objGoogleMap.addPolyline(objPolylineOptions);

                showHideProgressBar(false);
            }
        }.execute();


        if(_objPolylineTravelToNextPoint != null) {
            _objPolylineTravelToNextPoint.remove();
        }
    }

    int _nSimulationPointIndex = 0;
    private LatLng[] _arrSimulationPoints = new LatLng[] {
        new LatLng(51.1182515, 17.0678470),
        new LatLng(51.1177624, 17.0608020),
        new LatLng(51.1151412, 17.0588785),
        new LatLng(51.1133966, 17.0594375),
        new LatLng(51.1127200, 17.0606494),
        new LatLng(51.1094937, 17.0503735),
        new LatLng(51.1084583, 17.0458727),
        new LatLng(51.1079945, 17.0419323)
    };

    private int _nNextPointToTravelIndex = 0;
    private final int _nIntervalValue = 1000 * 1;
    private boolean _bIsFirstTravelLoad = true;
    private boolean _bIsTravellingPaused = false;

    private void startTraveling() {
        this.stopTravelling();

        _objHandlerTravelling = new Handler();
        _objHandlerTravelling.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("index", "Run!!!");
                if(_bIsTravellingPaused) {
                    _objHandlerTravelling.postDelayed(this, 1000);
                }
                else {
                    Location objLocationCurrent = new Location("dummy");
                    objLocationCurrent.setLongitude(_arrSimulationPoints[_nSimulationPointIndex].longitude);
                    objLocationCurrent.setLatitude(_arrSimulationPoints[_nSimulationPointIndex].latitude);

                    WebAPIManager.RouteSpot objRouteSpotFound = null;

                    for (WebAPIManager.RouteSpot objRouteSpot : _objRoute.Spots) {
                        if (GoogleMapsManager.isLocationNearToAnotherLocation(objLocationCurrent.getLatitude(), objLocationCurrent.getLongitude(), objRouteSpot.CoorX, objRouteSpot.CoorY, 100)) {
                            objRouteSpotFound = objRouteSpot;
                            break;
                        }
                    }

                    if (objRouteSpotFound != null) {
                        _bIsTravellingPaused = true;
                        showSpotNotificationOverlay(objRouteSpotFound);
                    }

                    MarkerOptions objMarkerOptions = new MarkerOptions();
                    objMarkerOptions.position(new LatLng(objLocationCurrent.getLatitude(), objLocationCurrent.getLongitude()));
                    objMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.yellow_man));

                    if (_objMarkerCurrentLocation != null) {
                        _objMarkerCurrentLocation.remove();
                    }

                    _objMarkerCurrentLocation = _objGoogleMap.addMarker(objMarkerOptions);

                    setNewTravelPointToPolyline(_objGoogleMap, objLocationCurrent.getLatitude(), objLocationCurrent.getLongitude(), _objRoute.Spots[_nNextPointToTravelIndex].CoorX, _objRoute.Spots[_nNextPointToTravelIndex].CoorY, _bIsFirstTravelLoad);
                    _bIsFirstTravelLoad = false;

                    _nSimulationPointIndex++;

                    if (_nSimulationPointIndex >= _arrSimulationPoints.length) {
                        stopTravelling();
                    } else {
                        _objHandlerTravelling.postDelayed(this, _nIntervalValue);
                    }
                }
            }
        }, 1);
    }

    private void stopTravelling() {
        _nSimulationPointIndex = 0;
        _nNextPointToTravelIndex = 0;
        _bIsTravellingPaused = false;

        if(_objMarkerCurrentLocation != null) {
            _objMarkerCurrentLocation.remove();
        }
        _objMarkerCurrentLocation = null;

        if(_objPolylineTravelToNextPoint != null) {
            _objPolylineTravelToNextPoint.remove();
        }
        _objPolylineTravelToNextPoint = null;

        _bIsFirstTravelLoad = true;
        if(_objHandlerTravelling != null) {
            _objHandlerTravelling.removeCallbacksAndMessages(null);
        }
    }

    private void showSpotNotificationOverlay(final WebAPIManager.RouteSpot objRouteSpot) {
        ((FancyButton) findViewById(R.id.btn_go_to_spot_details)).setIconResource(R.drawable.ic_create_black_24dp);
        ((FancyButton) findViewById(R.id.btn_continue_travel)).setIconResource(R.drawable.ic_directions_run_black_24dp);
        ((TextView) findViewById(R.id.tv_spot_notification_name)).setText("Jesteś w pobliżu:\n" + objRouteSpot.Name);
        findViewById(R.id.btn_go_to_spot_details).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View view) {
                findViewById(R.id.spot_notification_overlay).setVisibility(View.GONE);
                findViewById(R.id.fab_delete_route).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_start_travel).setVisibility(View.VISIBLE);

                showHideProgressBar(true);
                new AsyncTask<Void, Void, WebAPIManager.GetSpotBySpotIDReply>() {
                    @Override
                    protected WebAPIManager.GetSpotBySpotIDReply doInBackground(Void... voids) {
                        return new WebAPIManager().getSpotBySpotID(objRouteSpot.SpotID);
                    }

                    @Override
                    protected void onPostExecute(WebAPIManager.GetSpotBySpotIDReply objGetSpotBySpotIDReply) {
                        if(objGetSpotBySpotIDReply != null && objGetSpotBySpotIDReply.isSuccess()) {
                            FragmentManager objFragmentManager = getSupportFragmentManager();
                            FragmentTransaction objFragmentTransaction = objFragmentManager.beginTransaction();
                            _objFragmentSpotDetails = FragmentSpotDetails.newInstance(objGetSpotBySpotIDReply.Spot);
                            objFragmentTransaction.replace(R.id.main_content, _objFragmentSpotDetails).addToBackStack(null);
                            objFragmentTransaction.commit();
                            findViewById(R.id.fab_delete_route).setVisibility(View.GONE);
                            findViewById(R.id.fab_start_travel).setVisibility(View.GONE);
                            findViewById(R.id.main_content).setVisibility(View.VISIBLE);
                        }
                        showHideProgressBar(false);
                    }
                }.execute();
            }
        });

        findViewById(R.id.btn_continue_travel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.spot_notification_overlay).setVisibility(View.GONE);
                findViewById(R.id.fab_delete_route).setVisibility(View.VISIBLE);
                findViewById(R.id.fab_start_travel).setVisibility(View.VISIBLE);
                _bIsTravellingPaused = false;
                _nNextPointToTravelIndex++;
            }
        });

        findViewById(R.id.fab_delete_route).setVisibility(View.GONE);
        findViewById(R.id.fab_start_travel).setVisibility(View.GONE);

        findViewById(R.id.spot_notification_overlay).setVisibility(View.VISIBLE);
    }
}