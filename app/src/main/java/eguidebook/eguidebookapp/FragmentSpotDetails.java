package eguidebook.eguidebookapp;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class FragmentSpotDetails extends Fragment {
    private WebAPIManager.Spot _objSpot = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View objView = inflater.inflate(R.layout.fragment_spot_details, container, false);

        _objSpot = getArguments().getParcelable("Spot");

        ImageView ivSpotDetailsImage1 = objView.findViewById(R.id.iv_spot_details_image1);
        ImageView ivSpotDetailsImage2 = objView.findViewById(R.id.iv_spot_details_image2);
        ImageView ivSpotDetailsImage3 = objView.findViewById(R.id.iv_spot_details_image3);
        ImageView ivSpotDetailsImage4 = objView.findViewById(R.id.iv_spot_details_image4);
        ImageView ivSpotDetailsImage5 = objView.findViewById(R.id.iv_spot_details_image5);

        Context objContext = getContext();

        this.setSpotImage(objContext, ivSpotDetailsImage1, _objSpot.Image1Path);
        this.setSpotImage(objContext, ivSpotDetailsImage2, _objSpot.Image2Path);
        this.setSpotImage(objContext, ivSpotDetailsImage3, _objSpot.Image3Path);
        this.setSpotImage(objContext, ivSpotDetailsImage4, _objSpot.Image4Path);
        this.setSpotImage(objContext, ivSpotDetailsImage5, _objSpot.Image5Path);

        return objView;
    }

    private void setSpotImage(Context objContext, ImageView objImageView, String strURL) {
        if(PLHelpers.stringIsNullOrEmpty(strURL)) {
            objImageView.setVisibility(View.GONE);
            objImageView.setImageBitmap(null);
        }
        else {
            Picasso.with(objContext).load(WebAPIManager.getBaseURL() + strURL).into(objImageView);
            objImageView.setVisibility(View.VISIBLE);
        }
    }

    public static FragmentSpotDetails newInstance(WebAPIManager.Spot objSpot) {
        Bundle objBundle = new Bundle();
        objBundle.putParcelable("Spot", objSpot);
        FragmentSpotDetails objFragmentSpotDetails = new FragmentSpotDetails ();
        objFragmentSpotDetails.setArguments(objBundle);
        return objFragmentSpotDetails;
    }
}
