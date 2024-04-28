package com.obb.backup.restore.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.obb.backup.restore.ViewModels.RestoreViewModel;
import com.obb.backup.restore.activity.auth.DialogVerifyEmail;
import com.obb.backup.restore.activity.auth.LoginActivity;
import com.obb.backup.restore.utils.AppConfig;
import com.obb.backup.restore.utils.InAppUpdateManager;
import com.obb.backup.restore.BuildConfig;
import com.obb.backup.restore.ViewModels.BackupViewModel;
import com.obb.backup.restore.activity.UpdateActivity;
import com.obb.backup.restore.databinding.HomeFragmentBinding;
import com.obb.backup.restore.utils.ApkSignatureHelper;
import com.obb.backup.restore.utils.SharedPref;

import java.util.Locale;

public class HomeFragment extends Fragment {

    GoogleSignInClient gClient;
    GoogleSignInOptions gOptions;
    boolean backedup = false;
    private HomeFragmentBinding binding;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private RestoreViewModel restoreViewModel;
    private BackupViewModel backupViewModel;

  //  private InAppUpdateManager inAppUpdateManager;

    private SharedPref sharedPref;

    private Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = HomeFragmentBinding.inflate(inflater, container, false);

        context = requireContext();

        restoreViewModel = new ViewModelProvider(this).get(RestoreViewModel.class);
        backupViewModel = new ViewModelProvider(this).get(BackupViewModel.class);

        sharedPref = new SharedPref(context);

        gOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gClient = GoogleSignIn.getClient(context, gOptions);

        checkAndRedirectToLogin();

        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        preferences = context.getSharedPreferences("backup_info", MODE_PRIVATE);
        editor = preferences.edit();

        firebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener((Activity) context, task -> {
                    if (task.isSuccessful()) {

                        String currentvcode = firebaseRemoteConfig.getString("Cur_VersionCode");

                        int myVcode = BuildConfig.VERSION_CODE;
                        if (Integer.parseInt(currentvcode) > myVcode) {
                            Intent intent = new Intent(context, UpdateActivity.class);
                            intent.putExtra("currentVersionCode", currentvcode);
                            intent.putExtra("myVersionCode", myVcode);
                            startActivity(intent);
                        }
                        SharedPreferences preferencesx = context.getSharedPreferences("download_info", MODE_PRIVATE);
                        SharedPreferences.Editor editorx = preferencesx.edit();


                        String defaultValue = firebaseRemoteConfig.getString("url_obb");
                        int VersionCode = ApkSignatureHelper.getVersionCode(context, "com.dts.freefireth");
                        String url_obb = firebaseRemoteConfig.getString("obb_" + VersionCode);
                        editorx.putBoolean("url_fetched", true);
                        if(url_obb.isEmpty()){
                            editorx.putString("url_obb", defaultValue);
                        }
                        editorx.putString("url_obb", url_obb);
                        editorx.putInt("VersionCode", VersionCode);
                        editorx.apply();
                    }
                });

        //createMoreApps();

        backedup = preferences.getBoolean("backup_completed", false);
        if (backedup) {
            SetupBacupInfo();
        } else {
            binding.restoreButton.setVisibility(View.GONE);
        }

        binding.backupButton.setBackgroundColor(Color.BLUE);
        backupViewModel.getToastMessageLiveData().observe(getViewLifecycleOwner(), message -> {
            Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show();
        });

        backupViewModel.getBackupInProgressLiveData().observe(getViewLifecycleOwner(), inProgress -> {
            binding.progressBar.setVisibility(inProgress ? View.VISIBLE : View.GONE);
            binding.progressBartxt.setVisibility(inProgress ? View.VISIBLE : View.GONE);
            binding.progressCard.setVisibility(inProgress ? View.VISIBLE : View.GONE);
            binding.backupButton.setActivated(!inProgress);
            AppConfig.isBackingup = inProgress;
        });

        backupViewModel.getProgressLiveData().observe(getViewLifecycleOwner(), progress -> {
            binding.progressBartxt.setText(String.format(Locale.ENGLISH, "Backup Progress: %d%% completed", progress));
            binding.progressBar.setProgress(progress);
            if (progress > 95) {
                new Handler().postDelayed(() -> {
                    binding.restoreButton.setVisibility(View.VISIBLE);
                    binding.backupinfo.setVisibility(View.VISIBLE);
                    SetupBacupInfo();
                }, 2000);
            }
        });

        //Uri targetUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fobb%2Fcom.dts.freefireth");
        Uri targetUri = sharedPref.getStoredDirectoryUri();

        binding.backupButton.setOnClickListener(v -> {
            binding.backupButton.setEnabled(false);
            backupViewModel.performBackup(targetUri, editor, context, binding);

        });

