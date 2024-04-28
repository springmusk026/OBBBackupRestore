package com.obb.backup.restore.activity.auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import com.obb.backup.restore.databinding.ActivitySignUpBinding;
public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;

    private FirebaseAuth auth;
    private EditText signupEmail, signupPassword;
    private Button signupButton;
    private TextView loginRedirectText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        auth = FirebaseAuth.getInstance();
        signupEmail = binding.signupEmail;
        signupPassword = binding.signupPassword;
        signupButton = binding.signupButton;
        loginRedirectText = binding.loginRedirectText;

        signupButton.setOnClickListener(view12 -> {
            String user = signupEmail.getText().toString().trim();
            String pass = signupPassword.getText().toString().trim();

            if (user.isEmpty()){
                signupEmail.setError("Email cannot be empty");
            }
            if (pass.isEmpty()){
                signupPassword.setError("Password cannot be empty");
            } else{
                auth.createUserWithEmailAndPassword(user, pass).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Snackbar.make(view, "SignUp Successful", Snackbar.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                    } else {
                        Snackbar.make(view, "SignUp Failed" + task.getException().getMessage(),Snackbar.LENGTH_SHORT).show();
                    }
                });
            }

        });

        loginRedirectText.setOnClickListener(view1 -> startActivity(new Intent(SignUpActivity.this, LoginActivity.class)));

    }
}