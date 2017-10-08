package eguidebook.eguidebookapp;

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
}
