package com.harsh.pdfstudio.ui;

import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.harsh.pdfstudio.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class FeedBack extends FontBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_back);

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://pdfstudio-e6658-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference databaseReference = database.getReference("feedback");


        MaterialToolbar toolbar=findViewById(R.id.back_arrow_feedback);
        setSupportActionBar(toolbar);
        int[] selectedCard=new int[1];
        int [] ratedStar=new int[1];

        MaterialButton submitFeedback=findViewById(R.id.submitFeedBack);

        RatingBar ratingBar =findViewById(R.id.ratingBar);

        EditText  feedbackText=findViewById(R.id.editText);

        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });


        DisplayMetrics metrics = getResources().getDisplayMetrics();

        float screenWidthDp = metrics.widthPixels / metrics.density;

        int spanCount = Math.max(1, (int) ((screenWidthDp - 8) / 104));


        GridLayout gridLayout=findViewById(R.id.gridLayout);

        gridLayout.setColumnCount(spanCount>=4? 4:2);



        CardView featuresCard=findViewById(R.id.featuresCard);
        CardView bugsCard=findViewById(R.id.bugsCard);
        CardView supportCard=findViewById(R.id.supportCard);
        CardView otherCard=findViewById(R.id.otherCard);
//        TextView featuresTextBox=findViewById(R.id.featuresTextBox);
//        TextView bugsTextBox=findViewById(R.id.bugsTextBox);
//        TextView supportTextBox=findViewById(R.id.supportTextBox);
//        TextView othersTextBox=findViewById(R.id.othersTextBox);

        CardView[] cards={featuresCard,bugsCard,supportCard,otherCard};
//        TextView[] textViews={featuresTextBox,bugsTextBox,supportTextBox,othersTextBox};



        ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {

           if (fromUser){
                   ratedStar[0]=(int) rating;
           }

        });


        int[] whichOne={-1};
        for(int i=0;i<cards.length;i++){

            int index=i;

            cards[i].setOnClickListener(v -> {

                for (int j=0;j<cards.length;j++){

//                    cards[j].setCardBackgroundColor(
//                            ContextCompat.getColor(this , R.drawable.card_default));// accepts only color files // not drawable

                    cards[j].setBackgroundResource(R.drawable.card_default);

                    TextView text = (TextView) ((ViewGroup) cards[j].getChildAt(0))
                            .getChildAt(1); // we can access it via id  but this is one something new to me that's by I preferred this
                     text.setTextColor(ContextCompat.getColor(this, R.color.black));
                }

//                cards[index].setCardBackgroundColor(
//                        ContextCompat.getColor(this , R.drawable.card_selected));

                cards[index].setBackgroundResource(R.drawable.card_selected);
                TextView text=(TextView) ((ViewGroup) cards[index].getChildAt(0))
                        .getChildAt(1);
                text.setTextColor(ContextCompat.getColor(this, R.color.white));
                selectedCard[0]=index+1;
                whichOne[0]=index;

            });

        }


        submitFeedback.setOnClickListener(v -> {
            String feedback = feedbackText.getText().toString().trim();



            if  (ratedStar[0]==0){
                Toast.makeText(this, "Please rate the app first", Toast.LENGTH_SHORT).show();
            }
            else if  (selectedCard[0]==0){
                Toast.makeText(this, "Please select a feedback category", Toast.LENGTH_SHORT).show();
            }
            else if  (feedback.isEmpty()){
                Toast.makeText(this, "Write Feedback", Toast.LENGTH_SHORT).show();
            }
            else {
                submitFeedback.setEnabled(false);
                HashMap<String, Object> map = new HashMap<>();
                map.put("time", System.currentTimeMillis());
                map.put("rating", ratedStar[0]);
                map.put("category", selectedCard[0]);
                map.put("date", new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date()));
                map.put("message", feedback);
                map.put("device", android.os.Build.MODEL);
                map.put("userId", Settings.Secure.getString(
                        getContentResolver(),
                        Settings.Secure.ANDROID_ID
                ));


                databaseReference.push().setValue(map)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Feedback submitted", Toast.LENGTH_SHORT).show();

                            feedbackText.setText("");
                            ratingBar.setRating(0);
                            selectedCard[0]=-1;
                            cards[whichOne[0]].setBackgroundResource(R.drawable.card_default); // to refresh the cardView after submitting the feedback
                            TextView text=(TextView) ((ViewGroup) cards[whichOne[0]].getChildAt(0))
                                    .getChildAt(1);
                            text.setTextColor(ContextCompat.getColor(this, R.color.black));
                            submitFeedback.setEnabled(true); // to  make btn work enable again
                            whichOne[0]=-1;



                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            submitFeedback.setEnabled(true);
                        });
            }
        });

    }

}