package com.harsh.pdfstudio.ui;

import android.content.Context;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatActivity;

public class FontBaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {

        Configuration configuration =
                new Configuration(newBase.getResources().getConfiguration());

        configuration.fontScale =
                Math.min(configuration.fontScale, 1.1f);

        Context context =
                newBase.createConfigurationContext(configuration);

        super.attachBaseContext(context);
    }


}
