package com.harsh.pdfstudio.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.adapter.Inp_ImageAdapter;
import com.harsh.pdfstudio.ui.MainActivity;
import com.harsh.pdfstudio.utils.Byte_MB;
import com.harsh.pdfstudio.utils.File_Size;
import com.harsh.pdfstudio.utils.GetFileExtension;
import com.harsh.pdfstudio.utils.SaveFile;
import com.harsh.pdfstudio.utils.ShareFiles;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class OutputFrag_Edit_Img extends Fragment {

    LinearLayout downloadAll;
    LinearLayout deleteAll;
    LinearLayout shareAll;
    TextView total_Size;
    TextView total_Images;
    RecyclerView recyclerView;
    TextView format_text;
    TextView nImages;
    ImageView emptyImage;
    ImageView backArrow;
    Long totalSize= 0L;
    int totalImages=0;
    MaterialCardView back_to_prev_fragment;
    MaterialCardView back_to_home;
    ArrayList<Uri> list=new ArrayList<>();
    FrameLayout progressBar;





    public OutputFrag_Edit_Img(){
        super(R.layout.edit_img_out_frag);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){

        super.onViewCreated(view, savedInstanceState);


        downloadAll=view.findViewById(R.id.download_ALL);
        deleteAll=view.findViewById(R.id.delete_all);
        shareAll=view.findViewById(R.id.share_All);
        total_Size=view.findViewById(R.id.download_size);
        recyclerView=view.findViewById(R.id.outputRecyclerView);
        total_Images=view.findViewById(R.id.total_cnvt_img);
        format_text=view.findViewById(R.id.format);
        emptyImage=view.findViewById(R.id.no_img_converted);
        nImages=view.findViewById(R.id.n_images_converted);
        back_to_home=view.findViewById(R.id.back_to_home);
        back_to_prev_fragment=view.findViewById(R.id.back_to_prev);
        backArrow=view.findViewById(R.id.back_arrow_edit_image);
        progressBar=view.findViewById(R.id.frameLayout);


        Bundle bundle=getArguments();
        if (bundle!=null){

            ArrayList<String> imageList=bundle.getStringArrayList("images");

            String format=bundle.getString("format").toUpperCase();

            totalImages=imageList.size();



            for (String s:imageList){

                Uri uri=Uri.parse(s);
                totalSize+=File_Size.getFileSize(requireContext(),uri);
                list.add(uri);

            }

            format_text.setText(format);
            total_Images.setText(getString(R.string.converted_images,totalImages));
            total_Size.setText(getString(R.string.download__all,Byte_MB.convertByte(totalSize)));
            nImages.setText(getString(R.string.n_img_converted_successfully,totalImages));

            Inp_ImageAdapter adapter=new Inp_ImageAdapter(list);

            recyclerView.setLayoutManager(new GridLayoutManager(requireContext(),3));

            adapter.setOnRemove((size) -> {
                    updateUI(size);

            });
            recyclerView.setAdapter(adapter);

        }

         deleteAll.setOnClickListener( v -> {
             list.clear();
             updateUI(totalSize);
             nImages.setText(getString(R.string.n_img_converted_successfully,0));
         });



        back_to_home.setOnClickListener(v ->{

            Intent intent = new Intent(requireContext(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);


        });

        back_to_prev_fragment.setOnClickListener(v -> {

            requireActivity().getSupportFragmentManager().popBackStack();


        });

        shareAll.setOnClickListener(v ->{

                Intent intent= ShareFiles.shareImage();
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,list);
                startActivity(Intent.createChooser(intent,"Share Images"));

        });

        backArrow.setOnClickListener( v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });




            downloadAll.setOnClickListener( v -> {
                ExecutorService executor = Executors.newSingleThreadExecutor();

                Handler handler = new Handler(Looper.getMainLooper());

                progressBar.setVisibility(View.VISIBLE);
                progressBar.bringToFront();

                executor.execute(() -> {

                    for (int i = 0; i < list.size(); i++) {
                        Uri uri = list.get(i);

                        String extension = GetFileExtension.getFileExtension(v.getContext(), uri);
                        String filename = "Img_" + System.currentTimeMillis() + "_" + i + extension;

                        SaveFile.saveImageFromUri(v.getContext(), uri, filename, extension);
                    }

                    handler.post(() -> {
                        Toast.makeText(v.getContext().getApplicationContext(),
                                "Saved to Download",
                                Toast.LENGTH_SHORT).show();

                        progressBar.setVisibility(View.GONE);

                    });

                });

            });


    }


    private void updateUI(Long size){

        if (list.size()==0){
            emptyImage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
        else{
            emptyImage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }


        totalImages-=1;
        total_Images.setText(getString(R.string.converted_images,totalImages));
        nImages.setText(getString(R.string.n_img_converted_successfully,totalImages));
        totalSize -= size;
        total_Size.setText(getString(R.string.download__all,Byte_MB.convertByte(totalSize)));




    }
}