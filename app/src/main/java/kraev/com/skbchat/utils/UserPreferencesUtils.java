package kraev.com.skbchat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by qbai on 15.04.2017.
 */

//хранение в памяти устройстрва значения текущего UID
//требуется для правильной прорисовки сообщений чата -
//свои слева, чужие - справа
public class UserPreferencesUtils {

    public static final String CURRENT_USER_UID = "current uid";


    public static void setCurrentUserUid(Context context, String uid) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPref.edit().putString(CURRENT_USER_UID, uid).apply();
    }

    public static String getCurrentUserUid(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getString(CURRENT_USER_UID, "");
    }


}
