package eguidebook.eguidebookapp;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;

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
        return strValue == null || strValue.length() == 0;
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
}
