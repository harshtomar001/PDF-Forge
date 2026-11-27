package com.harsh.pdfstudio.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.databinding.ParentPageBinding;
import com.harsh.pdfstudio.fragments.LoginFragment;
import com.harsh.pdfstudio.fragments.SignupFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends AppCompatActivity
        implements LoginFragment.loginActionListener, SignupFragment.SignupActionListener {

    private ParentPageBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ParentPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.Fragment_container,
                            new LoginFragment(),
                            "LOGIN"
                    )
                    .commit();
        }
    }

    // --- LoginFragment.loginActionListener ---
    @Override
    public void onGoogleClicked() {
    }

    @Override
    public void onSignUpClicked() {
        showSignup();
    }

    @Override
    public void onForgotPasswordClicked() {
    }

    // --- SignupFragment.SignupActionListener ---
    @Override
    public void onLoginClicked() {
        showLogin();
    }

    @Override
    public void onAppleClicked() {
    }

    private void showSignup() {

        FragmentManager fm = getSupportFragmentManager();

        Fragment current = fm.findFragmentById(R.id.Fragment_container);

        // If Signup is already the current fragment, do nothing
        if (current instanceof SignupFragment) {
            return;
        }

        // Remove any previous Signup entry from the back stack
        for (int i = fm.getBackStackEntryCount() - 1; i >= 0; i--) {

            String name = fm.getBackStackEntryAt(i).getName();

            if ("SIGNUP".equals(name)) {
                fm.popBackStack(
                        name,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                );
                break;
            }
        }

        SignupFragment signupFragment = new SignupFragment();

        fm.beginTransaction()
                .replace(
                        R.id.Fragment_container,
                        signupFragment

                )
                .addToBackStack("SIGNUP")
                .commit();
    }

    private void showLogin() {

        FragmentManager fm = getSupportFragmentManager();

        if (fm.getBackStackEntryCount() > 0) {

            fm.popBackStack();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        binding = null;
    }
}