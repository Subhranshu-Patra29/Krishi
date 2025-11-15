package com.subha.krishi;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {
    private EditText etPhone;
    private Button btnSend;
    private ProgressBar progress;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etPhone = findViewById(R.id.etPhone);
        btnSend = findViewById(R.id.btnSend);
        progress = findViewById(R.id.progress);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        setupCallbacks();

        btnSend.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (TextUtils.isEmpty(phone)) {
                etPhone.setError("Enter phone number");
                return;
            }
            startPhoneAuth(phone);
        });
    }

    private void setupCallbacks() {
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                // Auto-retrieval or instant verification
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                progress.setVisibility(View.GONE);
                System.out.println("Verification failed: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
                Toast.makeText(LoginActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCodeSent(@NonNull String verId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                super.onCodeSent(verId, token);
                progress.setVisibility(View.GONE);
                verificationId = verId;
                resendToken = token;
                showOtpDialog();
            }
        };
    }

    private void startPhoneAuth(String phoneNumber) {
        // show progress
        progress.setVisibility(View.VISIBLE);

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void showOtpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_otp, null);
        EditText etOtp = view.findViewById(R.id.etOtp);
        Button btnVerify = view.findViewById(R.id.btnVerify);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        btnVerify.setOnClickListener(v -> {
            String code = etOtp.getText().toString().trim();
            if (TextUtils.isEmpty(code)) {
                etOtp.setError("Enter OTP");
                return;
            }
            progress.setVisibility(View.VISIBLE);
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInWithPhoneAuthCredential(credential);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progress.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            checkAndCreateProfile(user.getUid(), user.getPhoneNumber());
                        }
                    } else {
                        System.out.println("Sign in failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        Toast.makeText(LoginActivity.this, "Sign in failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void checkAndCreateProfile(String uid, String phoneNumber) {
        DocumentReference ref = db.collection("users").document(uid);
        progress.setVisibility(View.VISIBLE);
        ref.get().addOnSuccessListener(documentSnapshot -> {
            progress.setVisibility(View.GONE);
            if (documentSnapshot.exists()) {
                // Existing user -> proceed to main app
                Toast.makeText(LoginActivity.this, "Welcome back!", Toast.LENGTH_SHORT).show();
                goToMain();
            } else {
                // First time -> ask for name and save
                askForNameAndSave(uid, phoneNumber);
            }
        }).addOnFailureListener(e -> {
            progress.setVisibility(View.GONE);
            System.out.println("Error checking profile: " + e.getMessage());
            Toast.makeText(LoginActivity.this, "Error checking profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void askForNameAndSave(String uid, String phoneNumber) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_name, null);
        EditText etName = view.findViewById(R.id.etName);
        Button btnOk = view.findViewById(R.id.btnOk);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        btnOk.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                etName.setError("Enter name");
                return;
            }
            dialog.dismiss();
            saveProfile(uid, name, phoneNumber);
        });

        dialog.show();
    }

    private void saveProfile(String uid, String name, String phoneNumber) {
        progress.setVisibility(View.VISIBLE);
        DocumentReference ref = db.collection("users").document(uid);
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("phone", phoneNumber);
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("uuid", UUID.randomUUID().toString());

        ref.set(data)
                .addOnSuccessListener(aVoid -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Profile created", Toast.LENGTH_SHORT).show();
                    goToMain();
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, "Error saving profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void goToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();

    }
}
