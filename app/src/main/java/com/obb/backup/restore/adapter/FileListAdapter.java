package com.obb.backup.restore.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import com.obb.backup.restore.databinding.FileListItemBinding;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileListAdapter extends ArrayAdapter<DocumentFile> {
    private final Context context;
    private LayoutInflater inflater;

    public FileListAdapter(Context context, DocumentFile[] files) {
        super(context, 0, files);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        FileListItemBinding binding;

        if (convertView == null) {
            binding = FileListItemBinding.inflate(inflater, parent, false);
            convertView = binding.getRoot();
            convertView.setTag(binding);
        } else {
            binding = (FileListItemBinding) convertView.getTag();
        }

        DocumentFile file = getItem(position);
        if (file != null) {
            binding.fileNameTextView.setText(file.getName());
            binding.fileSizeTextView.setText(formatFileSize(file.length()));
            binding.dateModifiedTextView.setText(formatDate(file.lastModified()));
            binding.fileTypeTextView.setText(getFileType(file));
        }

        return convertView;
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));

        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private String getFileType(DocumentFile file) {
        String fileName = file.getName();
        assert fileName != null;
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1).toUpperCase();
        }
        return "Unknown";
    }
}
