package com.obb.backup.restore.activity;

import com.obb.backup.restore.fragments.AboutUsFragment;
import com.obb.backup.restore.fragments.ModFragment;
import com.obb.backup.restore.utils.AppConfig;
import com.obb.backup.restore.utils.InAppUpdateManager;
import com.obb.backup.restore.viewpager.CustomViewPager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.obb.backup.restore.R;
import com.obb.backup.restore.databinding.ActivityActiveBinding;
import com.obb.backup.restore.fragments.DownloadFragment;
import com.obb.backup.restore.fragments.HomeFragment;
import com.obb.backup.restore.utils.SharedPref;

public class MainActivity extends AppCompatActivity{
    private static final int PERMISSION_REQUEST_CODE = 1;
    private CustomViewPager viewPager;
    private BottomNavigationView bottomNavigation;

    private SharedPref sharedPref;
    private ActivityActiveBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityActiveBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewPager = binding.viewPager;
        bottomNavigation = binding.bottomNavigation;

        sharedPref = new SharedPref(MainActivity.this);
        setupViewPager();
        setupBottomNavigation();
        requestStoragePermission();
        checkAndRequestPermission();
        createObbBackupFolder();

        InAppUpdateManager.inAppReview(this,this);
    }

    private void setupViewPager() {
        FragmentAdapter adapter = new FragmentAdapter(this.getSupportFragmentManager());
        adapter.addFragment(new HomeFragment());
        adapter.addFragment(new DownloadFragment());
        adapter.addFragment(new ModFragment());
        adapter.addFragment(new AboutUsFragment());

        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // Not needed for swipe detection
            }

            @Override
            public void onPageSelected(int position) {
                bottomNavigation.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Not needed for swipe detection
            }
        });

    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            if(AppConfig.isBackingup || AppConfig.isRestoring){
                return false;
            }
            else if (item.getItemId() == R.id.nav_home) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (item.getItemId() == R.id.nav_download) {
                viewPager.setCurrentItem(1);
                return true;
            }else if (item.getItemId() == R.id.nav_mods) {
                viewPager.setCurrentItem(2);
                return true;
            }else if (item.getItemId() == R.id.nav_aboutus) {
                viewPager.setCurrentItem(3);
                return true;
            }
            return false;
        });
    }

    public void requestStoragePermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", this.getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 1001);
            } else {
                openDirectoryPicker();
            }
        } else {
            openDirectoryPicker();
        }
    }

    private void checkAndRequestPermission() {
        String[] permissions = {
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        if (!hasPermissions(this, permissions)) {
            ActivityCompat.requestPermissions((Activity) this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void createObbBackupFolder() {
        String folderName = "OBBBackup";
        File downloadsDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        File obbBackupFolder = new File(downloadsDirectory, folderName);
        if (!obbBackupFolder.exists()) {
            obbBackupFolder.mkdir();
        }
    }

    private void openDirectoryPicker() {
        StorageManager storageManager = (StorageManager) this.getSystemService(Context.STORAGE_SERVICE);
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            intent = storageManager.getPrimaryStorageVolume().createOpenDocumentTreeIntent();
        }
        String targetDirectory = "Android%2Fobb%2Fcom.dts.freefireth";
        assert intent != null;
        Uri uri = intent.getParcelableExtra("android.provider.extra.INITIAL_URI");
        String scheme = uri.toString();
        scheme = scheme.replace("/root/", "/document/");
        scheme += "%3A" + targetDirectory;
        uri = Uri.parse(scheme);
        intent.putExtra("android.provider.extra.INITIAL_URI", uri);
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK) {
            assert data != null;
            Uri treeUri = data.getData();
            sharedPref.setStoredDirectoryUri(treeUri.toString()); // Store the granted directory URI
        }
    }

    @Override
    public void onBackPressed() {
        if(AppConfig.isBackingup || AppConfig.isRestoring) {
        } else {
            super.onBackPressed();
        }
    }


    private static class FragmentAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragments = new ArrayList<>();

        public FragmentAdapter(FragmentManager fragmentManager) {
            super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        public void addFragment(Fragment fragment) {
            fragments.add(fragment);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
