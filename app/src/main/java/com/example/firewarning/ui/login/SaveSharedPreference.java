package com.example.firewarning.ui.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SaveSharedPreference {
    private static final String PREF_USER_NAME = "username";
    private static final String PREF_PASS_WORD = "password";

    static SharedPreferences getSharedPreferences(Context ctx) {
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void setLoginUser(Context ctx, User user) {
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString(PREF_USER_NAME, user.getEmail());
        editor.putString(PREF_PASS_WORD, user.getPassword());
        editor.apply();
    }

    public static String getPrefUserName(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_USER_NAME, "");
    }

    public static String getPrefPassWord(Context ctx) {
        return getSharedPreferences(ctx).getString(PREF_PASS_WORD, "");
    }
}
