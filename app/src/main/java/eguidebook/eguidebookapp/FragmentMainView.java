package eguidebook.eguidebookapp;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class FragmentMainView extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View objView = inflater.inflate(R.layout.fragment_main_view, container, false);

        this.setButtons(objView);
        this.setRoutesCount(objView);
        this.setSearchEditText(objView);
        this.setTemperatureAndCity(objView);

        ((MainActivity) getActivity()).setTopBarTitle("Główna");
        ((MainActivity) getActivity()).showHide3DotVerticalIcon(false);
        ((MainActivity) getActivity()).showHideSearchIcon(false);

        return objView;
    }

    public void setButtons(View objView) {
        objView.findViewById(R.id.ll_redirect_to_route_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).goToRouteListView();
            }
        });

        objView.findViewById(R.id.ll_redirect_to_creating_spot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity) getActivity()).goToCreateSpotView();
            }
        });
    }

    private void setRoutesCount(View objView) {
        ((TextView)objView.findViewById(R.id.tv_routes_count)).setText(String.valueOf(EGuidebookApplication.mRoutesCount));
    }

    private void setSearchEditText(View objView) {

        ((EditText) objView.findViewById(R.id.et_main_view_find_spot)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String strTextSearch = v.getText().toString().toLowerCase().trim();
                    if(!strTextSearch.equals("")) {
                        PLHelpers.hideKeyboard(getActivity());
                        ((MainActivity) getActivity()).goToSpotList("", strTextSearch);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    public void setTemperatureAndCity(final View objView) {
        final Location objLocation = EGuidebookApplication.mGPSTrackerService.getLocation();
        ((TextView) objView.findViewById(R.id.tv_city_name)).setText(EGuidebookApplication.mLastSetCityName);
        ((TextView) objView.findViewById(R.id.tv_city_temperature)).setText(String.valueOf(EGuidebookApplication.mLastSetTemperature) + " \u2103");

        new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                EGuidebookApplication.mLastSetCityName = GoogleMapsManager.getCityNameByCoordinates(getContext(), objLocation.getLatitude(), objLocation.getLongitude());
                EGuidebookApplication.mLastSetTemperature = WeatherAPIManager.getTemperatureByCoordinates(objLocation.getLatitude(), objLocation.getLongitude());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                ((TextView) objView.findViewById(R.id.tv_city_name)).setText(EGuidebookApplication.mLastSetCityName);
                ((TextView) objView.findViewById(R.id.tv_city_temperature)).setText(String.valueOf(EGuidebookApplication.mLastSetTemperature) + " \u2103");
                ((TextView) getActivity().findViewById(R.id.tv_nav_city_name)).setText(EGuidebookApplication.mLastSetCityName);
            }
        }.execute();
    }

    public static FragmentMainView newInstance() {
        Bundle objBundle = new Bundle();
        FragmentMainView objFragmentMainView = new FragmentMainView();
        objFragmentMainView.setArguments(objBundle);
        return objFragmentMainView;
    }
}
