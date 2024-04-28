package com.obb.backup.restore.utils;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class SnackbarHelper {

    private static Snackbar snackbar;
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public static void showSnackbar(View view, String message) {
        snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    public static void dismissSnackbar() {
        if (snackbar != null && snackbar.isShownOrQueued()) {
            snackbar.dismiss();
            handler.postDelayed(() -> snackbar = null, 1000);
        }
    }
}
