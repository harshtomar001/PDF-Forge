package com.harsh.pdfstudio.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.harsh.pdfstudio.R;

public class ConversionFailed extends Fragment {

    String for_="conversion";
    MaterialButton returnHome;
    MaterialButton back_to_prev_Frag;
    ImageView errorImage;
    TextView errorTitle;
    TextView errorMessage;

    public ConversionFailed(){
        super(R.layout.conversion_failed);
    }

    public ConversionFailed(String document_name){
        super(R.layout.conversion_failed);
        this.for_=document_name;

    }
    @Override
    public  void onViewCreated(View view, Bundle savedInstanceBundle){

        super.onViewCreated(view,savedInstanceBundle);

        returnHome=view.findViewById(R.id.returnHomeBtn);
        back_to_prev_Frag=view.findViewById(R.id.chooseAnotherFileBtn);
        errorImage=view.findViewById(R.id.errorImage);
        errorTitle=view.findViewById(R.id.errorTitle);
        errorMessage=view.findViewById(R.id.errorMessage);

        if (for_.equals("for_pdf_operation")){
            errorTitle.setVisibility(View.GONE);
            errorMessage.setVisibility(View.GONE);
            errorImage.setImageResource(R.drawable.invalid_operation_pdf);
        }


        returnHome.setOnClickListener(v -> {

            Intent intent=new Intent(requireActivity(), MainActivity.class);
            requireActivity().startActivity(intent);

        });


        back_to_prev_Frag.setOnClickListener(v -> {

            requireActivity().getSupportFragmentManager().popBackStack();

        });

    }


}
