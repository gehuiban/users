package com.logan19gp.users.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.EditText;

/**
 * Created by george on 11/5/2016.
 */

public class Utils {

    /**
     *
     * @param ageEdit is the EditText field where the age is collected.
     * @return the age as a Long
     */
    public static Long getAge(EditText ageEdit) {
        Long age = null;
        try {
            age = Long.parseLong(ageEdit.getText().toString().trim());
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            return age;
        }
    }

    /**
     *
     * @param context is the app context
     * @return if network connection is available or not.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
