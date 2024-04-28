package com.obb.backup.restore.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.obb.backup.restore.model.Product;
import com.obb.backup.restore.utils.AppConfig;
import com.obb.backup.restore.utils.DownloadManagerHelper;
import com.obb.backup.restore.utils.SharedPref;
import com.obb.backup.restore.BuildConfig;
import com.obb.backup.restore.R;
import com.obb.backup.restore.databinding.ActivityDetailBinding;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private DownloadManagerHelper downloadManagerHelper;

    private SharedPref sharedPref;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        sharedPref = new SharedPref(this);

        binding.mainfoCardView.setCardBackgroundColor(Color.DKGRAY);

        downloadManagerHelper = new DownloadManagerHelper(this);

        // Enable the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Set a click listener to the back button
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        Product product = getIntent().getParcelableExtra("product");
        if (product != null) {
            binding.productNameTextView.setText("Mod Name : "+product.getName());
            binding.productNameTextView.setTextColor(Color.RED);

            binding.productVersionTextView.setText("Version : "+product.getVersion());
            binding.productVersionTextView.setTextColor(Color.GREEN);

            binding.productSizeTextView.setText("Size : "+product.getSize());
            binding.productSizeTextView.setTextColor(Color.GREEN);

            binding.productDeveloperTextView.setText("Developer : "+product.getDeveloper());
            binding.productDeveloperTextView.setTextColor(Color.GREEN);

            binding.productStatusTextView.setText("Status : "+product.getStatus());
            binding.productStatusTextView.setTextColor(Color.GREEN);

            binding.productDescriptionTextView.setText(Html.fromHtml(product.getDescription()));

            Glide.with(this)
                    .load(product.getImageUrl())
                    .centerInside()
                    .placeholder(R.drawable.logo)
                    .error(R.drawable.error_image)
                    .into(binding.productImage);

            int timerValue = product.getTimer();

            binding.downloadButton.setOnClickListener(v -> {
                String downloadUrl = product.getDownloadUrl();
                if (downloadUrl != null && !downloadUrl.isEmpty()) {
                    countDownTimer = new CountDownTimer(timerValue * 1000L, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            int progress = (int) (((timerValue * 1000L - millisUntilFinished) / (float) (timerValue * 1000L)) * 100);
                            binding.downloadButton.setText("Please Wait " + String.valueOf(progress) +"% completed");
                            binding.downloadButton.setEnabled(false);
                            binding.downloadButton.setTextColor(Color.RED);
                        }

                        @Override
                        public void onFinish() {
                            binding.downloadButton.setText("Download Started");
                            downloadManagerHelper.fetchFileDetailsAndStartDownload(downloadUrl);
                            binding.downloadButton.setEnabled(true);
                        }
                    }.start();
                } else {
                    Snackbar.make(binding.getRoot(), "Download URL is empty", Snackbar.LENGTH_SHORT).show();
                }
            });

        }
    }
    @Override
    public void onDestroy(){
        finish();
        super.onDestroy();
    }
}
