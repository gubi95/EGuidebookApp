package eguidebook.eguidebookapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import mehdi.sakout.fancybuttons.FancyButton;

public class FragmentCreateSpot extends Fragment {
    public boolean _bIsNewSpotPhotoSet = false;
    public int _nNewSpotUserGrade = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View objView = inflater.inflate(R.layout.fragment_create_spot, container, false);

        if(savedInstanceState == null) {
            PLHelpers.setCreateSpotCurrentImage(null);
            objView.findViewById(R.id.iv_create_spot_image).setVisibility(View.GONE);
            _bIsNewSpotPhotoSet = false;
        }

        this.setCreateNewSpotPhotoButton(objView);
        this.setImageGradeStars(objView);
        this.setCreateSpotPhotoButton(objView);
        this.setCreateNewSpotConfirmButton(objView);
        this.setCloseSpotConfirmationOverlayButton(objView);

        return objView;
    }

    @Override
    public void onResume() {
        super.onResume();
        byte[] arrCapturedImage = PLHelpers.getCreateSpotCurrentImage();
        if(arrCapturedImage != null && arrCapturedImage.length > 0) {
            Bitmap objBitmap = BitmapFactory.decodeByteArray(arrCapturedImage, 0, arrCapturedImage.length);
            ((ImageView)getView().findViewById(R.id.iv_create_spot_image)).setImageBitmap(objBitmap);
            getView().findViewById(R.id.iv_create_spot_image).setVisibility(View.VISIBLE);
            _bIsNewSpotPhotoSet = true;
        }
        else {
            getView().findViewById(R.id.iv_create_spot_image).setVisibility(View.GONE);
            _bIsNewSpotPhotoSet = false;
        }

        this.setCreateSpotPhotoButton(getView());
    }

    public void setCreateSpotPhotoButton(View objView) {
        FancyButton objFancyButton = objView.findViewById(R.id.btn_create_spot_photo);
        if(this._bIsNewSpotPhotoSet) {
            objFancyButton.setIconResource(R.drawable.ic_delete_forever_black_24dp);
            objFancyButton.setText("Usuń zdjęcie");
        }
        else {
            objFancyButton.setIconResource(R.drawable.ic_photo_camera_black_24dp);
            objFancyButton.setText("Zrób zdjęcie");
        }
    }

    public void setImageGradeStars(View objView) {
        final View[] arrStarViews = {
                objView.findViewById(R.id.iv_spot_grade_star1),
                objView.findViewById(R.id.iv_spot_grade_star2),
                objView.findViewById(R.id.iv_spot_grade_star3),
                objView.findViewById(R.id.iv_spot_grade_star4),
                objView.findViewById(R.id.iv_spot_grade_star5),
        };

        for(int i = 0; i < arrStarViews.length; i++) {
            final int finalI = i;
            arrStarViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    _nNewSpotUserGrade = finalI + 1;

                    for(int j = 0; j <= finalI; j++) {
                        ((ImageView) arrStarViews[j]).setImageResource(R.drawable.star_yellow);
                    }

                    for(int j = finalI + 1; j < arrStarViews.length; j++) {
                        ((ImageView) arrStarViews[j]).setImageResource(R.drawable.star_empty);
                    }
                }
            });
        }
    }

    public void setCreateNewSpotPhotoButton(final View objView) {
        objView.findViewById(R.id.btn_create_spot_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(_bIsNewSpotPhotoSet) {
                    PLHelpers.setCreateSpotCurrentImage(null);
                    _bIsNewSpotPhotoSet = false;
                    ((ImageView)objView.findViewById(R.id.iv_create_spot_image)).setImageBitmap(null);
                    objView.findViewById(R.id.iv_create_spot_image).setVisibility(View.GONE);
                    setCreateSpotPhotoButton(objView);
                }
                else {
                    startActivity(new Intent(getContext(), CaptureSpotImageActivity.class));
                }
            }
        });
    }

    public void setCreateNewSpotConfirmButton(final View objView) {
        final FancyButton objFancyButton = objView.findViewById(R.id.btn_create_spot);
        objFancyButton.setIconResource(R.drawable.ic_check_black_24dp);

        objFancyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText etNewSpotName = objView.findViewById(R.id.et_new_spot_spot_name);
                final String strSpotName = etNewSpotName.getText().toString();

                if(strSpotName.trim().equals("")) {
                    etNewSpotName.setError("To pole jest wymagane");
                    return;
                }
                else {
                    etNewSpotName.setError(null);
                }

                Location objLocation = EGuidebookApplication.mGPSTrackerService.getLocation();
                if(objLocation == null) {
                    //if(EGuidebookApplication.isEmulator()) {
                        objLocation = new Location("dummy");
                        objLocation.setLongitude(51.1127200);
                        objLocation.setLatitude(17.0606494);
                    //}
                    //else {
                    //    Toast.makeText(getContext(), "Proszę włączyć usługę GPS", Toast.LENGTH_LONG).show();
                    //    return;
                    //}
                }

                String strNewSpotImageBase64 = "";
                byte[] arrNewSpotImageBytes = PLHelpers.getCreateSpotCurrentImage();

                if(arrNewSpotImageBytes != null && arrNewSpotImageBytes.length > 0) {
                    strNewSpotImageBase64 = new String(Base64.encode(arrNewSpotImageBytes, Base64.DEFAULT));
                }

                ((MainActivity) getActivity()).showHideProgressBar(true);
                final String finalStrNewSpotImageBase6 = strNewSpotImageBase64;
                final Location finalObjLocation = objLocation;
                new AsyncTask<Void, Void, WebAPIManager.WebAPIReply>() {

                    @Override
                    protected WebAPIManager.WebAPIReply doInBackground(Void... voids) {
                        try {
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException e) { }
                        return new WebAPIManager().createSpot(strSpotName, finalObjLocation.getLatitude(), finalObjLocation.getLongitude(), finalStrNewSpotImageBase6, _nNewSpotUserGrade);
                    }

                    @Override
                    protected void onPostExecute(WebAPIManager.WebAPIReply objWebAPIReply) {
                        super.onPostExecute(objWebAPIReply);

                        if(objWebAPIReply.isSuccess()) {
                            ((ImageView)objView.findViewById(R.id.iv_create_spot_image)).setImageBitmap(null);
                            objView.findViewById(R.id.spot_confirmation_overlay).setVisibility(View.VISIBLE);
                        }
                        else {
                            Toast.makeText(getContext(), "Coś poszło nie tak, proszę spróbować ponownie...", Toast.LENGTH_LONG).show();
                        }

                        ((MainActivity) getActivity()).showHideProgressBar(false);
                    }
                }.execute();
            }
        });
    }

    public void setCloseSpotConfirmationOverlayButton(final View objView) {
        FancyButton objFancyButton = objView.findViewById(R.id.btn_close_spot_confirmation_overlay);
        objFancyButton.setIconResource(R.drawable.ic_check_black_24dp);
        objFancyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                objView.findViewById(R.id.spot_confirmation_overlay).setVisibility(View.GONE);
            }
        });
    }

    public static FragmentCreateSpot newInstance() {
        Bundle objBundle = new Bundle();
        FragmentCreateSpot objFragmentCreateSpot = new FragmentCreateSpot ();
        objFragmentCreateSpot.setArguments(objBundle);
        return objFragmentCreateSpot;
    }
}
