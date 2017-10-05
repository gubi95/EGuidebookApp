package eguidebook.eguidebookapp;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FragmentSpotsByCategory extends Fragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public static FragmentSpotsByCategory newInstance(String strSpotCategoryID) {

        Bundle objBundle = new Bundle();
        objBundle.putString("SpotCategoryID", strSpotCategoryID);

        FragmentSpotsByCategory objFragmentSpotsByCategory = new FragmentSpotsByCategory();
        objFragmentSpotsByCategory.setArguments(objBundle);
        return objFragmentSpotsByCategory;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ((MainActivity)getActivity()).showHideProgressBar(true);
        new GetSpotsAsyncTask(getArguments().getString("SpotCategoryID")).execute();

        final View view = inflater.inflate(R.layout.fragment_spots_by_category, container, false);
        return view;
    }

    private class GetSpotsAsyncTask extends AsyncTask<Void, Void, WebAPIManager.GetSpotsByReply> {
        private String _strSpotCategoryID;

        public GetSpotsAsyncTask(String strSpotCategoryID) {
            this._strSpotCategoryID = strSpotCategoryID;
        }

        @Override
        protected WebAPIManager.GetSpotsByReply doInBackground(Void... voids) {
            try {
                Thread.sleep(1000);
            }
            catch (Exception ex) { }
            return new WebAPIManager().getSpotsBy(_strSpotCategoryID);
        }

        @Override
        protected void onPostExecute(WebAPIManager.GetSpotsByReply objGetSpotsByReply) {
            ((MainActivity)getActivity()).showHideProgressBar(false);
        }
    }
}
