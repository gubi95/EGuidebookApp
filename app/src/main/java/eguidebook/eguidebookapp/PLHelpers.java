package eguidebook.eguidebookapp;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.gson.Gson;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PLHelpers {
     static byte[] _arrCreateSpotCurrentImage = null;

    public static boolean isEmailValid(String strEmail) {
        try {
            return Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE).matcher(strEmail).find();
        }
        catch (Exception ex) { }
        return false;
    }

    public static boolean stringIsNullOrEmpty(String strValue) {
        return strValue == null || strValue.trim().length() == 0;
    }

    public static int DPtoPX(int nDP, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = nDP * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return (int) px;
    }

    public static void setCreateSpotCurrentImage(byte[] arrCreateSpotCurrentImage) {
        if(arrCreateSpotCurrentImage != null) {
            _arrCreateSpotCurrentImage = new byte[arrCreateSpotCurrentImage.length];
            System.arraycopy(arrCreateSpotCurrentImage, 0, _arrCreateSpotCurrentImage, 0, arrCreateSpotCurrentImage.length);
        }
        else {
            _arrCreateSpotCurrentImage = null;
        }
    }

    public static byte[] getCreateSpotCurrentImage() {
        return _arrCreateSpotCurrentImage;
    }

    public static void hideKeyboard(FragmentActivity objFragmentActivity) {
        try {
            View objView = objFragmentActivity.getCurrentFocus();
            if (objView != null) {
                InputMethodManager objInputMethodManager = (InputMethodManager) objFragmentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                objInputMethodManager.hideSoftInputFromWindow(objView.getWindowToken(), 0);
            }
        }
        catch (Exception ex) { }
    }

    public static <T> T copyObject(T objectToCopy) {
        return (T) new Gson().fromJson(new Gson().toJson(objectToCopy, objectToCopy.getClass()), objectToCopy.getClass());
    }
}
