package com.obb.backup.restore.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;


public class InAppUpdateManager {

    private static final int MY_REQUEST_CODE = 1001; // Replace with your own request code
    private final Activity activity;
    private final AppUpdateManager appUpdateManager;
    private InstallStateUpdatedListener installStateUpdatedListener;

    public InAppUpdateManager(Activity activity) {
        this.activity = activity;
        this.appUpdateManager = AppUpdateManagerFactory.create(activity);
    }

    public void checkForUpdateAndPrompt() {
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                promptUserForUpdate(appUpdateInfo);
            }
        });
    }

    private void promptUserForUpdate(AppUpdateInfo appUpdateInfo) {
        try {
            appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    activity,
                    MY_REQUEST_CODE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
    }

    public void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                // Handle the update failure or user cancellation
            }
        }
    }

    public void registerInstallStateListener() {
        installStateUpdatedListener = state -> {
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                // The update has been downloaded and is ready to be installed.
                // You can prompt the user to install the update here.
            }
        };
        appUpdateManager.registerListener(installStateUpdatedListener);
    }

    public void unregisterInstallStateListener() {
        if (installStateUpdatedListener != null) {
            appUpdateManager.unregisterListener(installStateUpdatedListener);
        }
    }

    public static void inAppReview(Context context,Activity atc) {
        SharedPref sharedPref = new SharedPref(context);
        if (sharedPref.getInAppReviewToken() <= 3) {
            sharedPref.updateInAppReviewToken(sharedPref.getInAppReviewToken() + 1);
        } else {
            sharedPref.updateInAppReviewDone(false);
            if (!sharedPref.getInAppReviewDone()) {
                ReviewManager manager = ReviewManagerFactory.create(context);
                Task<ReviewInfo> request = manager.requestReviewFlow();
                request.addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ReviewInfo reviewInfo = task.getResult();
                        Task<Void> launchTask = manager.launchReviewFlow(atc, reviewInfo);
                        launchTask.addOnCompleteListener(complete -> {
                            if (complete.isSuccessful()) {
                               // sharedPref.updateInAppReviewDone(true);
                            } else {
                              //  sharedPref.updateInAppReviewDone(false);
                            }
                        });
                    } else {
                        sharedPref.updateInAppReviewDone(false);
                    }
                });
            }
        }
    }

}