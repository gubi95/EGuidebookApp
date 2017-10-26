package eguidebook.eguidebookapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import mehdi.sakout.fancybuttons.FancyButton;

public class FragmentCreateSpot extends Fragment {
    public boolean _bIsNewSpotPhotoSet = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View objView = inflater.inflate(R.layout.fragment_create_spot, container, false);
        ((FancyButton)objView.findViewById(R.id.btn_create_spot)).setIconResource(R.drawable.ic_check_black_24dp);

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

        if(savedInstanceState == null) {
            PLHelpers.setCreateSpotCurrentImage(null);
            objView.findViewById(R.id.iv_create_spot_image).setVisibility(View.GONE);
            _bIsNewSpotPhotoSet = false;
        }

        this.setCreateSpotPhotoButton(objView);

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

    public static FragmentCreateSpot newInstance() {
        Bundle objBundle = new Bundle();
        FragmentCreateSpot objFragmentCreateSpot = new FragmentCreateSpot ();
        objFragmentCreateSpot.setArguments(objBundle);
        return objFragmentCreateSpot;
    }
}