        ////////////////////////////////////////////////////////////////////////////////////////////
        restoreViewModel.getRestoreInProgressLiveData().observe(getViewLifecycleOwner(), isRestoreInProgress -> {
            binding.restoreButton.setActivated(!isRestoreInProgress);
            binding.progressBar.setVisibility(isRestoreInProgress ? View.VISIBLE : View.GONE);
            binding.progressBartxt.setVisibility(isRestoreInProgress ? View.VISIBLE : View.GONE);
            binding.progressCard.setVisibility(isRestoreInProgress ? View.VISIBLE : View.GONE);
            AppConfig.isRestoring = isRestoreInProgress;
        });

        restoreViewModel.getProgressLiveData().observe(getViewLifecycleOwner(), progress -> {
            // Update UI with progress value
            binding.progressBartxt
                    .setText(String.format(Locale.ENGLISH, "Restoring Process: %d%% completed", progress));
            binding.progressBar.setProgress(progress);
            if (progress > 99) {
                editor.remove("backup_completed");
                editor.remove("backup_filename");
                editor.remove("backup_time");
                editor.remove("backup_path");
                editor.apply();
                binding.restoreButton.setVisibility(View.GONE);
                binding.backupinfo.setVisibility(View.GONE);
            }
        });

        restoreViewModel.getToastMessageLiveData().observe(getViewLifecycleOwner(), message -> {
            // Display toast messages
            Snackbar.make(requireView(), message, Snackbar.LENGTH_SHORT).show();
        });
        binding.restoreButton.setBackgroundColor(Color.MAGENTA);
        binding.restoreButton.setOnClickListener(v -> {
            backedup = preferences.getBoolean("backup_completed", false);
            if (!backedup) {
                Snackbar.make(requireView(), "Sorry there's no any backup of obb file ", Snackbar.LENGTH_SHORT).show();
                return;
            }
            String backedUpFileName = preferences.getString("backup_filename", "null");
            restoreViewModel.startRestore(context, targetUri, backedUpFileName);

        });

        ////////////////////////////////////////////////////////////////////////////////////////////////////////

        binding.appLogout.setBackgroundColor(Color.RED);
        binding.appLogout.setTextColor(Color.WHITE);
        binding.appLogout.setOnClickListener(v -> logout());

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null && !firebaseUser.isEmailVerified()) {
            DialogVerifyEmail dialog = new DialogVerifyEmail(context, firebaseAuth);
            dialog.show();
        }

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAndRedirectToLogin();
    }

    @Override
    public void onStart() {
        super.onStart();
        checkAndRedirectToLogin();
    }

    private void checkAndRedirectToLogin() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            redirectToLogin();
        }
    }

    private void redirectToLogin() {
        Intent loginIntent = new Intent(context, LoginActivity.class);
        startActivity(loginIntent);
        ((Activity) context).finish();
    }

    /*public void createMoreApps() {
        LinearLayout haxxcker_app = binding.haxxckerApp;
        LinearLayout modhub_app = binding.modhubApp;
        haxxcker_app.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=haxx.cker.cheat&hl=en_US"));
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                startActivity(intent);
            }
        });
        modhub_app.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=com.musk.modhub&hl=en_US"));
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                startActivity(intent);
            }
        });
    }*/

    public void SetupBacupInfo() {

        binding.restoreButton.setVisibility(View.VISIBLE);
        CardView backupinfo = binding.backupinfo;
        TextView backupNote = binding.backupNote;
        TextView backup_filename = binding.backupFilename;
        TextView backup_time = binding.backupTime;
        TextView backup_path = binding.backupPath;

        boolean backedup = preferences.getBoolean("backup_completed", false);
        if (backedup) {
            String fileName = preferences.getString("backup_filename", "");
            String time = preferences.getString("backup_time", "");
            String backupPath = preferences.getString("backup_path", "");

            backupinfo.setVisibility(View.VISIBLE);
            backupinfo.setCardBackgroundColor(Color.DKGRAY);

            backupNote.setTextColor(Color.RED);
            backup_filename.setTextColor(Color.GREEN);
            backup_time.setTextColor(Color.GREEN);
            backup_path.setTextColor(Color.GREEN);

            backupNote.setText("There's a backup that you made previously");
            backup_filename.setText("Name:" + fileName);
            backup_time.setText("Backup time:" + time);
            backup_path.setText("Backup in:" + backupPath);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void logout() {
        gClient.signOut().addOnCompleteListener(((Activity) context), task -> {
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(context, LoginActivity.class);
            startActivity(intent);
            ((Activity) context).finish();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //inAppUpdateManager.handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}