package com.obb.backup.restore.ViewModels;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;

import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class RestoreViewModel extends ViewModel {

    private final MutableLiveData<Boolean> restoreInProgressLiveData = new MutableLiveData<>();
    private final MutableLiveData<Integer> progressLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessageLiveData = new MutableLiveData<>();

    public LiveData<Boolean> getRestoreInProgressLiveData() {
        return restoreInProgressLiveData;
    }

    public LiveData<Integer> getProgressLiveData() {
        return progressLiveData;
    }

    public LiveData<String> getToastMessageLiveData() {
        return toastMessageLiveData;
    }

    public void startRestore(Context context, Uri targetUri, String backedUpFileName) {
        restoreInProgressLiveData.setValue(true);

        AsyncTask.execute(() -> {
            try {
                File backedUpFile;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Android 10+ uses SAF to access files
                    backedUpFile = new File(context.getExternalFilesDir(null), backedUpFileName);
                } else {
                    // Older Android versions (pre-Android 10)
                    File backupFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "OBBBackup");
                    backedUpFile = new File(backupFolder, backedUpFileName);
                }

                long totalFileSize = backedUpFile.length();

                if (backedUpFile.exists()) {
                    DocumentFile targetDocument = DocumentFile.fromTreeUri(context, targetUri);

                    assert targetDocument != null;
                    DocumentFile existingFile = targetDocument.findFile(backedUpFileName);
                    if (existingFile != null) {
                        existingFile.delete();
                    }
                    DocumentFile newFile = targetDocument.createFile("application/octet-stream", backedUpFileName);
                    if (newFile != null) {

                        try (OutputStream docOutputStream = context.getContentResolver().openOutputStream(newFile.getUri());
                             InputStream docInputStream = new FileInputStream(backedUpFile)) {

                            byte[] docBuffer = new byte[1024];
                            int docBytesRead;
                            long copiedFileSize = 0;

                            while ((docBytesRead = docInputStream.read(docBuffer)) != -1) {
                                docOutputStream.write(docBuffer, 0, docBytesRead);
                                copiedFileSize += docBytesRead; // Update copied file size
                                int progress = (int) ((copiedFileSize * 100) / totalFileSize);
                                progressLiveData.postValue(progress); // Update progressLiveData
                            }

                            toastMessageLiveData.postValue("Successfully restored backup");
                        } catch (Exception e) {
                            e.printStackTrace();
                            toastMessageLiveData.postValue("Failed to restore backup: " + e.getMessage());
                        } finally {
                            progressLiveData.postValue(100); // Update progress to 100
                            backedUpFile.delete();
                        }
                    }
                } else {
                    toastMessageLiveData.postValue("Backup file not found in the backup folder");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                restoreInProgressLiveData.postValue(false);
            }
        });
    }

}
