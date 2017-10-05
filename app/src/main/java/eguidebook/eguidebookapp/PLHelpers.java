package eguidebook.eguidebookapp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PLHelpers {

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
}
