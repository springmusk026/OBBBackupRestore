package com.obb.backup.restore.ViewModels;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.obb.backup.restore.adapter.FileListAdapter;
import com.obb.backup.restore.databinding.HomeFragmentBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BackupViewModel extends ViewModel {

    private final MutableLiveData<Boolean> backupInProgressLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> progressLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessageLiveData = new MutableLiveData<>();


    public LiveData<Boolean> getBackupInProgressLiveData() {
        return backupInProgressLiveData;
    }

    public LiveData<Integer> getProgressLiveData() {
        return progressLiveData;
    }

    public LiveData<String> getToastMessageLiveData() {
        return toastMessageLiveData;
    }

    public void performBackup(Uri targetUri, SharedPreferences.Editor editor, Context applicationContext, HomeFragmentBinding binding) {
        DocumentFile directory = DocumentFile.fromTreeUri(applicationContext, targetUri);
        assert directory != null;
        DocumentFile[] files = directory.listFiles();

        if (files.length > 0) {
            showBackupDialog(files, editor, applicationContext, binding);
        } else {
            toastMessageLiveData.postValue("Sorry, there's no any obb file to backup");
        }
    }

    private void showBackupDialog(DocumentFile[] files, SharedPreferences.Editor editor, Context applicationContext, HomeFragmentBinding binding) {
        final ArrayAdapter<DocumentFile> adapter = new FileListAdapter(applicationContext, files);

        AlertDialog.Builder builder = new AlertDialog.Builder(applicationContext);
        builder.setTitle("Select an Obb File To Backup")
                .setAdapter(adapter, (dialog, which) -> {
                    DocumentFile selectedFile = files[which];
                    startBackup(selectedFile, editor, applicationContext);
                })
                .setOnDismissListener(dialog -> binding.backupButton.setEnabled(true));

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void startBackup(DocumentFile selectedFile, SharedPreferences.Editor editor, Context applicationContext) {
        backupInProgressLiveData.setValue(true);
        AsyncTask.execute(() -> {
            try {
                File destinationFile;
                long totalFileSize = selectedFile.length();
                String fileName;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10+ uses SAF to access files
                    fileName = selectedFile.getName();
                    assert fileName != null;
                    destinationFile = new File(applicationContext.getExternalFilesDir(null), fileName);
                } else {
                    // Older Android versions (pre-Android 10)
                    File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    File obbBackupFolder = new File(downloadsDirectory, "OBBBackup");

                    if (!obbBackupFolder.exists()) {
                        obbBackupFolder.mkdirs();
                    }

                    fileName = selectedFile.getName();
                    assert fileName != null;
                    destinationFile = new File(obbBackupFolder, fileName);
                }

                if (destinationFile != null) {
                    try (InputStream inputStream = applicationContext.getContentResolver().openInputStream(selectedFile.getUri());
                         OutputStream outputStream = new FileOutputStream(destinationFile)) {

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        long copiedFileSize = 0;

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            copiedFileSize += bytesRead;
                            int progress = (int) ((copiedFileSize * 100) / totalFileSize);
                            progressLiveData.postValue(progress);
                        }

                        saveBackupInformation(fileName, getCurrentTime(), destinationFile.getAbsolutePath(), editor);
                        toastMessageLiveData.postValue("Successfully completed backup");
                    } catch (Exception e) {
                        e.printStackTrace();
                        toastMessageLiveData.postValue("Failed to complete backup");
                    } finally {
                        backupInProgressLiveData.postValue(false);
                    }
                } else {
                    toastMessageLiveData.postValue("Failed to access destination folder");
                    backupInProgressLiveData.postValue(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void saveBackupInformation(String fileName, String time, String backupPath,SharedPreferences.Editor editor) {
        editor.putBoolean("backup_completed", true);
        editor.putString("backup_filename", fileName);
        editor.putString("backup_time", time);
        editor.putString("backup_path", backupPath);
        editor.apply();
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

}
