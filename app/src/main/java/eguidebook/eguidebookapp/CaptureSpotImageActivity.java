package eguidebook.eguidebookapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;

import mehdi.sakout.fancybuttons.FancyButton;

public class CaptureSpotImageActivity extends AppCompatActivity {
    private CaptureSpotImagePreview _objCaptureSpotImagePreview;
    private RelativeLayout _objRelativeLayout;
    private FancyButton _objFancyButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _objFancyButton = new FancyButton(getApplicationContext());
        _objFancyButton.setBackgroundColor(Color.parseColor("#6EC2EF"));
        _objFancyButton.setFocusBackgroundColor(Color.parseColor("#519BC2"));
        _objFancyButton.setRadius(30);
        _objFancyButton.setText("");
        _objFancyButton.setBorderWidth(1);
        _objFancyButton.setBorderColor(Color.parseColor("#FFFFFF"));
        _objFancyButton.setIconPosition(FancyButton.POSITION_LEFT);
        _objFancyButton.setIconResource(R.drawable.ic_photo_camera_black_24dp);

        RelativeLayout.LayoutParams objLayoutParamsButton = new RelativeLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        objLayoutParamsButton.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        objLayoutParamsButton.addRule(RelativeLayout.CENTER_HORIZONTAL);
        objLayoutParamsButton.setMargins(0, 0, 0, PLHelpers.DPtoPX(20, getApplicationContext()));

        _objFancyButton.setLayoutParams(objLayoutParamsButton);

        RelativeLayout.LayoutParams objLayoutParamsRelativeLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        RelativeLayout objRelativeLayout = new RelativeLayout(getApplicationContext());
        objRelativeLayout.setLayoutParams(objLayoutParamsRelativeLayout);
        objRelativeLayout.addView(_objFancyButton);
        objRelativeLayout.setBackgroundColor(Color.parseColor("#000000"));

        this._objRelativeLayout = objRelativeLayout;
        setContentView(this._objRelativeLayout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        _objCaptureSpotImagePreview = new CaptureSpotImagePreview(this, 0, CaptureSpotImagePreview.LayoutMode.FitToParent, _objFancyButton);
        ActionBar.LayoutParams objLayoutParams = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        _objRelativeLayout.addView(_objCaptureSpotImagePreview, 0, objLayoutParams);
    }

    @Override
    protected void onPause() {
        super.onPause();
        _objCaptureSpotImagePreview.stop();
        _objRelativeLayout.removeView(_objCaptureSpotImagePreview); // This is necessary.
        _objCaptureSpotImagePreview = null;
    }
}
