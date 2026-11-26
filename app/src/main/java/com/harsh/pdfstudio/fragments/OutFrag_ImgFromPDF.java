package com.harsh.pdfstudio.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.adapter.Ext_Img_Pdf_Adapter;
import com.harsh.pdfstudio.model.ImageItem;
import com.harsh.pdfstudio.utils.Byte_MB;
import com.harsh.pdfstudio.utils.File_Size;
import com.harsh.pdfstudio.utils.GetFileName;
import com.harsh.pdfstudio.utils.SaveFile;
import com.harsh.pdfstudio.utils.ShareFiles;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class OutFrag_ImgFromPDF extends Fragment {

    private MaterialCardView conversionMessCard;
    private MaterialCardView extractInformation;
    private MaterialCardView recyclerCard;

    private View noImgExtr;
    private ImageView downloadOrBack , backArrow;

    private TextView title;
    private TextView subtitle;
    private TextView downloadAll;
    private TextView nImgExt , conversion_tv_title;
    private TextView formatTextView;

    private RecyclerView recyclerView;

    private LinearLayout previousFrag;
    private LinearLayout shareAll;
    private LinearLayout deleteAll;
    private LinearLayout downloadBtn;

    private final ArrayList<ImageItem> list = new ArrayList<>();

    private long fileSize = 0L;

    private Ext_Img_Pdf_Adapter adapter;

    private boolean returnHome =  false;


    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());


    public OutFrag_ImgFromPDF() {
        super(R.layout.extract_img_out_frag);
    }


    @Override
    public void onViewCreated(
            @NonNull View view,
            Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);


        title = view.findViewById(R.id.title);
        subtitle = view.findViewById(R.id.subtitle_fetch_img_pdf);
        recyclerView = view.findViewById(R.id.recyclerView_ext_img);
        recyclerCard = view.findViewById(R.id.recycler_card);
        downloadAll = view.findViewById(R.id.download_all_ext_img_pdf);
        downloadBtn = view.findViewById(R.id.download_all_btn);
        downloadOrBack = view.findViewById(R.id.download_or_back);
        conversionMessCard = view.findViewById(R.id.conversion_mess_card);
        extractInformation = view.findViewById(R.id.description_extractImg);
        noImgExtr = view.findViewById(R.id.no_img_extracted);
        previousFrag = view.findViewById(R.id.back_to_inp_fragment);
        nImgExt = view.findViewById(R.id.pdf_name_extract_out_pdf);
        formatTextView = view.findViewById(R.id.format_textView);
        shareAll = view.findViewById(R.id.share_All_);
        deleteAll = view.findViewById(R.id.delete_all);
        conversion_tv_title = view.findViewById(R.id.conversionTv_title);
        backArrow = view.findViewById(R.id.back_arrow_edit_image);

        backArrow.setOnClickListener(v -> requireActivity().finish());


        title.setText(R.string.image_extracted);

        subtitle.setVisibility(View.GONE);

        returnHome = false; //  to make sure that download btn doesn't  misbehave

        // IMPORTANT:
        // In the new XML this does not have visibility="gone",
        // so we explicitly hide it for the normal result state.
        previousFrag.setVisibility(View.GONE);

        // Empty illustration starts hidden.
        noImgExtr.setVisibility(View.GONE);


        if (!loadDataFromArguments()) {
            showNoImagesState();
            return;
        }

        setupRecyclerView();

        setupDownloadButton();

        setupShareButton();

        setupDeleteAllButton();

        setupExtractAnotherButton();


        updateDownloadButton();
    }


    private boolean loadDataFromArguments() {

        Bundle bundle = getArguments();

        if (bundle == null) {
            return false;
        }


        ArrayList<String> imageUriList =
                bundle.getStringArrayList("list");


        if (imageUriList == null ||
                imageUriList.isEmpty()) {

            return false;
        }


        String fileName =
                bundle.getString("fileName");


        String format =
                bundle.getString("format");


        // ---------------------------------------------------------
        // Format fallback
        // ---------------------------------------------------------

        if (format == null ||
                format.trim().isEmpty()) {

            format = "PNG";
        }


        formatTextView.setText(
                format.toUpperCase(Locale.ROOT)
        );


        if (fileName != null &&
                !fileName.trim().isEmpty()) {

            nImgExt.setText(
                    String.format(
                            getString(
                                    R.string.d_images_extracted_from_s
                            ),
                            imageUriList.size(),
                            fileName
                    )
            );
        }


        list.clear();

        fileSize = 0L;


        Context context = requireContext();


        for (String uriString : imageUriList) {

            if (uriString == null ||
                    uriString.trim().isEmpty()) {

                continue;
            }


            try {

                Uri imageUri = Uri.parse(uriString);


                long size = File_Size.getFileSize(
                                context,
                                imageUri
                );

                fileSize += size;

                ImageItem item = new ImageItem(
                                imageUri,
                                size,
                                format
                        );

                list.add(item);

            }
            catch (Exception e) {

                e.printStackTrace();
            }
        }


        return !list.isEmpty();
    }


    private void setupRecyclerView() {

        adapter = new Ext_Img_Pdf_Adapter(list);


        recyclerView.setLayoutManager(
                new GridLayoutManager(
                        requireContext(),
                        3
                )
        );


        recyclerView.setHasFixedSize(true);


        recyclerView.setAdapter(adapter);


        adapter.setOnRemoveImgClick((size) -> {

            checkTotalSize(size);

        });

    }


    private void setupDownloadButton() {

        downloadBtn.setOnClickListener(v -> {


            if (list.isEmpty() && returnHome ) {

               requireActivity().finish();

               return;

            }


            downloadBtn.setEnabled(false);


            Context context = requireContext().getApplicationContext();

            // Copy current list for background task
            ArrayList<ImageItem> imagesToDownload = new ArrayList<>(list);

            executorService.execute(() -> {

                int successCount = 0;

                int failedCount = 0;

                for (ImageItem item : imagesToDownload) {

                    try {

                        SaveFile.saveImageFromUri(
                                context,
                                item.getUri(),
                                GetFileName.getFileName(
                                        context,
                                        item.getUri()
                                ),
                                item.getFormat()
                        );


                        successCount++;


                    }
                    catch (Exception e) {

                        failedCount++;

                        e.printStackTrace();
                    }
                }


                int finalSuccessCount =
                        successCount;

                int finalFailedCount =
                        failedCount;



                handler.post(() -> {

                    if (!isAdded()) {
                        return;
                    }


                    downloadBtn.setEnabled(true);


                    if (finalFailedCount == 0) {

                        Toast.makeText(
                                requireContext(),
                                "Images saved to Downloads",
                                Toast.LENGTH_LONG
                        ).show();


                    } else if (finalSuccessCount > 0) {

                        Toast.makeText(
                                requireContext(),
                                finalSuccessCount +
                                        " images saved, " +
                                        finalFailedCount +
                                        " failed",
                                Toast.LENGTH_LONG
                        ).show();


                    } else {

                        Toast.makeText(
                                requireContext(),
                                "Failed to save images",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                });

            });


        });


    }


    private void setupShareButton() {

        shareAll.setOnClickListener(v -> {

            if (list.isEmpty()) {
                return;
            }


            ArrayList<Uri> uriList =
                    new ArrayList<>();


            for (ImageItem item : list) {

                uriList.add(item.getUri());

            }


            try {

                Intent intent =
                        ShareFiles.shareImage();


                intent.putParcelableArrayListExtra(
                        Intent.EXTRA_STREAM,
                        uriList
                );


                startActivity(
                        Intent.createChooser(
                                intent,
                                "Share Images"
                        )
                );


            } catch (Exception e) {

                e.printStackTrace();


                Toast.makeText(
                        requireContext(),
                        "Unable to share images",
                        Toast.LENGTH_SHORT
                ).show();
            }

        });
    }


    private void setupDeleteAllButton() {

        deleteAll.setOnClickListener(v -> {

            if (list.isEmpty()) {
                return;
            }


            new MaterialAlertDialogBuilder(
                    requireContext()
            )
                    .setTitle("Delete All Images?")
                    .setMessage(
                            "All extracted images will be removed " +
                                    "from this screen."
                    )
                    .setNegativeButton(
                            "Cancel",
                            null
                    )
                    .setPositiveButton(
                            "Delete",
                            (dialog, which) -> {

                                clearAllImages();

                            }
                    )
                    .show();

        });
    }

    private void clearAllImages() {

        list.clear();

        fileSize = 0L;


        if (adapter != null) {

            adapter.notifyDataSetChanged();

        }


        showNoImagesState();
    }

    private void setupExtractAnotherButton() {

        previousFrag.setOnClickListener(v -> {

            goBackToPreviousScreen();

        });
    }

    private void checkTotalSize(long size) {

        fileSize -= size;


        if (fileSize < 0) {
            fileSize = 0L;
        }

        if (list.isEmpty()) {

            showNoImagesState();

            return;
        }


        updateDownloadButton();
    }


    private void updateDownloadButton() {

        if (list.isEmpty()) {

            downloadAll.setText(
                    "Back to Home"
            );


            downloadOrBack.setImageResource(
                    R.drawable.white_home_icon_filled
            );


        } else {

            downloadAll.setText(
                    String.format(
                            Locale.getDefault(),
                            "Download All (%s)",
                            Byte_MB.convertByte(fileSize)
                    )
            );


            downloadOrBack.setImageResource(
                    R.drawable.download_icon_no_bg
            );
        }
    }


    private void showNoImagesState() {

        returnHome = true;

        // Hide normal result sections
        conversionMessCard.setVisibility(View.VISIBLE);

        extractInformation.setVisibility(View.GONE);
        recyclerCard.setVisibility(View.GONE);


        // Show empty state
        noImgExtr.setVisibility(View.VISIBLE);


        // Show secondary action
        previousFrag.setVisibility(View.VISIBLE);


        // Disable actions that require images
        shareAll.setEnabled(false);
        deleteAll.setEnabled(false);


        // Change bottom action
        downloadAll.setText("Back to Home");

        nImgExt.setText("All Images have been removed from the list.");
        conversion_tv_title.setText("Extraction Completed");

        downloadOrBack.setImageResource(
                R.drawable.white_home_icon_filled
        );

    }


    private void goBackToPreviousScreen() {

        requireActivity().getSupportFragmentManager().clearBackStack(null);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.Fragment_container ,
                        new InpFrag_ImgFromPDF()
                ).commit();


    }


    @Override
    public void onDestroyView() {

        handler.removeCallbacksAndMessages(null);

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {

        executorService.shutdownNow();

        super.onDestroy();
    }
}


