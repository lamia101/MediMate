package com.example.personalhealthcareapplication.view;

import static android.content.ContentValues.TAG;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.personalhealthcareapplication.MainActivity;
import com.example.personalhealthcareapplication.R;
import com.example.personalhealthcareapplication.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class SignUp extends AppCompatActivity {

    private TextInputEditText etName, etMobile, etEmail, etDOB, etWeight, etHeight;
    private Spinner spinnerMedicalConditions;
    private MaterialButton btnSignUp;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        etName = findViewById(R.id.etName);
        etMobile = findViewById(R.id.etMobile);
        etEmail = findViewById(R.id.etEmail);
        etDOB = findViewById(R.id.etDOB);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        spinnerMedicalConditions = findViewById(R.id.spinnerMedicalConditions);
        btnSignUp = findViewById(R.id.btnSignUp);

        // Setup Medical Conditions Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.medical_conditions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMedicalConditions.setAdapter(adapter);

        // Set Date Picker on DOB field
        etDOB.setOnClickListener(this::showDatePicker);

        // Handle Sign-Up Button Click
        btnSignUp.setOnClickListener(v -> registerUser());
    }

    private void showDatePicker(View view) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view1, int year1, int monthOfYear, int dayOfMonth) -> {
                    String dob = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year1;
                    etDOB.setText(dob);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();
        String weight = etWeight.getText().toString().trim();
        String height = etHeight.getText().toString().trim();
        String medicalCondition = spinnerMedicalConditions.getSelectedItem().toString();

        // Validate inputs
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(mobile)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a Firebase user with email and password
        mAuth.createUserWithEmailAndPassword(email, "defaultPassword") // Consider adding password input in production apps
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Get the authenticated user
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Save user info in Firebase Database
                            System.out.println(user.getUid());
                            saveUserInfo(user.getUid(), name, mobile, email, dob, weight, height, Collections.singletonList(medicalCondition));
                        }
                    } else {
                        handleSignUpError(task.getException());
                    }
                });
    }

    private void saveUserInfo(String uid, String name, String mobile, String email,
                              String dob, String weight, String height, List<String> condition) {
        // Create a User object or use a Map to store data
        User user = new User(name, mobile, email, dob, weight, height, condition);
        System.out.println(user);

        db.collection("Users")
                .document(uid) // Use the uid as the document ID
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SignUp.this, "Sign-up successful!", Toast.LENGTH_SHORT).show();
                        navigateToMainActivity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private void handleSignUpError(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            Toast.makeText(this, "This email is already registered.", Toast.LENGTH_SHORT).show();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(this, "Invalid email format.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Sign-up failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(SignUp.this, MainActivity.class);
        startActivity(intent);
        finish(); // Close SignUpActivity to prevent going back to it
    }
}