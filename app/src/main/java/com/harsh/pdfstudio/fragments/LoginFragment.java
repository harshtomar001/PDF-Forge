package com.harsh.pdfstudio.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.controllers.PdfLoaderController;
import com.harsh.pdfstudio.databinding.CreatePdfLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.harsh.pdfstudio.ui.MainActivity;

public class LoginFragment extends Fragment {

    public interface  loginActionListener{

        void onGoogleClicked();
        void onSignUpClicked();
        void onForgotPasswordClicked();

    }

    private loginActionListener listener;

    private PdfLoaderController loaderController;

    public void setLoginActionListener(loginActionListener listener){
        this.listener = listener;
    }

    private CreatePdfLoginBinding binding;
    private FirebaseAuth auth;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = CreatePdfLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();


        binding.backButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        loaderController = new PdfLoaderController(R.animator.loader_dot_pulse ,
                binding.loaderLayout.dot1 ,
                binding.loaderLayout.dot2,
                binding.loaderLayout.dot3
                );


        binding.loginButton.setOnClickListener(v -> {

            String email = binding.emailEditText.getText() != null
                    ? binding.emailEditText.getText().toString().trim()
                    : "";

            String password = binding.passwordEditText.getText() != null
                    ? binding.passwordEditText.getText().toString()
                    : "";

            // ---------- VALIDATION ----------

            if (email.isEmpty()) {

                binding.emailInputLayout.setError("Enter your email");
                return;

            }

            binding.emailInputLayout.setError(null);

            if (password.isEmpty()) {

                binding.passwordInputLayout.setError("Enter your password");
                return;

            }

            binding.passwordInputLayout.setError(null);

            // ---------- FIREBASE LOGIN ----------

            showLoader();

            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity(), task -> {

                        if (task.isSuccessful()) {

                            FirebaseUser user = auth.getCurrentUser();

                            if (user != null) {

                                if (user.isEmailVerified()) {

                                    String userName = user.getDisplayName();
                                    String userEmail = user.getEmail();


                                    // Email verified → allow access
                                    hideLoader();

                                    Intent intent = new Intent(requireContext(), MainActivity.class);

                                    intent.addFlags(
                                            Intent.FLAG_ACTIVITY_NEW_TASK |
                                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    );

                                    startActivity(intent);

                                }
                                else {

                                    // Email not verified
                                    hideLoader();

                                    Toast.makeText(
                                            requireContext(),
                                            "Please verify your email before logging in.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    auth.signOut();
                                }

                            }
                            else {

                                hideLoader();

                                Toast.makeText(
                                        requireContext(),
                                        "Unable to sign in. Please try again.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }

                        }

                        else {

                            hideLoader();

                            Exception exception = task.getException();

                            if (exception != null) {
                                showLoginError(exception);
                            }
                        }


                    });
        });


        binding.googleButton.setOnClickListener(v -> {

            listener.onGoogleClicked();

        });

        binding.appleButton.setOnClickListener(v -> {

            // TODO: trigger Apple sign-in flow

        });

        binding.forgotPassword.setOnClickListener(v -> {

            listener.onForgotPasswordClicked();

        });

        binding.signUpLink.setOnClickListener(v ->{

            listener.onSignUpClicked();

        });

    }


    private void showLoader(){

        if (binding != null){

            binding.loaderLayout.getRoot().setVisibility(View.VISIBLE);
            binding.loaderLayout.getRoot().setClickable(true);
            binding.loaderLayout.getRoot().setFocusable(true);
            binding.loaderLayout.getRoot().requestFocus();

            loaderController.showLoader();

        }

    }

    private void hideLoader(){

        if (binding != null){

            binding.loaderLayout.getRoot().setVisibility(View.GONE);

            binding.loaderLayout.getRoot().setClickable(false);
            binding.loaderLayout.getRoot().setFocusable(false);


            loaderController.hideLoader();


        }

    }

    private void showLoginError(Exception exception) {

        String message = "Login failed";

        if (exception instanceof FirebaseAuthInvalidUserException) {

            message = "No account found with this email";

        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {

            message = "Incorrect email or password";

        }

        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
