package com.harsh.pdfstudio.fragments;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.adapter.Inp_ImageAdapter;
import com.harsh.pdfstudio.utils.Image_picker;
import java.util.ArrayList;
import android.graphics.PorterDuff;



public class InputFrag_Edit_Img extends Fragment {

    private final ArrayList<Uri> imageList = new ArrayList<>();

    private Inp_ImageAdapter adapter;

    private String outputImageType = "jpeg";
    private int imageQuality = 90;

    private RecyclerView recyclerView;
    private ImageView emptyImage;

    private MaterialCardView selectImages;
    private MaterialCardView formatCard;
    private MaterialCardView qualityCard;

    private MaterialButton convertBtn;
    private LinearLayout selectImagesButton;

    private TextView selectedFormat;
    private TextView selectedQuality;
    private TextView infoText;

    private boolean returnFromOutputFrag = false;

    private generateImg listener;


    public InputFrag_Edit_Img() {
        super(R.layout.edit_img_inp_frag);
    }


    public void setOnGenerateImageClick(generateImg listener) {
        this.listener = listener;
    }


    @Override
    public void onViewCreated(
            @NonNull View view,
            Bundle savedInstanceState
    ) {

        super.onViewCreated(view, savedInstanceState);


        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(
                        getViewLifecycleOwner(),
                        new OnBackPressedCallback(true) {

                            @Override
                            public void handleOnBackPressed() {
                                requireActivity().finish();
                            }
                        }
                );


        recyclerView = view.findViewById(R.id.recyclerView);

        emptyImage = view.findViewById(R.id.empty_image_edit_img);

        selectImages = view.findViewById(R.id.card_view_SelectImage);

        selectImagesButton = view.findViewById(R.id.select_images_button);

        convertBtn = view.findViewById(R.id.gen);

        formatCard = view.findViewById(R.id.format_card);

        qualityCard = view.findViewById(R.id.quality_card);

        selectedFormat = view.findViewById(R.id.selected_format);

        selectedQuality = view.findViewById(R.id.selected_quality);

        infoText = view.findViewById(R.id.info_textView);

        TextView addMore = view.findViewById(R.id.add_more_text);

        ImageView backArrow = view.findViewById(R.id.back_arrow_edit_image);


        recyclerView.setLayoutManager(
                new GridLayoutManager(
                        requireContext(),
                        3
                )
        );

        adapter = new Inp_ImageAdapter(imageList);

        recyclerView.setAdapter(adapter);


        outputImageType = "jpeg";
        imageQuality = 90;

        selectedFormat.setText("JPG");
        selectedQuality.setText("High • 90%");


        backArrow.setOnClickListener(v ->
                requireActivity().finish()
        );


        View.OnClickListener selectListener = v -> openImagePicker();


        selectImagesButton.setOnClickListener(selectListener);

        addMore.setOnClickListener(selectListener);


        formatCard.setOnClickListener(v ->
                showFormatDialog()
        );


        qualityCard.setOnClickListener(v ->
                showQualityDialog()
        );


        adapter.setOnRemove(position -> {

            if (position >= 0 &&
                    position < imageList.size()) {

                imageList.remove(position);

                adapter.notifyItemRemoved(Math.toIntExact(position));

                updateUI();
            }
        });


        convertBtn.setOnClickListener(v -> {

            if (imageList.isEmpty()) {
                return;
            }

            if (listener != null) {

                returnFromOutputFrag = true;

                listener.generateImage(
                        new ArrayList<>(imageList),
                        outputImageType,
                        imageQuality
                );
            }
        });


        updateUI();

    }


    private void openImagePicker() {

        Intent intent =
                new Image_picker().openImageFiles();

        pickImagesLauncher.launch(intent);
    }


    private final ActivityResultLauncher<Intent>
            pickImagesLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {

                        if (result.getResultCode()
                                != Activity.RESULT_OK) {
                            return;
                        }

                        Intent data = result.getData();

                        if (data == null) {
                            return;
                        }


                        // Multiple images

                        if (data.getClipData() != null) {

                            ClipData clipData =
                                    data.getClipData();

                            for (int i = 0;
                                 i < clipData.getItemCount();
                                 i++) {

                                Uri uri =
                                        clipData
                                                .getItemAt(i)
                                                .getUri();

                                imageList.add(uri);
                            }
                        }


                        // Single image

                        else if (data.getData() != null) {

                            imageList.add(
                                    data.getData()
                            );
                        }


                        adapter.notifyDataSetChanged();

                        updateUI();
                    }
            );



