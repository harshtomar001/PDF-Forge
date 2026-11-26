package com.harsh.pdfstudio.ui;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.harsh.pdfstudio.R;

public class ImageViewActivity extends AppCompatActivity {

            @Override
    protected void  onCreate(Bundle savedInstanceState){
                super.onCreate(savedInstanceState);

                setContentView(R.layout.image_view_activity);

                ImageView imageView=findViewById(R.id.image_view_activity);


                Uri uri= getIntent().getParcelableExtra("image_uri");

                Glide.with(this).load(uri).into(imageView);

            }


}
