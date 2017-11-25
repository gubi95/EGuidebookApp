package eguidebook.eguidebookapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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

public class ActivityRouteDetails extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private GoogleMap _objGoogleMap;
    private WebAPIManager.Route _objRoute = null;
    private ArrayList<WebAPIManager.GoogleMapSpot> _listGoogleMapSpot = null;
    private HashMap<String, Marker> _dictSpotIDToMapMarker = null;
    private EnumMode _eMode = EnumMode.VIEW;
    private boolean _bIsSearchBarVisible = false;
    private boolean _bIsSpotListVisible = false;
    private Polyline _objPolyline = null;

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

        _dictSpotIDToMapMarker = new HashMap<>();

        this.setViewSpotListButton();
        this.setSearchBarButton();
        this.setFloatingActionButtons();
    }

    @Override
    public void onMapReady(GoogleMap objGoogleMap) {
        _objGoogleMap = objGoogleMap;
        _objGoogleMap.setOnMarkerClickListener(this);
        this.loadMap(EnumMode.VIEW);
    }

    private void showHideProgressBar(boolean bShow) {
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
                if(_eMode == EnumMode.VIEW) {
                    _eMode = EnumMode.EDIT;
                }
                else if(_eMode == EnumMode.EDIT) {
                    _eMode = EnumMode.VIEW;
                    saveRoute(_objRoute);
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
    }

    private void hideSearchBar() {
        _bIsSearchBarVisible = false;
        PLHelpers.hideKeyboard(this);
        findViewById(R.id.ll_route_search_panel).setVisibility(View.GONE);
        ((EditText) findViewById(R.id.et_route_search)).setText("");
        findViewById(R.id.et_route_search).clearFocus();
    }

    private void showSpotList() {
        _bIsSpotListVisible = true;
        this.loadRouteSpotList();
        findViewById(R.id.ll_route_spot_list_panel).setVisibility(View.VISIBLE);
        findViewById(R.id.fab_edit_route).setVisibility(_eMode == EnumMode.VIEW ? View.GONE : View.VISIBLE);
    }

    private void hideSpotList() {
        _bIsSpotListVisible = false;
        findViewById(R.id.ll_route_spot_list_panel).setVisibility(View.GONE);
        findViewById(R.id.fab_edit_route).setVisibility(View.VISIBLE);
    }

    public void loadMap(EnumMode eMode) {
        this.hideSearchBar();
        if(eMode == EnumMode.VIEW) {
            ((TextView) findViewById(R.id.tv_route_mode_name)).setText("Podgląd");
            findViewById(R.id.iv_route_details_search_icon).setVisibility(View.GONE);
            ((FloatingActionButton) findViewById(R.id.fab_edit_route)).setImageResource(R.drawable.ic_edit_black_24dp);
            this.loadRoute(_objGoogleMap, _objRoute, true);
        }
        else if(eMode == EnumMode.EDIT) {
            ((TextView) findViewById(R.id.tv_route_mode_name)).setText("Edycja");
            findViewById(R.id.iv_route_details_search_icon).setVisibility(View.VISIBLE);
            ((FloatingActionButton) findViewById(R.id.fab_edit_route)).setImageResource(R.drawable.ic_check_black_24dp);
            this.loadMapForEditMode();
        }
        else {
            ((TextView)findViewById(R.id.tv_route_mode_name)).setText("Zwiedzanie");
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
        if(_eMode == EnumMode.EDIT) {
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
        private WebAPIManager.Route _objRoute = null;

        public SpotListRecyclerViewAdapter(WebAPIManager.Route objRoute) {
            this._objRoute = PLHelpers.copyObject(objRoute);
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
            final WebAPIManager.RouteSpot objRouteSpot = this._objRoute.Spots[position];

            objSpotViewHolder.getSpotNameTextView().setText(String.valueOf(position + 1) + ". " + objRouteSpot.Name);

            if(_eMode == EnumMode.VIEW) {
                objSpotViewHolder.getSpotDeleteImageView().setVisibility(View.GONE);
            }
            else {
                objSpotViewHolder.getSpotDeleteImageView().setVisibility(View.VISIBLE);
                objSpotViewHolder.getSpotDeleteImageView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int nCurrentPosition = objSpotViewHolder.getAdapterPosition();
                        _listSpotIDsToDelete.add(objRouteSpot.SpotID);
                        ArrayList<WebAPIManager.RouteSpot> listRouteSpot = new ArrayList<>(Arrays.asList(_objRoute.Spots));
                        listRouteSpot.remove(nCurrentPosition);
                        _objRoute.Spots = listRouteSpot.toArray(new WebAPIManager.RouteSpot[listRouteSpot.size()]);
                        notifyDataSetChanged();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return this._objRoute.Spots.length;
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

    @SuppressLint("StaticFieldLeak")
    private void saveRoute(final WebAPIManager.Route objRoute) {
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
                    ArrayList<String> listSpotIDs = new ArrayList<>();

                    for (WebAPIManager.RouteSpot objRouteSpot : _objRoute.Spots) {
                        listSpotIDs.add(objRouteSpot.SpotID);
                    }

                    String[] arrSpotIDs = listSpotIDs.toArray(new String[listSpotIDs.size()]);

                    if (PLHelpers.stringIsNullOrEmpty(objRoute.RouteID)) {
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
                }
                showHideProgressBar(false);
            }
        }.execute();
    }
}