// ============================================================
// FORMAT DIALOG
// ============================================================

    private void showFormatDialog() {

        View view = getLayoutInflater().inflate(
                R.layout.dialog_format,
                null
        );

        RadioGroup formatGroup =
                view.findViewById(R.id.format_group);

        MaterialRadioButton jpg =
                view.findViewById(R.id.radio_jpg);

        MaterialRadioButton png =
                view.findViewById(R.id.radio_png);

        MaterialRadioButton webp =
                view.findViewById(R.id.radio_webp);


        // ========================================================
        // RADIO BUTTON COLORS
        // ========================================================

        ColorStateList radioTint = getRadioTint();

        jpg.setButtonTintList(radioTint);
        png.setButtonTintList(radioTint);
        webp.setButtonTintList(radioTint);


        // ========================================================
        // SET CURRENT SELECTION
        // ========================================================

        if ("png".equals(outputImageType)) {

            formatGroup.check(R.id.radio_png);

        } else if ("webp".equals(outputImageType)) {

            formatGroup.check(R.id.radio_webp);

        } else {

            formatGroup.check(R.id.radio_jpg);
        }


        // ========================================================
        // CREATE DIALOG
        // ========================================================

        MaterialAlertDialogBuilder builder =
                new MaterialAlertDialogBuilder(
                        requireContext()
                );

        builder.setView(view);

        AlertDialog dialog = builder.create();


        // ========================================================
        // SELECTION LISTENER
        // ========================================================

        formatGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    if (checkedId == R.id.radio_jpg) {

                        outputImageType = "jpeg";

                        selectedFormat.setText("JPG");


                    } else if (checkedId == R.id.radio_png) {

                        outputImageType = "png";

                        selectedFormat.setText("PNG");


                    } else if (checkedId == R.id.radio_webp) {

                        outputImageType = "webp";

                        selectedFormat.setText("WEBP");
                    }


                    // Update quality display
                    updateQualityForFormat();


                    // Close dialog
                    dialog.dismiss();
                }
        );


        // ========================================================
        // SHOW DIALOG
        // ========================================================

        dialog.show();


        // ========================================================
        // WHITE ROUNDED BACKGROUND
        // ========================================================

        styleDialogBackground(dialog);
    }


    private void showQualityDialog() {


        if ("png".equals(outputImageType)) {

            MaterialAlertDialogBuilder builder =
                    new MaterialAlertDialogBuilder(
                            requireContext()
                    );

            builder.setTitle("Image quality")
                    .setMessage(
                            "PNG uses lossless compression, so image quality cannot be adjusted."
                    )
                    .setPositiveButton(
                            "OK",
                            null
                    );

            AlertDialog dialog = builder.create();

            dialog.setOnShowListener(d -> {

                TextView title = dialog.findViewById(
                        com.google.android.material.R.id.alertTitle
                );


                if (title != null) {
                    title.setTextColor(
                            ContextCompat.getColor(
                                    requireContext(),
                                    R.color.colorTextPrimary
                            )
                    );
                }


            });

            dialog.show();

            styleSimpleDialog(dialog);

            return;
        }


        View view = getLayoutInflater().inflate(
                R.layout.dialog_quality,
                null
        );


        RadioGroup qualityGroup = view.findViewById(
                        R.id.quality_group
        );


        MaterialRadioButton low = view.findViewById(
                        R.id.radio_low
        );

        MaterialRadioButton medium = view.findViewById(
                        R.id.radio_medium
        );

        MaterialRadioButton high = view.findViewById(
                        R.id.radio_high
        );

        MaterialRadioButton max = view.findViewById(
                        R.id.radio_max
        );


        ColorStateList radioTint = getRadioTint();

        low.setButtonTintList(radioTint);
        medium.setButtonTintList(radioTint);
        high.setButtonTintList(radioTint);
        max.setButtonTintList(radioTint);


        // ========================================================
        // SET CURRENT QUALITY
        // ========================================================

        if (imageQuality == 60) {

            qualityGroup.check(
                    R.id.radio_low
            );

        }
        else if (imageQuality == 80) {

            qualityGroup.check(
                    R.id.radio_medium
            );

        }
        else if (imageQuality == 90) {

            qualityGroup.check(
                    R.id.radio_high
            );

        }
        else {

            qualityGroup.check(
                    R.id.radio_max
            );
        }



        MaterialAlertDialogBuilder builder =
                new MaterialAlertDialogBuilder(
                        requireContext()
                );

        builder.setView(view);

        AlertDialog dialog = builder.create();


        qualityGroup.setOnCheckedChangeListener(
                (group, checkedId) -> {

                    if (checkedId == R.id.radio_low) {

                        imageQuality = 60;

                        selectedQuality.setText(
                                "Low • 60%"
                        );


                    } else if (
                            checkedId == R.id.radio_medium) {

                        imageQuality = 80;

                        selectedQuality.setText(
                                "Medium • 80%"
                        );


                    } else if (
                            checkedId == R.id.radio_high) {

                        imageQuality = 90;

                        selectedQuality.setText(
                                "High • 90%"
                        );


                    } else if (
                            checkedId == R.id.radio_max) {

                        imageQuality = 100;

                        selectedQuality.setText(
                                "Maximum • 100%"
                        );
                    }


                    // Close dialog
                    dialog.dismiss();
                }
        );



        dialog.show();

        styleDialogBackground(dialog);
    }



    private ColorStateList getRadioTint() {

        int blue =
                ContextCompat.getColor(
                        requireContext(),
                        R.color.color_primary
                );

        int gray =
                ContextCompat.getColor(
                        requireContext(),
                        R.color.color_border
                );

        return new ColorStateList(

                new int[][]{

                        // Checked
                        new int[]{
                                android.R.attr.state_checked
                        },

                        // Unchecked
                        new int[]{}

                },

                new int[]{

                        blue,
                        gray

                }
        );
    }


    private void styleDialogBackground(
            AlertDialog dialog
    ) {

        if (dialog.getWindow() == null) {
            return;
        }


        GradientDrawable background =
                new GradientDrawable();

        background.setColor(
                Color.WHITE
        );

        background.setCornerRadius(
                dpToPx(28)
        );


        dialog.getWindow()
                .setBackgroundDrawable(
                        background
                );
    }



    private void styleSimpleDialog(
            AlertDialog dialog
    ) {

        styleDialogBackground(dialog);


        // --------------------------------------------------------
        // Title
        // --------------------------------------------------------

        int titleId =
                getResources().getIdentifier(
                        "alertTitle",
                        "id",
                        "android"
                );

        TextView title =
                dialog.findViewById(titleId);

        if (title != null) {

            title.setTextColor(
                    Color.BLACK
            );
        }


        // --------------------------------------------------------
        // Message
        // --------------------------------------------------------

        TextView message =
                dialog.findViewById(
                        android.R.id.message
                );

        if (message != null) {

            message.setTextColor(
                    Color.DKGRAY
            );
        }


        // --------------------------------------------------------
        // Button
        // --------------------------------------------------------

        Button button =
                dialog.getButton(
                        AlertDialog.BUTTON_POSITIVE
                );

        if (button != null) {

            button.setTextColor(
                    ContextCompat.getColor(
                            requireContext(),
                            R.color.darkBlue
                    )
            );
        }
    }


    private String getQualityText(
            int quality
    ) {

        switch (quality) {

            case 60:
                return "Low • 60%";

            case 80:
                return "Medium • 80%";

            case 90:
                return "High • 90%";

            case 100:
                return "Maximum • 100%";

            default:
                return "High • 90%";
        }
    }



    private void updateQualityForFormat() {

        if ("png".equals(outputImageType)) {

            selectedQuality.setText(
                    "Lossless"
            );

        } else {

            selectedQuality.setText(
                    getQualityText(imageQuality)
            );
        }
    }



    private int dpToPx(
            int dp
    ) {

        return Math.round(
                dp *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }


    private void updateUI() {

        boolean hasImages =
                !imageList.isEmpty();


        if (hasImages) {

            // Hide upload card

            selectImages.setVisibility(
                    View.GONE
            );

            // Show image information

            View infoBar =
                    getView().findViewById(
                            R.id.image_info_bar
                    );

            infoBar.setVisibility(
                    View.VISIBLE
            );


            infoText.setText(
                    imageList.size() +
                            (imageList.size() == 1
                                    ? " image selected"
                                    : " images selected")
            );


            // Show RecyclerView

            recyclerView.setVisibility(
                    View.VISIBLE
            );

            emptyImage.setVisibility(
                    View.GONE
            );


            // Enable convert button

            convertBtn.setEnabled(true);
            convertBtn.setAlpha(1f);


        } else {

            // Show upload card

            selectImages.setVisibility(
                    View.VISIBLE
            );


            // Hide info bar

            View infoBar =
                    getView().findViewById(
                            R.id.image_info_bar
                    );

            infoBar.setVisibility(
                    View.GONE
            );


            // Hide RecyclerView

            recyclerView.setVisibility(
                    View.GONE
            );


            // Show empty state

            emptyImage.setVisibility(
                    View.VISIBLE
            );


            // Disable convert

            convertBtn.setEnabled(false);
            convertBtn.setAlpha(0.5f);
        }
    }

    @Override
    public void onResume() {

        super.onResume();

        if (!returnFromOutputFrag) {
            return;
        }


        imageList.clear();

        adapter.notifyDataSetChanged();

        updateUI();

        returnFromOutputFrag = false;
    }


    public interface generateImg {

        void generateImage(
                ArrayList<Uri> imageList,
                String outputFormat,
                int quality
        );
    }
}