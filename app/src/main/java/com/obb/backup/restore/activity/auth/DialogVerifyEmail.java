package com.obb.backup.restore.activity.auth;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.annotation.NonNull;

import com.obb.backup.restore.databinding.DialogVerifyEmailBinding;

import java.util.Objects;

public class DialogVerifyEmail extends Dialog {

    private final FirebaseAuth firebaseAuth;

    public DialogVerifyEmail(@NonNull Context context, FirebaseAuth firebase) {
        super(context);
        firebaseAuth = firebase;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogVerifyEmailBinding binding = DialogVerifyEmailBinding.inflate(getLayoutInflater());

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        View view = binding.getRoot();
        setContentView(view);

        binding.btnCancel.setOnClickListener(v -> dismiss());

        binding.btnVerify.setOnClickListener(v -> {

            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                user.sendEmailVerification()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Snackbar.make(view,  "Verification email sent to :" + user.getEmail(), Snackbar.LENGTH_SHORT).show();
                                dismiss();
                            } else {
                                Snackbar.make(view, "Failed to send verification email." + Objects.requireNonNull(task.getException()).getMessage(),Snackbar.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Snackbar.make(view, "No user found.",Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}
