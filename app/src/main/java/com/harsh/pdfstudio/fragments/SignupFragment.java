package com.harsh.pdfstudio.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.controllers.PdfLoaderController;
import com.harsh.pdfstudio.databinding.CreatePdfSignupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;


public class SignupFragment extends Fragment {

    private CreatePdfSignupBinding binding;
    private FirebaseAuth auth;

    private PdfLoaderController loaderController ;

    public interface SignupActionListener {

        void onLoginClicked();

        void onGoogleClicked();

        void onAppleClicked();
    }

    private SignupActionListener listener;

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        if (context instanceof SignupActionListener) {
            listener = (SignupActionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public void setSignupActionListener(SignupActionListener listener) {

        this.listener = listener;

    }


    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = CreatePdfSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        setupTermsText();

        loaderController =  new PdfLoaderController(R.animator.loader_dot_pulse ,
                binding.loaderLayout.dot1,
                binding.loaderLayout.dot2 ,
                binding.loaderLayout.dot3
        );

        setupPasswordStrengthMeter();

        auth = FirebaseAuth.getInstance();

        binding.backButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());


        InputFilter.LengthFilter passwordLengthFilter =
                new InputFilter.LengthFilter(64);

        binding.passwordEditText.setFilters(
                new InputFilter[]{passwordLengthFilter}
        );

        binding.confirmPasswordEditText.setFilters(
                new InputFilter[]{passwordLengthFilter}
        );


        binding.signUpButton.setOnClickListener(v -> {

            String fullName = binding.fullNameEditText.getText() != null
                    ? binding.fullNameEditText.getText().toString().trim()
                    : "";

            String email = binding.emailEditText.getText() != null
                    ? binding.emailEditText.getText().toString().trim()
                    : "";

            String password = binding.passwordEditText.getText() != null
                    ? binding.passwordEditText.getText().toString()
                    : "";

            String confirmPassword = binding.confirmPasswordEditText.getText() != null
                    ? binding.confirmPasswordEditText.getText().toString()
                    : "";

            // ================= VALIDATION =================

            if (fullName.isEmpty()) {
                binding.fullNameInputLayout.setError("Enter your full name");
                return;
            }

            binding.fullNameInputLayout.setError(null);

            if (email.isEmpty()) {
                binding.emailInputLayout.setError("Enter your email");
                return;
            }

            binding.emailInputLayout.setError(null);

            if (password.isEmpty()) {
                binding.passwordInputLayout.setError("Enter a password");
                return;
            }

            binding.passwordInputLayout.setError(null);

            if (password.length() < 6) {
                binding.passwordInputLayout.setError(
                        "Password must be at least 6 characters"
                );
                return;
            }

            binding.passwordInputLayout.setError(null);

            if (!password.equals(confirmPassword)) {

                binding.confirmPasswordInputLayout.setError(
                        "Passwords do not match"
                );
                return;
            }

            binding.confirmPasswordInputLayout.setError(null);

            if (!binding.termsCheckBox.isChecked()) {

                Toast.makeText(
                        requireContext(),
                        "Please accept the Terms of Service and Privacy Policy",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // ================= FIREBASE ACCOUNT CREATION =================

            binding.signUpButton.setEnabled(false);

            showLoader();

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(requireActivity(), task -> {

                        if (!task.isSuccessful()) {

                            hideLoader();

                            Exception exception = task.getException();

                            if (exception != null) {


                                showSignupError(exception);


                            }

                            return;
                        }

                        FirebaseUser user = auth.getCurrentUser();

                        if (user == null) {

                            hideLoader();

                            Toast.makeText(
                                    requireContext(),
                                    "Account creation failed. Please try again.",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        UserProfileChangeRequest profileUpdates =
                                new UserProfileChangeRequest.Builder()
                                        .setDisplayName(fullName)
                                        .build();

                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(profileTask -> {

                                    if (!profileTask.isSuccessful()) {

                                        hideLoader();

                                        Toast.makeText(
                                                requireContext(),
                                                "Could not save your profile name.",
                                                Toast.LENGTH_LONG
                                        ).show();

                                        return;
                                    }

                                    user.sendEmailVerification()
                                            .addOnCompleteListener(verificationTask -> {

                                                hideLoader();

                                                if (verificationTask.isSuccessful()) {

                                                    Toast.makeText(
                                                            requireContext(),
                                                            "Account created. Please verify your email.",
                                                            Toast.LENGTH_LONG
                                                    ).show();

                                                    if (listener != null) {
                                                        listener.onLoginClicked();
                                                    }

                                                } else {

                                                    Toast.makeText(
                                                            requireContext(),
                                                            "Account created, but verification email could not be sent.",
                                                            Toast.LENGTH_LONG
                                                    ).show();
                                                }

                                            });
                                });
                    });


        });


        binding.googleButton.setOnClickListener(v -> {
            // TODO: trigger Google sign-up flow
        });


        binding.appleButton.setOnClickListener(v -> {
            // TODO: trigger Apple sign-up flow
        });


        binding.loginLink.setOnClickListener(v ->{

            if (listener != null) {
                listener.onLoginClicked();
            } else if (getActivity() instanceof SignupActionListener) {
                ((SignupActionListener) getActivity()).onLoginClicked();
            }

        });

    }

    /**
     * Turns "Terms of Service" and "Privacy Policy" inside the shared
     * R.string.agree_to_terms string into clickable, primary-blue spans.
     */
    private void setupTermsText() {

        String fullText = getString(R.string.agree_to_terms).trim();
        String termsOfService = getString(R.string.terms_of_service);
        String privacyPolicy = getString(R.string.privacy_policy);

        SpannableString spannable = new SpannableString(fullText);
        int linkColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary);

        applyClickableSpan(spannable, fullText, termsOfService, linkColor, () -> {
            // TODO: open Terms of Service screen / URL
        });
        applyClickableSpan(spannable, fullText, privacyPolicy, linkColor, () -> {
            // TODO: open Privacy Policy screen / URL
        });

        binding.termsText.setText(spannable);
        binding.termsText.setMovementMethod(LinkMovementMethod.getInstance());
        binding.termsText.setHighlightColor(android.graphics.Color.TRANSPARENT);
    }

    private void applyClickableSpan(
            SpannableString spannable,
            String fullText,
            String target,
            int color,
            Runnable onClick
    ) {

        int start = fullText.indexOf(target);
        if (start == -1) return;
        int end = start + target.length();

        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                onClick.run();
            }

            @Override
            public void updateDrawState(@NonNull android.text.TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(color);
                ds.setUnderlineText(false);
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }


    private void setupPasswordStrengthMeter() {

        binding.passwordEditText.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        updatePasswordStrength(s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );
    }


    private void updatePasswordStrength(String password) {

        int strength = calculatePasswordStrength(password);

        updateStrengthSegments(strength);
        updateStrengthText(strength);
    }

    private int calculatePasswordStrength(String password) {

        if (password.isEmpty()) {
            return 0;
        }

        int score = 0;

        // Length
        if (password.length() >= 6) {
            score++;
        }

        if (password.length() >= 10) {
            score++;
        }

        // Lowercase + uppercase
        if (password.matches(".*[a-z].*")
                && password.matches(".*[A-Z].*")) {
            score++;
        }

        // Number
        if (password.matches(".*\\d.*")) {
            score++;
        }

        // Special character
        if (password.matches(".*[^a-zA-Z0-9].*")) {
            score++;
        }

        /*
         * Convert the 5-point score into
         * our 4-segment meter.
         */
        if (score <= 1) {
            return 1;
        } else if (score == 2) {
            return 2;
        } else if (score == 3 || score == 4) {
            return 3;
        } else {
            return 4;
        }
    }


    private void updateStrengthText(int strength) {

        int textColor;

        switch (strength) {

            case 0:
                binding.strengthValueText.setText("None");
                textColor = ContextCompat.getColor(
                        requireContext(),
                        R.color.colorTextSecondary
                );
                break;

            case 1:
                binding.strengthValueText.setText("Weak");
                textColor = ContextCompat.getColor(
                        requireContext(),
                        R.color.password_strength_weak
                );
                break;

            case 2:
                binding.strengthValueText.setText("Medium");
                textColor = ContextCompat.getColor(
                        requireContext(),
                        R.color.password_strength_medium
                );
                break;

            case 3:
                binding.strengthValueText.setText("Strong");
                textColor = ContextCompat.getColor(
                        requireContext(),
                        R.color.password_strength_strong
                );
                break;

            default:
                binding.strengthValueText.setText("Very Strong");
                textColor = ContextCompat.getColor(
                        requireContext(),
                        R.color.password_strength_strong
                );
                break;
        }

        binding.strengthValueText.setTextColor(textColor);
    }


    private void updateStrengthSegments(int strength) {

        int filledColor;
        int trackColor = ContextCompat.getColor(
                requireContext(),
                R.color.colorBorder
        );

        if (strength == 1) {

            // Weak
            filledColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.password_strength_weak
            );

        } else if (strength == 2) {

            // Medium
            filledColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.password_strength_medium
            );

        } else {

            // Strong / Very Strong
            filledColor = ContextCompat.getColor(
                    requireContext(),
                    R.color.password_strength_strong
            );
        }

        View[] segments = {
                binding.strengthSegmentOne,
                binding.strengthSegmentTwo,
                binding.strengthSegmentThree,
                binding.strengthSegmentFour
        };

        for (int i = 0; i < segments.length; i++) {

            int color = i < strength
                    ? filledColor
                    : trackColor;

            segments[i].getBackground().setTint(color);
        }
    }


    private void showLoader(){

        if (binding != null){

            binding.loaderLayout.getRoot().setVisibility(View.VISIBLE);
            binding.loaderLayout.getRoot().setClickable(true);
            binding.loaderLayout.getRoot().setFocusable(true);
            binding.loaderLayout.getRoot().requestFocus();

            loaderController.showLoader();

            setLoading(true);

        }

    }


    private void hideLoader(){

        if (binding != null){

            binding.loaderLayout.getRoot().setVisibility(View.GONE);

            binding.loaderLayout.getRoot().setClickable(false);
            binding.loaderLayout.getRoot().setFocusable(false);


            loaderController.hideLoader();

            setLoading(false);

        }

    }


    private void setLoading(boolean loading) {

        binding.signUpButton.setEnabled(!loading);

        binding.fullNameEditText.setEnabled(!loading);
        binding.emailEditText.setEnabled(!loading);
        binding.passwordEditText.setEnabled(!loading);
        binding.confirmPasswordEditText.setEnabled(!loading);
        binding.termsCheckBox.setEnabled(!loading);
        binding.googleButton.setEnabled(!loading);
        binding.appleButton.setEnabled(!loading);
        binding.loginLink.setEnabled(!loading);
        binding.backButton.setEnabled(!loading);

    }


    private void showSignupError(Exception exception) {

        String message = "Unable to create account";

        if (exception instanceof FirebaseAuthUserCollisionException) {

            message = "An account already exists with this email";

        } else if (exception instanceof FirebaseAuthWeakPasswordException) {

            message = "Password is too weak";

        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {

            message = "Please enter a valid email address";
        }

        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_LONG
        ).show();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}