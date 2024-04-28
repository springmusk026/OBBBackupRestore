package com.obb.backup.restore.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

public class SharedPref {

    private Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String STORAGE_PERMISSION_GRANTED_KEY = "storagePermissionGranted";

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }


    public Integer getInAppReviewToken() {
        return sharedPreferences.getInt("in_app_review_token", 0);
    }

    public void updateInAppReviewToken(int value) {
        editor.putInt("in_app_review_token", value);
        editor.apply();
    }
    public boolean getInAppReviewDone() {
        return sharedPreferences.getBoolean("in_app_review_done", false);
    }

    public void updateInAppReviewDone(boolean value) {
        editor.putBoolean("in_app_review_done", value);
        editor.apply();
    }


    public Uri getStoredDirectoryUri() {
        String uriString =  sharedPreferences.getString(STORAGE_PERMISSION_GRANTED_KEY, "");
        return !uriString.equals("") ? Uri.parse(uriString) : null;
    }

    public boolean isStoredDirectoryUri() {
        String uriString =  sharedPreferences.getString(STORAGE_PERMISSION_GRANTED_KEY, "");
        return !uriString.equals("") ? true : false;
    }

    public void setStoredDirectoryUri(String uri) {
        editor.putString(STORAGE_PERMISSION_GRANTED_KEY, uri);
        editor.apply();
    }
}
