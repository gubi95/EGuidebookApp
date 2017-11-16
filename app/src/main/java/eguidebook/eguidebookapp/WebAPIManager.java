package eguidebook.eguidebookapp;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class WebAPIManager {
    private static final String strBaseURL = "192.168.1.2";
    private final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    private final int CODE_OK = 0;
    private final int CODE_INTERNAL_SERVER_ERROR = 1;
    public static final int CODE_USER_ALREADY_EXISTS = 5;
    public static final int CODE_INCORRECT_USERNAME = 6;
    public static final int CODE_INCORRECT_PASSWORD = 7;

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
            //myURLConnection.setRequestProperty("Content-Length", String.valueOf(strJSON.length()));
            myURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            myURLConnection.setUseCaches(false);
            myURLConnection.setDoInput(true);
            myURLConnection.setDoOutput(true);
            //myURLConnection.connect();

            OutputStreamWriter objOutputStreamWriter = new OutputStreamWriter(myURLConnection.getOutputStream());
            objOutputStreamWriter.write(strJSON);
            objOutputStreamWriter.close();

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

    private class RegisterData {
        public String Username;
        public String Password;
    }

    public LoginReply register(String strUsername, String strPassword) {
        LoginReply objLoginReply = null;
        try {
            RegisterData objRegisterData = new RegisterData();
            objRegisterData.Username = strUsername;
            objRegisterData.Password = strPassword;
            objLoginReply = new Gson().fromJson(this.uploadString(strBaseURL + "/api/User/Register", new Gson().toJson(objRegisterData, RegisterData.class)), LoginReply.class);
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

    public class Spot implements Parcelable {
        public String SpotID;
        public String SpotCategoryID;
        public String Name;
        public String Description;
        public double CoorX;
        public double CoorY;
        public String Image1Path;
        public String Image2Path;
        public String Image3Path;
        public String Image4Path;
        public String Image5Path;
        public int AverageGrade;
        public boolean IsOpeningHoursDefined;
        public String OpeningHours_MondayFrom;
        public String OpeningHours_MondayTo;
        public String OpeningHours_TuesdayFrom;
        public String OpeningHours_TuesdayTo;
        public String OpeningHours_WednesdayFrom;
        public String OpeningHours_WednesdayTo;
        public String OpeningHours_ThursdayFrom;
        public String OpeningHours_ThursdayTo;
        public String OpeningHours_FridayFrom;
        public String OpeningHours_FridayTo;
        public String OpeningHours_SaturdayFrom;
        public String OpeningHours_SaturdayTo;
        public String OpeningHours_SundayFrom;
        public String OpeningHours_SundayTo;
        public int UserGrade;

        protected Spot(Parcel in) {
            SpotID = in.readString();
            SpotCategoryID = in.readString();
            Name = in.readString();
            Description = in.readString();
            CoorX = in.readDouble();
            CoorY = in.readDouble();
            Image1Path = in.readString();
            Image2Path = in.readString();
            Image3Path = in.readString();
            Image4Path = in.readString();
            Image5Path = in.readString();
            AverageGrade = in.readInt();
            IsOpeningHoursDefined = in.readByte() != 0;
            OpeningHours_MondayFrom = in.readString();
            OpeningHours_MondayTo = in.readString();
            OpeningHours_TuesdayFrom = in.readString();
            OpeningHours_TuesdayTo = in.readString();
            OpeningHours_WednesdayFrom = in.readString();
            OpeningHours_WednesdayTo = in.readString();
            OpeningHours_ThursdayFrom = in.readString();
            OpeningHours_ThursdayTo = in.readString();
            OpeningHours_FridayFrom = in.readString();
            OpeningHours_FridayTo = in.readString();
            OpeningHours_SaturdayFrom = in.readString();
            OpeningHours_SaturdayTo = in.readString();
            OpeningHours_SundayFrom = in.readString();
            OpeningHours_SundayTo = in.readString();
            UserGrade = in.readInt();
        }

        public final Creator<Spot> CREATOR = new Creator<Spot>() {
            @Override
            public Spot createFromParcel(Parcel in) {
                return new Spot(in);
            }

            @Override
            public Spot[] newArray(int size) {
                return new Spot[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(SpotID);
            parcel.writeString(SpotCategoryID);
            parcel.writeString(Name);
            parcel.writeString(Description);
            parcel.writeDouble(CoorX);
            parcel.writeDouble(CoorY);
            parcel.writeString(Image1Path);
            parcel.writeString(Image2Path);
            parcel.writeString(Image3Path);
            parcel.writeString(Image4Path);
            parcel.writeString(Image5Path);
            parcel.writeInt(AverageGrade);
            parcel.writeByte((byte) (IsOpeningHoursDefined ? 1 : 0));
            parcel.writeString(OpeningHours_MondayFrom);
            parcel.writeString(OpeningHours_MondayTo);
            parcel.writeString(OpeningHours_TuesdayFrom);
            parcel.writeString(OpeningHours_TuesdayTo);
            parcel.writeString(OpeningHours_WednesdayFrom);
            parcel.writeString(OpeningHours_WednesdayTo);
            parcel.writeString(OpeningHours_ThursdayFrom);
            parcel.writeString(OpeningHours_ThursdayTo);
            parcel.writeString(OpeningHours_FridayFrom);
            parcel.writeString(OpeningHours_FridayTo);
            parcel.writeString(OpeningHours_SaturdayFrom);
            parcel.writeString(OpeningHours_SaturdayTo);
            parcel.writeString(OpeningHours_SundayFrom);
            parcel.writeString(OpeningHours_SundayTo);
            parcel.writeInt(UserGrade);
        }
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

    private class CreateGradePostData {
        public String SpotID;
        public int Grade;
        public String Message;
    }

    public WebAPIReply createSpotGrade(int nGrade, String strSpotID) {
        WebAPIReply objWebAPIReply = null;
        CreateGradePostData objCreateGradePostData = new CreateGradePostData();
        objCreateGradePostData.SpotID = strSpotID;
        objCreateGradePostData.Grade = nGrade;
        objCreateGradePostData.Message = "";

        try {
            objWebAPIReply = new Gson().fromJson(this.uploadString(strBaseURL + "/api/SpotGrade/Create", new Gson().toJson(objCreateGradePostData, CreateGradePostData.class)), WebAPIReply.class);
        }
        catch (Exception ex) { }

        if(objWebAPIReply == null) {
            objWebAPIReply = new WebAPIReply();
            objWebAPIReply.Code = this.CODE_INTERNAL_SERVER_ERROR;
            objWebAPIReply.Message = this.INTERNAL_SERVER_ERROR;
        }

        return objWebAPIReply;
    }

    private class CreateSpotPostData {
        public String Name;
        public String ImageBase64;
        public double CoorX;
        public double CoorY;
        public int UserGrade;
    }

    public WebAPIReply createSpot(String strName, double dCoorX, double dCoorY, String strBase64Image, int nUserGrade) {
        WebAPIReply objWebAPIReply = null;
        CreateSpotPostData objCreateSpotPostData = new CreateSpotPostData();
        objCreateSpotPostData.Name = strName;
        objCreateSpotPostData.ImageBase64 = strBase64Image;
        objCreateSpotPostData.CoorX = dCoorX;
        objCreateSpotPostData.CoorY = dCoorY;
        objCreateSpotPostData.UserGrade = nUserGrade;

        try {
            objWebAPIReply = new Gson().fromJson(this.uploadString(strBaseURL + "/api/Spot/Create", new Gson().toJson(objCreateSpotPostData, CreateSpotPostData.class)), WebAPIReply.class);
        }
        catch (Exception ex) { }

        if(objWebAPIReply == null) {
            objWebAPIReply = new WebAPIReply();
            objWebAPIReply.Code = this.CODE_INTERNAL_SERVER_ERROR;
            objWebAPIReply.Message = this.INTERNAL_SERVER_ERROR;
        }

        return objWebAPIReply;
    }
}