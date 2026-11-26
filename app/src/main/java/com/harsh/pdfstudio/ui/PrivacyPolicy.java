package com.harsh.pdfstudio.ui;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.harsh.pdfstudio.R;

public class PrivacyPolicy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_policy);

        TextView privacyPolicy=findViewById(R.id.privacyPolicy_textView);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            privacyPolicy.setText(Html.fromHtml(getString(R.string.privacy_policy_html), Html.FROM_HTML_MODE_COMPACT));
        } else {
            privacyPolicy.setText(Html.fromHtml(getString(R.string.privacy_policy_html)));
        }
    }
}