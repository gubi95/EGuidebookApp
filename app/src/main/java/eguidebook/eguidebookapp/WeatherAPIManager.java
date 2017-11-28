package eguidebook.eguidebookapp;

import com.google.gson.Gson;

public class WeatherAPIManager {
    private static final String API_KEY = "ec637ddb30fd1c2547c3a81ba816af29";

    private class GetTemperatureByCoordinatesReply {
        public Main main;
    }

    private class Main {
        public int temp;
    }

    public static int getTemperatureByCoordinates(double dCoorX, double dCoorY) {

        try {
            String strJSON = new WebAPIManager().downloadString("api.openweathermap.org/data/2.5/weather?lat=" + String.valueOf(dCoorX) + "&lon=" + String.valueOf(dCoorY) + "&APPID=" + API_KEY + "&lang=pl&units=metric");
            return new Gson().fromJson(strJSON, GetTemperatureByCoordinatesReply.class).main.temp;
        }
        catch (Exception ex) {
            return -1;
        }
    }
}
