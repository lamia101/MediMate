package com.example.personalhealthcareapplication.repository;

import com.example.personalhealthcareapplication.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserRepository {
    private final FirebaseAuth auth;
    private final DatabaseReference database;

    public UserRepository() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference("Users");
    }

    public void createUser(User user) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();
            database.child(userId).setValue(user);
        }
    }

    public FirebaseAuth getAuth() {
        return auth;
    }
}