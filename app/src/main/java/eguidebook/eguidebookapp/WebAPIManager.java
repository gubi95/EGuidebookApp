package eguidebook.eguidebookapp;

import android.util.Base64;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class WebAPIManager {
    private static final String strBaseURL = "192.168.1.4";
    private final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    private final int CODE_OK = 0;
    private final int CODE_INTERNAL_SERVER_ERROR = 1;

    public static String getBaseURL() {
        return "http://" + strBaseURL;
    }

    private String downloadString(String strURL) {
        try {
            URL myURL = new URL("http://" + strURL);
            HttpURLConnection myURLConnection = (HttpURLConnection) myURL.openConnection();
            String userCredentials = EGuidebookApplication.mUsername + ":" + EGuidebookApplication.mPassword;
            String basicAuth = "Basic " + new String(Base64.encode(userCredentials.getBytes(), Base64.DEFAULT));
            myURLConnection.setRequestProperty("Authorization", basicAuth);
            myURLConnection.setRequestMethod("GET");
            //myURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //myURLConnection.setRequestProperty("Content-Length", "" + postData.getBytes().length);
            //myURLConnection.setRequestProperty("Content-Language", "en-US");
            myURLConnection.setUseCaches(false);
            myURLConnection.setDoInput(true);
            //myURLConnection.setDoOutput(true);
            myURLConnection.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
            String strBuffer = "";
            StringBuilder objStringBuilder = new StringBuilder();
            while ((strBuffer = in.readLine()) != null) {
                objStringBuilder.append(strBuffer);
            }
            return objStringBuilder.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String uploadString(String strURL, String strJSON) {
        try {
            URL myURL = new URL("http://" + strURL);
            HttpURLConnection myURLConnection = (HttpURLConnection) myURL.openConnection();
            String userCredentials = EGuidebookApplication.mUsername + ":" + EGuidebookApplication.mPassword;
            String basicAuth = "Basic " + new String(Base64.encode(userCredentials.getBytes(), Base64.DEFAULT));
            myURLConnection.setRequestProperty("Authorization", basicAuth);
            myURLConnection.setRequestMethod("POST");
            myURLConnection.setRequestProperty("Content-Length", String.valueOf(strJSON.length()));
            myURLConnection.setUseCaches(false);
            myURLConnection.setDoInput(true);
            myURLConnection.setDoOutput(true);
            myURLConnection.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
            String strBuffer = "";
            StringBuilder objStringBuilder = new StringBuilder();
            while ((strBuffer = in.readLine()) != null) {
                objStringBuilder.append(strBuffer);
            }
            return objStringBuilder.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public class WebAPIReply {
        public int Code;
        public String Message;

        public boolean isSuccess() {
            return this.Code == 0;
        }
    }

    public class LoginReply extends WebAPIReply {
        public SpotCategory[] SpotCategories;

        public ArrayList<SpotCategory> getSpotCategoriesAsArrayList() {
            return new ArrayList<>(Arrays.asList(this.SpotCategories));
        }
    }

    public LoginReply login() {
        LoginReply objLoginReply = null;
        try {
            objLoginReply = new Gson().fromJson(this.uploadString(strBaseURL + "/api/User/Login", ""), LoginReply.class);
        }
        catch (Exception ex) { }

        if(objLoginReply == null) {
            objLoginReply = new LoginReply();
            objLoginReply.Code = this.CODE_INTERNAL_SERVER_ERROR;
            objLoginReply.Message = this.INTERNAL_SERVER_ERROR;
            objLoginReply.SpotCategories = new SpotCategory[]{};
        }
        return objLoginReply;
    }

    public class GetAllSpotCategoriesReply extends WebAPIReply {
        public SpotCategory[] SpotCategories;

        public ArrayList<SpotCategory> getSpotCategoriesAsArrayList() {
            return new ArrayList<>(Arrays.asList(this.SpotCategories));
        }
    }

    public class SpotCategory {
        public String SpotCategoryId;
        public String Name;
        public String IconPath;
    }

    public GetAllSpotCategoriesReply getAllSpotCategories() {
        GetAllSpotCategoriesReply objGetAllSpotCategoriesReply = null;
        try {
            objGetAllSpotCategoriesReply = new Gson().fromJson(this.downloadString(strBaseURL + "/api/SpotCategory/GetAll"), GetAllSpotCategoriesReply.class);
        }
        catch (Exception ex) { }

        if(objGetAllSpotCategoriesReply == null) {
            objGetAllSpotCategoriesReply = new GetAllSpotCategoriesReply();
            objGetAllSpotCategoriesReply.Code = this.CODE_INTERNAL_SERVER_ERROR;
            objGetAllSpotCategoriesReply.Message = this.INTERNAL_SERVER_ERROR;
            objGetAllSpotCategoriesReply.SpotCategories = new SpotCategory[]{};
        }
        return objGetAllSpotCategoriesReply;
    }

    public class Spot {
        public String SpotID;
        public String SpotCategoryID;
        public String Name;
        public String Description;
        public double CoorX;
        public double CoorY;
        public String ImagePath1;
        public String ImagePath2;
        public String ImagePath3;
        public String ImagePath4;
        public String ImagePath5;
    }

    public class GetSpotsByReply extends WebAPIReply {
        public Spot[] Spots;

        public ArrayList<Spot> getSpotsAsArrayList() {
            return new ArrayList<>(Arrays.asList(this.Spots));
        }
    }

    public GetSpotsByReply getSpotsBy(String strSpotCategoryID) {
        GetSpotsByReply objGetSpotsByReply = null;
        try {
            objGetSpotsByReply = new Gson().fromJson(this.downloadString(strBaseURL + "/api/Spot/GetBy?CategoryID=" + strSpotCategoryID + "&CoorX=&CoorY="), GetSpotsByReply.class);
        }
        catch (Exception ex) { }

        if(objGetSpotsByReply == null) {
            objGetSpotsByReply = new GetSpotsByReply();
            objGetSpotsByReply.Code = this.CODE_INTERNAL_SERVER_ERROR;
            objGetSpotsByReply.Message = this.INTERNAL_SERVER_ERROR;
            objGetSpotsByReply.Spots = new Spot[]{};
        }

        return objGetSpotsByReply;
    }
}