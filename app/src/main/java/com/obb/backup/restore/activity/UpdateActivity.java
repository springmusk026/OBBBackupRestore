package com.obb.backup.restore.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.obb.backup.restore.databinding.UpdateLayoutBinding;

import java.util.Locale;

public class UpdateActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UpdateLayoutBinding binding = UpdateLayoutBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        int currentVersionCode = Integer.parseInt(getIntent().getStringExtra("currentVersionCode"));

        binding.updateMessage.setText(String.format(Locale.ENGLISH,"A new version %d of the app is available. Update now for improved features and bug fixes!",currentVersionCode));
        binding.updateButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.obb.backuprestore"));
            startActivity(intent);
        });

    }
}
