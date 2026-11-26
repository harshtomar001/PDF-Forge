package com.harsh.pdfstudio.ui;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.controllers.PdfLoaderController;
import com.harsh.pdfstudio.databinding.SecurityActivityBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SecurityActivity extends FontBaseActivity {

    private SecurityActivityBinding binding;

    private FirebaseAuth auth;

    private PdfLoaderController  loaderController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        binding = SecurityActivityBinding.inflate(getLayoutInflater());

        loaderController = new PdfLoaderController(R.animator.loader_dot_pulse ,
                binding.loaderLayout.dot1,
                binding.loaderLayout.dot2,
                binding.loaderLayout.dot3
        );

        if (binding.toolbar != null) {
            ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar,
                    (v, insets) -> {

                        Insets systemBars =
                                insets.getInsets(WindowInsetsCompat.Type.statusBars());

                        v.setPadding(
                                v.getPaddingLeft(),
                                (int) (systemBars.top / 1.7f) ,
                                v.getPaddingRight(),
                                v.getPaddingBottom()
                        );

                        return insets;
                    });
        }

        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        bindSecurityData();

        setupClickListeners();
    }


    private void bindSecurityData() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            finish();
            return;
        }

        // Email

        String email = user.getEmail();

        binding.securityEmail.setText(
                email != null ? email : "Not available"
        );


        // Email verification

        if (user.isEmailVerified()) {

            binding.emailVerificationStatus.setText(
                    "✓ Email verified"
            );

            binding.emailVerificationStatus.setTextColor(
                    ContextCompat.getColor(
                            this,
                            R.color.password_strength_strong
                    )
            );

        }
        else {

            binding.emailVerificationStatus.setText(
                    "⚠ Email not verified"
            );

            binding.emailVerificationStatus.setTextColor(
                    ContextCompat.getColor(
                            this,
                            R.color.password_strength_weak
                    )
            );
        }


        // Last sign-in

        long timestamp =
                user.getMetadata().getLastSignInTimestamp();

        binding.lastSignInText.setText(
                formatDate(timestamp)
        );
    }


    private void setupClickListeners() {

        binding.securityBackButton.setOnClickListener(v ->
                finish()
        );


        binding.changePasswordCard.setOnClickListener(v ->
                sendPasswordResetEmail()
        );


        binding.deleteAccountButton.setOnClickListener(v ->
                showDeleteAccountDialog()
        );
    }


    private void sendPasswordResetEmail() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null || user.getEmail() == null) {
            return;
        }

        String email = user.getEmail();

        showLoader();

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        hideLoader();

                        Toast.makeText(
                                this,
                                "Password reset email sent.",
                                Toast.LENGTH_LONG
                        ).show();

                    }
                    else {

                        hideLoader();

                        Toast.makeText(
                                this,
                                "Could not send password reset email.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                });
    }


    private void showDeleteAccountDialog() {

        MaterialAlertDialogBuilder builder =  new MaterialAlertDialogBuilder(this)
                .setTitle("Delete account?")
                .setMessage(
                        "This will permanently delete your account. " +
                                "This action cannot be undone."
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> deleteAccount()
                );

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d -> {

            TextView title = dialog.findViewById(
                    com.google.android.material.R.id.alertTitle
            );


            if (title != null) {
                title.setTextColor(
                        ContextCompat.getColor(
                                this,
                                R.color.colorTextPrimary
                        )
                );
            }

            // Message
            TextView message = dialog.findViewById(
                    android.R.id.message
            );

            if (message != null) {
                message.setTextColor(
                        ContextCompat.getColor(
                                this,
                                R.color.colorTextSecondary
                        )
                );
            }


            GradientDrawable background = new GradientDrawable();
            background.setColor(
                    ContextCompat.getColor(
                            this,
                            R.color.colorSurface
                    )
            );
            background.setCornerRadius(28f);

            dialog.getWindow().setBackgroundDrawable(background);

            // Positive button
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(
                            ContextCompat.getColor(
                                    this,
                                    R.color.pdf_red_pressed
                            )
                    );

            // Negative button
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(
                            ContextCompat.getColor(
                                    this,
                                    R.color.colorTextSecondary
                            )
                    );
        });

        dialog.show();


    }


    private void deleteAccount() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            return;
        }

        showLoader();

        user.delete()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        hideLoader();

                        Toast.makeText(
                                this,
                                "Account deleted.",
                                Toast.LENGTH_SHORT
                        ).show();



                        Intent intent = new Intent(this, AuthActivity.class);

                        intent.addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                        );

                        startActivity(intent);

                    }
                    else {

                        hideLoader();

                        Toast.makeText(
                                this,
                                "Please sign in again before deleting your account.",
                                Toast.LENGTH_LONG
                        ).show();

                    }

                });
    }


    private String formatDate(long timestamp) {

        if (timestamp <= 0) {
            return "Not available";
        }

        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "dd MMMM yyyy, hh:mm a",
                        Locale.getDefault()
                );

        return formatter.format(
                new Date(timestamp)
        );
    }

    private void showLoader(){

        if (binding != null){

            binding.loaderLayout.getRoot().setVisibility(View.VISIBLE);
            binding.loaderLayout.getRoot().setFocusable(true);
            binding.loaderLayout.getRoot().setClickable(true);
            binding.loaderLayout.getRoot().requestFocus();

            loaderController.showLoader();

        }

    }

    private void hideLoader(){

        if (binding != null){

            binding.loaderLayout.getRoot().setVisibility(View.GONE);

            loaderController.hideLoader();

        }


    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        binding = null;
    }
}
