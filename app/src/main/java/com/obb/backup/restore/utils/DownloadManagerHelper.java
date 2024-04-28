package com.obb.backup.restore.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import java.io.File;

public class DownloadManagerHelper {

    private Context context;

    private long downloadId = -1;

    public DownloadManagerHelper(Context context) {
        this.context = context;
    }

    public void fetchFileDetailsAndStartDownload(String downloadUrl) {
        try {
            String fileName = extractFileNameFromUrl(downloadUrl);
            String mimeType = getMimeTypeFromFileName(fileName);
            startDownload(downloadUrl, fileName, mimeType);
        } catch (Exception e) {
            showToast("Error fetching file details: " + e.getMessage());
        }
    }

    private String extractFileNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    private String getMimeTypeFromFileName(String fileName) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(fileName);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    private void startDownload(String downloadUrl, String fileName, String mimeType) {
        File downloadDirectory = new File(Environment.getExternalStorageDirectory(),
                "Download" + File.separator + "OBBBackup");
        if (!downloadDirectory.exists()) {
            downloadDirectory.mkdirs();
        }

        File destinationFile = new File(downloadDirectory, fileName);
        if (destinationFile.exists()) {
            destinationFile.delete();
        }
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        request.setTitle("Downloading " + fileName);
        request.setDescription("Downloading file...");
        request.setMimeType(mimeType);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationUri(Uri.fromFile(destinationFile));

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
