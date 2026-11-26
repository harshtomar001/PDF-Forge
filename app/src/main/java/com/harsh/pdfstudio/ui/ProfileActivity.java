package com.harsh.pdfstudio.ui;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.databinding.ProfilePageBinding;
import com.harsh.pdfstudio.notifications.NotificationActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class ProfileActivity extends AppCompatActivity {

    private ProfilePageBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        binding = ProfilePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bindUserData();
        setClickListeners();
    }


    private void bindUserData() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null){

            binding.profileName.setText(user.getDisplayName());
            binding.profileEmail.setText(user.getEmail());

            binding.premiumBadge.setVisibility(View.GONE);

        }


    }

    private void setClickListeners() {

        binding.profileBackButton.setOnClickListener(v -> onBackPressed());

        binding.editProfileButton.setOnClickListener(v ->
                Toast.makeText(this, "Edit profile photo", Toast.LENGTH_SHORT).show());

        binding.profileNotificationsButton.setOnClickListener(v ->{

            Intent intent = new Intent(this, NotificationActivity.class);
            startActivity(intent);

        });

        binding.upgradeButton.setOnClickListener(v -> openUpgradeScreen());

        binding.profilePersonalInfo.setOnClickListener(v -> openPersonalInformation());

        binding.profileSubscription.setOnClickListener(v -> openSubscription());

        binding.profileBilling.setOnClickListener(v -> openBillingAndPayments());

        binding.profileSecurity.setOnClickListener(v -> openSecurity());

        binding.profileNotifications.setOnClickListener(v -> openNotificationSettings());

        binding.profileAppSettings.setOnClickListener(v -> openAppSettings());

        binding.profileHelpSupport.setOnClickListener(v -> openHelpAndSupport());

        binding.profileAbout.setOnClickListener(v -> openAbout());

        binding.profileAppStorage.setOnClickListener(v -> openAppStorage());

        binding.logoutButton.setOnClickListener(v ->showLogoutDialog());
    }

    private void openUpgradeScreen() {

        Intent intent = new Intent(this  , UpgradeActivity.class);
        startActivity(intent);

    }

    private void openPersonalInformation() {

        FirebaseUser user =
                FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            return;
        }

        View view = getLayoutInflater()
                .inflate(R.layout.dialog_personal_information, null);

        TextView name = view.findViewById(R.id.dialogName);
        TextView email = view.findViewById(R.id.dialogEmail);
        TextView created = view.findViewById(R.id.dialogCreated);
        TextView lastSignIn = view.findViewById(R.id.dialogLastSignIn);
        TextView verification = view.findViewById(R.id.dialogVerification);
        TextView uid = view.findViewById(R.id.dialogUid);

        // Name
        String displayName = user.getDisplayName();

        name.setText(
                "Name\n" +
                        (displayName != null && !displayName.isEmpty()
                                ? displayName
                                : "Not available")
        );

        // Email
        email.setText(
                "Email\n" +
                        (user.getEmail() != null
                                ? user.getEmail()
                                : "Not available")
        );

        // Creation date
        long creationTimestamp =
                user.getMetadata().getCreationTimestamp();

        created.setText(
                "Account created\n" +
                        formatDate(creationTimestamp)
        );

        // Last sign-in
        long lastSignInTimestamp =
                user.getMetadata().getLastSignInTimestamp();

        lastSignIn.setText(
                "Last sign-in\n" +
                        formatDate(lastSignInTimestamp)
        );

        // Email verification
        verification.setText(
                "Email verification\n" +
                        (user.isEmailVerified()
                                ? "Verified"
                                : "Not verified")
        );

        // UID
        uid.setText(
                "User ID\n" +
                        user.getUid()
        );

        MaterialAlertDialogBuilder builder =
                new MaterialAlertDialogBuilder(this);

        builder.setView(view)
                .setPositiveButton("Close", null);

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d -> {

            GradientDrawable background = new GradientDrawable();
            background.setColor(
                    ContextCompat.getColor(
                            this,
                            R.color.colorSurface
                    )
            );
            background.setCornerRadius(28f);

            dialog.getWindow().setBackgroundDrawable(background);

            Button closeButton =
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            // Text color
            closeButton.setTextColor(
                    ContextCompat.getColor(
                            this,
                            android.R.color.black
                    )
            );


        });

        dialog.show();
    }

    private void openSubscription() {

        Intent intent = new Intent(this , SubscriptionActivity.class);
        startActivity(intent);

    }

    private void openBillingAndPayments() {

        Intent intent = new Intent(this , BillingPaymentsActivity.class);
        startActivity(intent);

    }

    private void openSecurity() {

        Intent intent = new Intent(this , SecurityActivity.class);
        startActivity(intent);

    }

    private void openNotificationSettings() {

        Intent intent = new Intent(ProfileActivity.this , NotificationActivity.class);
        startActivity(intent);

    }

    private void openAppSettings() {

        Intent intent = new Intent(
                ProfileActivity.this,
                MainActivity.class
        );

        intent.putExtra("open_settings", true);

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(intent);

        finish();
    }

    private void openAppStorage(){

        Intent intent = new Intent(this , AppStorageActivity.class);

        startActivity(intent);
    }

    private void openHelpAndSupport() {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(
                Uri.parse(
                        "https://pdfstudioapp.github.io/pdfstudio.github.io/help.html"
                )
        );

        startActivity(intent);

    }

    private void openAbout() {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(
                Uri.parse(
                        "https://pdfstudioapp.github.io/pdfstudio.github.io/about_pdf_studio.html"
                )
        );

        startActivity(intent);

    }

    private void showLogoutDialog() {

        MaterialAlertDialogBuilder builder =
                new MaterialAlertDialogBuilder(this);

        builder.setTitle("Log out?")
                .setMessage("Are you sure you want to log out of your account?")

                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.dismiss();
                })

                .setPositiveButton("Log out", (dialog, which) -> {

                    FirebaseAuth.getInstance().signOut();

                    Intent intent = new Intent(
                            this,
                            MainActivity.class
                    );

                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                            Intent.FLAG_ACTIVITY_NEW_TASK

                    );

                    startActivity(intent);
                });

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
                                    R.color.colorPrimary
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


    private String formatDate(long timestamp) {

        if (timestamp <= 0) {
            return "Not available";
        }

        Date date = new Date(timestamp);

        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "dd MMMM yyyy, hh:mm a",
                        Locale.getDefault()
                );

        return formatter.format(date);

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }


}
