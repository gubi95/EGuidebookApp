package eguidebook.eguidebookapp;

import android.os.Build;

import java.util.ArrayList;

public class EGuidebookApplication {
    public static String mUsername;
    public static String mPassword;

    public static ArrayList<WebAPIManager.SpotCategory> mSpotCategories;

    public static GPSTrackerService mGPSTrackerService = null;

    public interface ILogoutSuccessCallback {
        void doAction();
    }

    public static void logout(ILogoutSuccessCallback objILogoutSuccessCallback) {
        mUsername = "";
        mPassword = "";
        mSpotCategories = null;
        mGPSTrackerService = null;

        if(objILogoutSuccessCallback != null) {
            objILogoutSuccessCallback.doAction();
        }
    }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }
}
