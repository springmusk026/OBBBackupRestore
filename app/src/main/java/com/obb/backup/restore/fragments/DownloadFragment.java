package com.obb.backup.restore.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;

import com.downloader.Error;
import com.downloader.OnDownloadListener;
import com.downloader.PRDownloader;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.obb.backup.restore.activity.auth.LoginActivity;
import com.obb.backup.restore.utils.AppConfig;
import com.obb.backup.restore.utils.SharedPref;
import com.obb.backup.restore.BuildConfig;
import com.obb.backup.restore.databinding.FragmentDownloadBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DownloadFragment extends Fragment {

    private static final int PERMISSION_REQUEST_CODE = 1;

    private FragmentDownloadBinding binding;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private CardView cardView;
    private TextView infoTextView, etaTextView, progressTextView, speedTextView;
    private Button downloadButton;
    private ProgressBar progressBar;

    private int downloadId;
    private long startTimeMillis; // Record the start time when the download begins
    private boolean isDownloadStarted = false;
    private boolean isDownloadPaused = false;
    private boolean isDownloadInProgress = false;
    private GoogleSignInOptions gOptions;
    private GoogleSignInClient gClient;

    // simple boolean to check the status of ad
    private boolean adLoaded = false;

    private SharedPref sharedPref;

    private Context context;
    private Activity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDownloadBinding.inflate(inflater, container, false);

        context = getContext();
        activity = getActivity();

        sharedPref = new SharedPref(context);
        gOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gClient = GoogleSignIn.getClient(context, gOptions);

        checkAndRedirectToLogin();
        preferences = context.getSharedPreferences("download_info", MODE_PRIVATE);
        editor = preferences.edit();

        binding.downloadFilename.setText("main."+preferences.getInt("VersionCode",2019116547)+".com.dts.freefireth.obb");
        PRDownloader.initialize(context);

        cardView = binding.cardView;
        infoTextView = binding.infoTextView;
        etaTextView = binding.etaTextView;
        progressTextView = binding.progressTextView;
        speedTextView = binding.speedTextView;
        downloadButton = binding.downloadButton;
        progressBar = binding.progressBar;

        downloadButton.setOnClickListener(v -> {
            startDownload();
        });
        SetupBacupInfo();
        return binding.getRoot();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            activity.onBackPressed(); // Go back when the home button is clicked
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        activity.finish();
    }

    private void startDownload() {

        if (isDownloadInProgress) {
            return;
        }
        isDownloadInProgress = true;

        downloadButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        etaTextView.setVisibility(View.VISIBLE);
        progressTextView.setVisibility(View.VISIBLE);
        speedTextView.setVisibility(View.VISIBLE);

        String url = preferences.getString("url_obb", "");
        startTimeMillis = System.currentTimeMillis();

        downloadId = PRDownloader.download(url, context.getCacheDir().getPath(), "my_obbb_file.obb")
                .build()
                .setOnStartOrResumeListener(() -> {
                    if (!isDownloadStarted) {
                        isDownloadStarted = true;
                        // pauseButtonX.setVisibility(View.VISIBLE);
                        activity.runOnUiThread(
                                () -> Snackbar.make(requireView(), "Download started", Snackbar.LENGTH_SHORT).show());
                    } else {
                        if (isDownloadPaused) {
                            activity.runOnUiThread(() -> {
                                isDownloadPaused = false;
                                PRDownloader.resume(downloadId);
                                isDownloadInProgress = true;
                                Snackbar.make(requireView(), "Download resumed", Snackbar.LENGTH_SHORT).show();
                            });
                        }

                    }
                })

                .setOnPauseListener(() -> {
                    if (!isDownloadPaused) {
                        // Handle pause actions here
                        activity.runOnUiThread(() -> {
                            PRDownloader.pause(downloadId); // Pause the download
                            isDownloadPaused = true;
                            isDownloadInProgress = true;
                            Snackbar.make(requireView(), "Download paused", Snackbar.LENGTH_SHORT).show();
                        });
                    }
                })

                .setOnCancelListener(() -> {
                    isDownloadInProgress = false;

                    PRDownloader.cancel(downloadId); // Pause the download
                    activity.runOnUiThread(() -> {
                        isDownloadInProgress = false;
                        Snackbar.make(requireView(), "Download Cancelled", Snackbar.LENGTH_SHORT).show();
                    });
                    // Handle cancel
                })
                .setOnProgressListener(progress -> {
                    int percent = (int) ((progress.currentBytes * 100) / progress.totalBytes);
                    progressBar.setProgress(percent);

                    long downloadedBytes = progress.currentBytes;
                    long totalBytes = progress.totalBytes;

                    long elapsedTimeMillis = Math.max(1, System.currentTimeMillis() - startTimeMillis);

                    long remainingBytes = totalBytes - downloadedBytes;

                    long ept = (elapsedTimeMillis / 1000) > 0 ? elapsedTimeMillis / 1000 : 1;
                    long speedBytesPerSecond = downloadedBytes / ept;
                    long etaMillis = (remainingBytes / speedBytesPerSecond) * 1000;

                    progressTextView.setText("Progress: " + percent + "%");
                    etaTextView.setText("ETA: " + formatTime(etaMillis));
                    speedTextView.setText("Speed: " + formatSpeed(speedBytesPerSecond) + "/s");
                })

                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        isDownloadInProgress = false;
                        // Handle download complete
                        progressBar.setVisibility(View.INVISIBLE);
                        etaTextView.setVisibility(View.INVISIBLE);
                        progressTextView.setVisibility(View.INVISIBLE);
                        speedTextView.setVisibility(View.INVISIBLE);
                        infoTextView.setText("Download Complete");
                        infoTextView.setVisibility(View.VISIBLE);

                       // Uri targetUri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fobb%2Fcom.dts.freefireth");
                        Uri targetUri = sharedPref.getStoredDirectoryUri();

                        File tempFile = new File(context.getCacheDir(), "my_obbb_file.obb");

                        AsyncTask.execute(() -> {

                            DocumentFile targetDocument = DocumentFile.fromTreeUri(context, targetUri);

                            DocumentFile existingFile = targetDocument
                                    .findFile("main."+preferences.getInt("VersionCode",2019116547)+".com.dts.freefireth.obb");
                            if (existingFile != null) {
                                existingFile.delete();
                            }
                            DocumentFile newFile = targetDocument.createFile("application/octet-stream",
                                    "main."+preferences.getInt("VersionCode",2019116547)+".com.dts.freefireth.obb");
                            if (newFile != null) {

                                try (OutputStream docOutputStream = activity.getContentResolver()
                                        .openOutputStream(newFile.getUri());
                                     InputStream docInputStream = new FileInputStream(tempFile)) {

                                    byte[] docBuffer = new byte[1024];
                                    int docBytesRead;

                                    while ((docBytesRead = docInputStream.read(docBuffer)) != -1) {
                                        docOutputStream.write(docBuffer, 0, docBytesRead);
                                    }
                                    activity.runOnUiThread(() -> Snackbar
                                            .make(requireView(), "Sucessfully downloaded obb", Snackbar.LENGTH_SHORT)
                                            .show());

                                    saveBackupInformation(newFile.getName(), getCurrentTime(),
                                            newFile.getUri().getPath(), editor);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    activity.runOnUiThread(() -> Snackbar.make(requireView(),
                                                    "Failed to downloaded obb : " + e.toString(), Snackbar.LENGTH_SHORT)
                                            .show());
                                } finally {
                                    tempFile.delete();
                                }
                            }
                        });

                    }

                    @Override
                    public void onError(Error error) {
                        isDownloadInProgress = false;
                        // Handle download error
                        progressBar.setVisibility(View.INVISIBLE);
                        etaTextView.setVisibility(View.INVISIBLE);
                        progressTextView.setVisibility(View.INVISIBLE);
                        speedTextView.setVisibility(View.INVISIBLE);
                        infoTextView.setText("Download Error");
                        infoTextView.setVisibility(View.VISIBLE);
                        Snackbar.make(requireView(), error.getServerErrorMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    public void SetupBacupInfo() {
        CardView backupinfo = binding.downloadinfo;
        TextView backupNote = binding.downloadNote;
        TextView backup_filename = binding.downloadFilename;
        TextView backup_time = binding.downloadTime;
        TextView backup_path = binding.downloadPath;

        boolean backedup = preferences.getBoolean("download_completed", false);
        if (backedup) {
            String fileName = preferences.getString("download_filename", "");
            String time = preferences.getString("download_time", "");
            String backupPath = preferences.getString("download_path", "");

            backupinfo.setVisibility(View.VISIBLE);
            backupinfo.setCardBackgroundColor(Color.DKGRAY);

            backupNote.setTextColor(Color.RED);
            backup_filename.setTextColor(Color.GREEN);
            backup_time.setTextColor(Color.GREEN);
            backup_path.setTextColor(Color.GREEN);

            backupNote.setText("You had Previously Downloaded OBB");
            backup_filename.setText("Name:" + fileName);
            backup_time.setText("Backup time:" + time);
            backup_path.setText("Backup in:" + backupPath);
        }
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        return String.format(Locale.getDefault(), "%02d:%02d:%02d",
                hours, minutes % 60, seconds % 60);
    }

    private String formatSpeed(long bytesPerSecond) {
        if (bytesPerSecond < 1024) {
            return bytesPerSecond + " B";
        } else if (bytesPerSecond < 1024 * 1024) {
            return (bytesPerSecond / 1024) + " KB";
        } else {
            return String.format("%.2f MB", bytesPerSecond / (1024.0 * 1024));
        }
    }

    @Override
    public void onDestroyView() {
        PRDownloader.cancel(downloadId);
        super.onDestroyView();
    }

    private void saveBackupInformation(String fileName, String time, String backupPath, SharedPreferences.Editor editor) {
        editor.putBoolean("download_completed", true);
        editor.putString("download_filename", fileName);
        editor.putString("download_time", time);
        editor.putString("download_path", backupPath);
        editor.apply();

        SetupBacupInfo();
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

}