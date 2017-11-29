package eguidebook.eguidebookapp;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class FragmentSpotsByCategory extends Fragment {
    private AppCompatActivity _objAppCompatActivity = null;
    private ArrayList<WebAPIManager.Spot> _listSpot = null;

    public class GridViewAdapter extends ArrayAdapter {
        private Context _objContext = null;
        private int _nLayoutResourceId = -1;
        private ArrayList<WebAPIManager.Spot> _listSpot = null;

        public GridViewAdapter(Context objContext, int nLayoutResourceId, ArrayList<WebAPIManager.Spot> listSpot) {
            super(objContext, nLayoutResourceId, listSpot);
            this._nLayoutResourceId = nLayoutResourceId;
            this._objContext = objContext;
            this._listSpot = listSpot;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = ((Activity) _objContext).getLayoutInflater();
                row = inflater.inflate(_nLayoutResourceId, parent, false);
            }

            final WebAPIManager.Spot objSpot = _listSpot.get(position);
            ((TextView)row.findViewById(R.id.spot_name)).setText(objSpot.Name);
            Picasso.with(_objContext).load(WebAPIManager.getBaseURL() + objSpot.Image1Path).into(((ImageView)row.findViewById(R.id.spot_main_image)));

            Location objLocationUser = EGuidebookApplication.mGPSTrackerService.getLocation();
            Location objLocationSpot = new Location("dummy");
            objLocationSpot.setLatitude(objSpot.CoorX);
            objLocationSpot.setLongitude(objSpot.CoorY);

            ImageView ivStart1 = row.findViewById(R.id.iv_spot_grade_star1);
            ImageView ivStart2 = row.findViewById(R.id.iv_spot_grade_star2);
            ImageView ivStart3 = row.findViewById(R.id.iv_spot_grade_star3);
            ImageView ivStart4 = row.findViewById(R.id.iv_spot_grade_star4);
            ImageView ivStart5 = row.findViewById(R.id.iv_spot_grade_star5);

            ivStart1.setImageResource(R.drawable.star_yellow);
            ivStart2.setImageResource(R.drawable.star_empty);
            ivStart3.setImageResource(R.drawable.star_empty);
            ivStart4.setImageResource(R.drawable.star_empty);
            ivStart5.setImageResource(R.drawable.star_empty);

            if(objSpot.AverageGrade >= 2) {
                ivStart2.setImageResource(R.drawable.star_yellow);
            }

            if(objSpot.AverageGrade >= 3) {
                ivStart3.setImageResource(R.drawable.star_yellow);
            }

            if(objSpot.AverageGrade >= 4) {
                ivStart4.setImageResource(R.drawable.star_yellow);
            }

            if(objSpot.AverageGrade >= 5) {
                ivStart5.setImageResource(R.drawable.star_yellow);
            }

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentManager objFragmentManager = _objAppCompatActivity.getSupportFragmentManager();
                    FragmentTransaction objFragmentTransaction = objFragmentManager.beginTransaction();
                    objFragmentTransaction.replace(R.id.main_content, FragmentSpotDetails.newInstance(objSpot)).addToBackStack(null);
                    objFragmentTransaction.commit();
                }
            });

            ((TextView)row.findViewById(R.id.tv_spot_distance)).setText(EGuidebookApplication.mGPSTrackerService.getDistanceBetweenTwoPointsAsString(objLocationUser, objLocationSpot));
            return row;
        }
    }

    public void filterSpots(String strSpotNameQuery) {
        if(strSpotNameQuery != null && _listSpot != null) {
            String strSpotNameQueryFormatted = strSpotNameQuery.trim().toLowerCase();
            ArrayList<WebAPIManager.Spot> listSpotFiltered = new ArrayList<>();
            for(int i = 0; i < _listSpot.size(); i++) {
                WebAPIManager.Spot objSpot = _listSpot.get(i);
                if(strSpotNameQuery.isEmpty() || objSpot.Name.trim().toLowerCase().contains(strSpotNameQueryFormatted)) {
                    listSpotFiltered.add(objSpot);
                }
            }

            ((GridView)getView().findViewById(R.id.gridView)).setAdapter(new GridViewAdapter(getContext(), R.layout.fragment_spots_by_category_gv_template, listSpotFiltered));
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static FragmentSpotsByCategory newInstance(String strSpotCategoryID, String strSpotName) {
        Bundle objBundle = new Bundle();
        objBundle.putString("SpotCategoryID", strSpotCategoryID);
        objBundle.putString("SpotName", strSpotName);

        FragmentSpotsByCategory objFragmentSpotsByCategory = new FragmentSpotsByCategory();
        objFragmentSpotsByCategory.setArguments(objBundle);
        return objFragmentSpotsByCategory;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        _objAppCompatActivity = (AppCompatActivity) getActivity();

        ((MainActivity)_objAppCompatActivity).showHideProgressBar(true);

        final View objView = inflater.inflate(R.layout.fragment_spots_by_category, container, false);

        objView.findViewById(R.id.tv_spot_category_name).setVisibility(View.GONE);
        objView.findViewById(R.id.divider_category_name).setVisibility(View.GONE);
        objView.findViewById(R.id.iv_spot_category_image).setVisibility(View.GONE);

        new GetSpotsAsyncTask(
                getArguments().getString("SpotCategoryID", ""),
                getArguments().getString("SpotName", "")).execute();

        ((MainActivity)getActivity()).setTopBarTitle("Miejsca");
        ((MainActivity)getActivity()).showHideSearchIcon(true);
        ((MainActivity)getActivity()).showHide3DotVerticalIcon(false);

        return objView;
    }

    private void loadGridView(List<WebAPIManager.Spot> listSpot) {
        _listSpot = new ArrayList<>(listSpot);
        GridView gridView = this.getView().findViewById(R.id.gridView);
        gridView.setAdapter(new GridViewAdapter(getContext(), R.layout.fragment_spots_by_category_gv_template, new ArrayList<>(listSpot)));
    }

    private void loadTitle(String strSpotCategoryID) {
        TextView tvSpotCatName = (getView().findViewById(R.id.tv_spot_category_name));
        ImageView ivSpotCatImage = (getView().findViewById(R.id.iv_spot_category_image));

        tvSpotCatName.setText("");
        ivSpotCatImage.setImageBitmap(null);

        if(EGuidebookApplication.mSpotCategories != null) {
            for(WebAPIManager.SpotCategory objSpotCategory : EGuidebookApplication.mSpotCategories) {
                if(objSpotCategory.SpotCategoryId.equals(strSpotCategoryID)){
                    tvSpotCatName.setText(objSpotCategory.Name);
                    Picasso.with(getContext()).load(WebAPIManager.getBaseURL() + objSpotCategory.IconPath).into(ivSpotCatImage);
                    break;
                }
            }
        }

        tvSpotCatName.setVisibility(View.VISIBLE);
        getView().findViewById(R.id.divider_category_name).setVisibility(View.VISIBLE);
        ivSpotCatImage.setVisibility(View.VISIBLE);

    }

    private class GetSpotsAsyncTask extends AsyncTask<Void, Void, WebAPIManager.GetSpotsByReply> {
        private String _strSpotCategoryID;
        private String _strSpotName;

        public GetSpotsAsyncTask(String strSpotCategoryID, String strSpotName) {
            this._strSpotCategoryID = strSpotCategoryID;
            this._strSpotName = strSpotName;
        }

        @Override
        protected WebAPIManager.GetSpotsByReply doInBackground(Void... voids) {
            try {
                Thread.sleep(1000);
            }
            catch (Exception ex) { }
            return new WebAPIManager().getSpotsBy(_strSpotCategoryID, _strSpotName);
        }

        @Override
        protected void onPostExecute(WebAPIManager.GetSpotsByReply objGetSpotsByReply) {
            if(!PLHelpers.stringIsNullOrEmpty(_strSpotName)) {
                TextView tvSpotCatName = (getView().findViewById(R.id.tv_spot_category_name));
                ImageView ivSpotCatImage = (getView().findViewById(R.id.iv_spot_category_image));
                tvSpotCatName.setText("\"" + _strSpotName + "\"");
                ivSpotCatImage.setImageBitmap(null);
                tvSpotCatName.setVisibility(View.VISIBLE);
                getView().findViewById(R.id.divider_category_name).setVisibility(View.VISIBLE);
                ivSpotCatImage.setVisibility(View.VISIBLE);
            }
            else {
                loadTitle(this._strSpotCategoryID);
            }

            if(objGetSpotsByReply != null && objGetSpotsByReply.isSuccess()) {
                loadGridView(objGetSpotsByReply.getSpotsAsArrayList());
            }
            else {
                loadGridView(new ArrayList<WebAPIManager.Spot>());
            }

            ((MainActivity)_objAppCompatActivity).showHideProgressBar(false);
        }
    }
}
