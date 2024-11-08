package com.example.personalhealthcareapplication.viewmodel;

import androidx.lifecycle.ViewModel;

import com.example.personalhealthcareapplication.model.User;
import com.example.personalhealthcareapplication.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;

public class UserViewModel extends ViewModel {
    private final UserRepository repository;

    public UserViewModel() {
        repository = new UserRepository();
    }

    public void createUser(User user) {
        repository.createUser(user);
    }

    public FirebaseAuth getAuth() {
        return repository.getAuth();
    }
}