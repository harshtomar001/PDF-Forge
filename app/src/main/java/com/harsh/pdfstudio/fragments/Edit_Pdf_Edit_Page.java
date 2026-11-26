package com.harsh.pdfstudio.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.util.LruCache;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.adapter.FontAdapter;
import com.harsh.pdfstudio.custom_view.AddImageView;
import com.harsh.pdfstudio.custom_view.AddTextView;
import com.harsh.pdfstudio.custom_view.ColorPickerView;
import com.harsh.pdfstudio.custom_view.CropImageView;
import com.harsh.pdfstudio.custom_view.EraserView;
import com.harsh.pdfstudio.model.AddImageObject;
import com.harsh.pdfstudio.model.AddTextObject;
import com.harsh.pdfstudio.model.EditPdfViewModel;
import com.harsh.pdfstudio.model.FontItem;
import com.harsh.pdfstudio.model.ImageObject;
import com.harsh.pdfstudio.model.PageItem;
import com.harsh.pdfstudio.model.SharingPages_VM_edt_pdf;
import com.harsh.pdfstudio.operations.AddImageOperation;
import com.harsh.pdfstudio.operations.CropOperation;
import com.harsh.pdfstudio.operations.ExtractOperation;
import com.harsh.pdfstudio.operations.PageOperation;
import com.harsh.pdfstudio.operations.PageRenderContext;
import com.harsh.pdfstudio.operations.ReplaceOperation;
import com.harsh.pdfstudio.operations.RotateOperation;
import com.harsh.pdfstudio.operations.WatermarkOperation;
import com.harsh.pdfstudio.utils.Byte_MB;
import com.harsh.pdfstudio.utils.Dp_to_Px;
import com.harsh.pdfstudio.utils.GenerateBitmapFromImgUri;
import com.harsh.pdfstudio.utils.GenerateBitmapFromPDF;
import com.harsh.pdfstudio.utils.Image_picker;
import com.harsh.pdfstudio.utils.SaveFile;
import com.vertlix.utils.file.File_Size;
import com.vertlix.utils.file.GetFileName;
import com.vertlix.utils.pdf.Page_Count;
import org.jspecify.annotations.NonNull;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Edit_Pdf_Edit_Page extends Fragment {

    private  int currentPage;

    PhotoView edit_page_imageView;

    int addTextTextColor = Color.BLUE;

    PdfRenderer pdfRenderer;

    FontAdapter fontAdapter;

    private boolean isBold = false;
    private boolean isItalic = false;
    private boolean isUnderline = false;
    private boolean isStrike = false;

    private int selectedWeight = 400;


    private ActivityResultLauncher<Intent> pickImagesLauncher;

    ArrayList<Uri> watermark_logo_list = new ArrayList<>();

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    Handler handler = new Handler(Looper.getMainLooper());


    CardView edit_page_cardview;

    String addTextFontSelected ;

    List<PageItem> pdfSelectedPages;

    ImageButton prevPage, nextPage;

    ImageView watermark_logo_imageView , undo,redo ,
            back_arrow_watermark, selected_img_preview,
            back_arrow_editPage, back_arrow_export_page,
            back_arrow_addImage;

    int currentPageIndex = 0;

    TextView pageCounter, btnText, btnImage,loadingBar_text,
            saveChanges,cancelSave_discardLayout ,
            preview_watermark_text ,opacity_value, size_value,
            rotation_value, size_textView, imageNametv, change_img_tv,
            extension_exp_pdf, eraseSizetv;

    Button btnSelectImage;

    MaterialButton btnApplyWatermark,saveChangesBtn_save_dis_layout,
                  discardChangesBtn_save_dis_layout, exportPage,
                  swapImg_addImg , addMoreText_addtextLayout, applyEraserbtn;

    LinearLayout  cancelEraserBtn;

    MaterialCardView  colorSelector;

    Slider rotationSlider, opacitySlider, sizeSlider, eraserSizeSlider;

    EditText edtText , edtTxtExpPage;

    LinearLayout addText, addImage, addWatermark,
            crop, delete, sign, eraser, moreToolsLayout,
            moreToolsClickLayout, replacePage, extractPage,
            rotateLeft, rotateRight;

    View watermarkOverlay, selectedImageCard, exportLayout,loadingBar,
            save_discard_layout,cropLayout, addImageOverlay , addTextOverlay ,
            fontSelector , styleSelector, eraserOverlay, eraserColorPreview;

    ColorPickerView colorPicker;

    MaterialCheckBox checkBoxExpPage;

    private boolean keyboardVisible = false;
    private boolean finishingTextEditing = false;

    ConstraintLayout editPageLayout , exportPdf, exportJpg, exportPng ;

    FrameLayout watermarkPreviewContainer, moreToolsOverlay,
                radioPDF, radioJPEG, radioPNG;

    MaterialAutoCompleteTextView dropdown_position;

    private boolean isImageWatermark;

    private Bitmap logoWatermarkBitmap=null;

    boolean moreToolsClickLayoutVisibility = false;

    PageItem swapImg_addImg_uri=null;

    TextInputEditText hexTextBox;

    AddTextView addTextView;

    boolean hasUnsavedChanges=false;

    EditPdfViewModel pdfOperationModel;

    int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

    int cacheSize = maxMemory / 8;

    EditText addTextEditText;

    Slider addTextOpacitySlider, addTextSizeSlider;

    Stack<PageOperation> undoStack=new Stack<>();

    Stack<PageOperation> redoStack=new Stack<>();

    HashMap<Long, List<PageOperation>> modifiedPagesMap = new HashMap<Long, List<PageOperation>>();

    private List<FontItem> fonts;

    CircularProgressIndicator progressBar;
    TextView stylePreview , styleTitle;

    Set<Long> modifiedIdSet=new HashSet<>();

    boolean isAnyOpPerformed=false;

    private boolean updatingEditText = false;
    private AddTextObject editingObject = null;

    private boolean creatingNewText = false;
    private String originalText;

    List<AddImageObject> addImageObjectList = new ArrayList<>();


    Stack<View> activeOverlay;

    private boolean maxTextSizeToastShown = false;
    //
    ImageButton backArrowCropLayout;

    CropImageView cropImageView;

    EraserView  eraserView;

    AddImageView addImageView;

    MaterialButton applyBtnCropLayout,resetCrop, cancelAddImage,
            applyAddImage, flip_hor, flip_ver, applyTextColor,
            cancelColorPicker, addTextResetBtn, resetEraser;

    TextView cropInfo, fontName;

    private PopupWindow fontPopup;

    //

    Uri ReplacePageUri=null; //  for  the replacing pdf page

    List<PageItem> addedImageUri=new ArrayList<>(); //  for  the  add image tool

    private final LruCache<Long, Bitmap> cachedBitmap =
            new LruCache<>(cacheSize) {
        @Override
        protected int sizeOf(Long key, Bitmap bitmap) {
            return bitmap.getByteCount() / 1024;
        }
    };

    private final LruCache<Long, Bitmap> modifiedBitmaps =
            new LruCache<>(cacheSize){
        @Override
        protected int sizeOf(Long item, Bitmap bitmap) {
            return bitmap.getByteCount() / 1024;
        }
    };

    // watermark position

    public enum WatermarkPosition {

        TOP_LEFT("Top Left"),
        TOP_CENTER("Top Center"),
        TOP_RIGHT("Top Right"),
        CENTER_LEFT("Center Left"),
        CENTER("Center"),
        CENTER_RIGHT("Center Right"),
        BOTTOM_LEFT("Bottom Left"),
        BOTTOM_CENTER("Bottom Center"),
        BOTTOM_RIGHT("Bottom Right");

        private final String displayName;

        WatermarkPosition(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private enum ImageRequestType {
        NONE, WATERMARK, REPLACE_PAGE, ADD_IMAGE, SWAP_IMG_ADD_IMAGE;
    }

    public   enum RadioButtonExtract {

        PDF(".pdf"),PNG(".png"),JPEG(".jpg");

        private final  String displayName;

        RadioButtonExtract(String displayName) {this.displayName=displayName;}

    }



    RadioButtonExtract currentRadioBtn= RadioButtonExtract.PDF;

    private ImageRequestType currentRequest = ImageRequestType.NONE;

    private WatermarkPosition currentPosition = WatermarkPosition.CENTER;

    private  PageItem currentPageitem;

    private  GenerateBitmapFromPDF getBitmapFromPDF;

    public Edit_Pdf_Edit_Page(@NonNull PdfRenderer pdfRenderer) {

        super(R.layout.edit_pdf_edit_page);
        this.pdfRenderer = pdfRenderer;
        getBitmapFromPDF= new GenerateBitmapFromPDF();

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceBundle) {

        super.onViewCreated(view, savedInstanceBundle);

        initializePage(view);

        addTextFontSelected  = "Poppins";

        back_arrow_editPage.setOnClickListener(v ->{


            if(hasUnsavedChanges){

                save_discard_layout.setVisibility(View.VISIBLE);

            }
            else requireActivity().getSupportFragmentManager().popBackStack();


        });

        SharingPages_VM_edt_pdf sharingDateBtwFrag = new ViewModelProvider(requireActivity())
                                                            .get(SharingPages_VM_edt_pdf.class);

        sharingDateBtwFrag.getSelectedPageItems()
                .observe(getViewLifecycleOwner(), pdfPages -> {

            pdfSelectedPages = new ArrayList<>(pdfPages);

            currentPage= currentPageIndex +1;

            currentPageitem=pdfSelectedPages.get(currentPageIndex);

            edit_page_cardview.post(() -> {

                pageCounter.setText(currentPage + "/" + pdfSelectedPages.size());

                try {

                    loadPage(pdfSelectedPages.get(currentPageIndex));

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }


            });

        });

        sharingDateBtwFrag.getModifiedIdSet()
                .observe(getViewLifecycleOwner(), set ->{
            modifiedIdSet=set;

        });

        modifiedPagesMap  =   sharingDateBtwFrag.getModifiedPageOperationsMap();

        // actual pdf operations model send the data to all the fragments
        // applying pdf operations pdf model

        pdfOperationModel = new ViewModelProvider(requireActivity())
                                .get(EditPdfViewModel.class);

        //  on swiping the previous pages

        prevPage.setOnClickListener(v -> {

            if (currentPageIndex > 0) {
                currentPageIndex--;
                try {
                    currentPageitem=pdfSelectedPages.get(currentPageIndex);

                    loadPage(currentPageitem);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                pageCounter.setText(currentPageIndex + 1 + "/" + pdfSelectedPages.size());


                currentPage= currentPageIndex;



            }

        });

        // on swiping the next pages

        nextPage.setOnClickListener(v -> {

            if (currentPageIndex < pdfSelectedPages.size() - 1) {

                currentPageIndex++;

                try {
                    currentPageitem=pdfSelectedPages.get(currentPageIndex);

                    loadPage(pdfSelectedPages.get(currentPageIndex));

                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }

                pageCounter.setText(currentPageIndex + 1 + "/" + pdfSelectedPages.size());

                currentPage= currentPageIndex;


            }
        });

        delete.setOnClickListener(v -> {

            if (pdfSelectedPages.size() == 1) {

                pdfSelectedPages.clear();
                currentPageitem=null;
                currentPageIndex =0;
                getParentFragmentManager().popBackStack();


            }
            else if (currentPageIndex == pdfSelectedPages.size() - 1) {

                pdfSelectedPages.remove(currentPageIndex);
                currentPageIndex--;

                try {

                    loadPage(pdfSelectedPages.get(currentPageIndex));
                    currentPageitem = pdfSelectedPages.get(currentPageIndex);

                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }

                pageCounter.setText(currentPageIndex + 1 + "/" + pdfSelectedPages.size());

            }
            else if (currentPageIndex < pdfSelectedPages.size() - 1) {

                pdfSelectedPages.remove(currentPageIndex);

                try {

                    loadPage(pdfSelectedPages.get(currentPageIndex));
                    currentPageitem = pdfSelectedPages.get(currentPageIndex);

                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }

                pageCounter.setText(currentPageIndex + 1 + "/" + pdfSelectedPages.size());


            }




        });

        moreToolsOverlay.setOnClickListener(v -> {

            moreToolsClickLayoutVisibility = !moreToolsClickLayoutVisibility ;

            moreToolsOverlay.setVisibility( moreToolsClickLayoutVisibility
                    ? View.VISIBLE
                    : View.GONE
            );

            moreToolsClickLayout.setVisibility(
                    moreToolsClickLayoutVisibility
                            ? View.VISIBLE
                            : View.GONE
            );


        });

        moreToolsLayout.setOnClickListener(v -> {

           moreToolsVisibility();

        });

        replacePage.setOnClickListener(v -> {

            moreToolsVisibility();

            currentRequest=ImageRequestType.REPLACE_PAGE;

            Intent intent=Image_picker.openImageFiles();
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false);

            pickImagesLauncher.launch(intent);


        });

        extractPage.setOnClickListener(v -> {
            exportLayout();

        });

        rotateLeft.setOnClickListener(v -> {

            moreToolsVisibility();

            rotateBitmap(-90);

        });

        rotateRight.setOnClickListener(v -> {

            moreToolsVisibility();

            rotateBitmap(90);

        });

        addWatermark.setOnClickListener(v -> {

            showWatermarkLayout();

        });

        saveChanges.setOnClickListener(v ->{
                    saveChangesToPage();
        });

        crop.setOnClickListener(v -> {

            showCropLayout();

        });

        addImage.setOnClickListener(v -> {

            currentRequest=ImageRequestType.ADD_IMAGE;

            Intent intent = Image_picker.openImageFiles();
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false);
            pickImagesLauncher.launch(intent);




        });

        addText.setOnClickListener(v -> {

           addTextLayout();


        });

        eraser.setOnClickListener(v -> {

            eraserLayout();

        });


        activeOverlay = new Stack<>();

        setUpColorPicker();
        ShowSaveBtn(false);
        setUpUndoRedo();
        onBackPressedBtn();
        imageLauncher();


    }

    private void initializePage(View view){

        undo = view.findViewById(R.id.undo_btn);
        redo = view.findViewById(R.id.redo_btn);

        loadingBar = view.findViewById(R.id.loading_overlay);
        loadingBar_text = view.findViewById(R.id.text_LoadingBar);
        progressBar = view.findViewById(R.id.progress_bar);
        progressBar.setIndicatorColor(ContextCompat.getColor(requireContext(),R.color.blue_color));

        editPageLayout = view.findViewById(R.id.edit_page);
        edit_page_imageView = view.findViewById(R.id.edit_page_image);
        edit_page_cardview = view.findViewById(R.id.card_edit_pg_img_view);
        prevPage = view.findViewById(R.id.btn_prev_page);
        nextPage = view.findViewById(R.id.btn_next_page);
        pageCounter = view.findViewById(R.id.page_counter);
        addText = view.findViewById(R.id.addText_Layout);
        addImage = view.findViewById(R.id.addImage_layout);
        addWatermark = view.findViewById(R.id.waterMark_Layout);
        sign = view.findViewById(R.id.sign_layout);
        eraser = view.findViewById(R.id.eraserLayout);
        crop = view.findViewById(R.id.cropLayout);
        delete = view.findViewById(R.id.deleteLayout);
        moreToolsLayout = view.findViewById(R.id.moreToolsLayout);
        moreToolsClickLayout = view.findViewById(R.id.more_tools_click_layout);
        replacePage = view.findViewById(R.id.replace_page);
        extractPage = view.findViewById(R.id.extract_page);
        rotateLeft = view.findViewById(R.id.rotate_left);
        rotateRight = view.findViewById(R.id.rotate_right);
        back_arrow_editPage=view.findViewById(R.id.backArrow_edit_pdf_inp_frag);
        saveChanges=view.findViewById(R.id.preview_or_save_edit_pdf);
        saveChanges.setText("Save");
        saveChanges.setPadding(60,10,60,10);
        saveChanges.setBackgroundResource(R.drawable.save_layout_bg);
        saveChanges.setTextColor(ContextCompat.getColor(requireContext(),R.color.white));
        saveChanges.setTypeface(saveChanges.getTypeface(), Typeface.BOLD);
        saveChanges.setTextSize(Dp_to_Px.dpToPx(6));


        //  save or discard changes  layout elements

        save_discard_layout=view.findViewById(R.id.save_discard_Overlay);
        cancelSave_discardLayout=view.findViewById(R.id.btnCancelExit);
        saveChangesBtn_save_dis_layout=view.findViewById(R.id.btnSaveChanges);
        discardChangesBtn_save_dis_layout=view.findViewById(R.id.btnDiscardChanges);
        saveOrDiscardChanges();

        moreToolsOverlay  = view.findViewById(R.id.more_tools_overlay);


        // for watermark layout screen


        btnText = view.findViewById(R.id.btn_text_watermark);
        btnImage = view.findViewById(R.id.btn_image_watermark);
        edtText = view.findViewById(R.id.edt_watermark_text);
        btnSelectImage = view.findViewById(R.id.upload_watermark_image);
        btnApplyWatermark = view.findViewById(R.id.btn_apply_watermark);
        opacitySlider = view.findViewById(R.id.slider_opacity);
        sizeSlider = view.findViewById(R.id.slider_size);
        rotationSlider = view.findViewById(R.id.slider_rotation);
        watermarkOverlay = view.findViewById(R.id.watermark_overlay);
        back_arrow_watermark = view.findViewById(R.id.backArrow_watermark);
        watermark_logo_imageView = view.findViewById(R.id.watermark_logo_image);
        preview_watermark_text = view.findViewById(R.id.preview_watermark_text);
        opacity_value = view.findViewById(R.id.opacity_value);
        size_value = view.findViewById(R.id.size_value);
        rotation_value = view.findViewById(R.id.rotation_value);
        size_textView = view.findViewById(R.id.size_textview);
        selectedImageCard = view.findViewById(R.id.cardSelectedWatermarkImage);
        selected_img_preview = view.findViewById(R.id.imgSelectedWatermark);
        imageNametv = view.findViewById(R.id.tvWatermarkImageName);
        change_img_tv = view.findViewById(R.id.tvChangeWatermarkImage);
        dropdown_position = view.findViewById(R.id.dropdown_position);
        watermarkPreviewContainer = view.findViewById(R.id.preview_container);


        // main preview page
        edit_page_imageView.setMinimumScale(1f);
        edit_page_imageView.setMediumScale(2.5f);
        edit_page_imageView.setMaximumScale(8f);

        //  crop layout

        cropLayout = view.findViewById(R.id.cropOverlay);
        cropImageView = view.findViewById(R.id.cropImageView);
        backArrowCropLayout = view.findViewById(R.id.backBtnCropLayout);
        applyBtnCropLayout = view.findViewById(R.id.applyBtnCropLayout);
        cropInfo= view.findViewById(R.id.cropInfoText);
        resetCrop = view.findViewById(R.id.resetBtnCropLayout);

        // export layout

        back_arrow_export_page = view.findViewById(R.id.back_arrow_export_page);
        exportLayout = view.findViewById(R.id.exportOverlay);
        edtTxtExpPage = view.findViewById(R.id.edit_fileName);
        exportPdf = view.findViewById(R.id.option_pdf);
        exportPng = view.findViewById(R.id.option_png);
        exportJpg = view.findViewById(R.id.option_jpeg);
        exportPage = view.findViewById(R.id.btn_export_page);
        checkBoxExpPage = view.findViewById(R.id.cb_share_after_export);
        radioPDF = view.findViewById(R.id.radio_pdf);
        radioJPEG = view.findViewById(R.id.radio_jpeg);
        radioPNG = view.findViewById(R.id.radio_png);
        extension_exp_pdf = view.findViewById(R.id.tv_extension_exp_pdf);

        // add image view

        back_arrow_addImage = view.findViewById(R.id.back_arrow_addImage);
        addImageView = view.findViewById(R.id.addImageView);
        addImageOverlay = view.findViewById(R.id.addImageOverlay);
        cancelAddImage = view.findViewById(R.id.cancel_add_img);
        applyAddImage = view.findViewById(R.id.apply_add_img);
        swapImg_addImg = view.findViewById(R.id.swap_image);
        flip_hor = view.findViewById(R.id.btnFlipHorizontal);
        flip_ver = view.findViewById(R.id.btnFlipVertical);


        // add text view


        addTextOverlay = view.findViewById(R.id.addTextOverlay);
        fontSelector = view.findViewById(R.id.fontSelector);
        fontName = fontSelector.findViewById(R.id.tvFontName);
        colorSelector  = view.findViewById(R.id.colorSelector);
        styleSelector = view.findViewById(R.id.weightSelector);
        stylePreview = styleSelector.findViewById(R.id.tvPreview_add_font);
        styleTitle = styleSelector.findViewById(R.id.tvFontName);
        addTextView = view.findViewById(R.id.addTextView);
        addTextEditText = view.findViewById(R.id.etTextEditor);
        addTextOpacitySlider = view.findViewById(R.id.sliderOpacity);
        addTextSizeSlider = view.findViewById(R.id.sliderTextSize);
        addMoreText_addtextLayout = view.findViewById(R.id.addText_addtext);
        addTextResetBtn = view.findViewById(R.id.btnResetText);

        //  default style preview
        updateStylePreview();



        //  color picker

        colorPicker  = view.findViewById(R.id.colorPicker);
        hexTextBox = view.findViewById(R.id.etHex);
        applyTextColor = colorPicker.findViewById(R.id.apply_add_img);
        cancelColorPicker = colorPicker.findViewById(R.id.cancel_add_img);

        // eraser

        eraserOverlay = view.findViewById(R.id.eraserOverLay);
        eraserView = view.findViewById(R.id.eraserView);
        resetEraser = view.findViewById(R.id.btnResetEraser);
        eraseSizetv = view.findViewById(R.id.tvEraserSizeValue);
        eraserSizeSlider = view.findViewById(R.id.eraserSizeSlider);
        eraserColorPreview = view.findViewById(R.id.eraserColorPreview);
        applyEraserbtn = view.findViewById(R.id.btnApplyEraser);
        cancelEraserBtn = view.findViewById(R.id.btnCancelEraser);


    }

    private void moreToolsVisibility(){
        moreToolsClickLayoutVisibility = !moreToolsClickLayoutVisibility ;

        moreToolsOverlay.setVisibility( moreToolsClickLayoutVisibility
                ? View.VISIBLE
                : View.GONE
        );

        moreToolsClickLayout.setVisibility(
                moreToolsClickLayoutVisibility
                        ? View.VISIBLE
                        : View.GONE
        );



    }

    private void imageLauncher() {

        pickImagesLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),

                        result -> {

                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                                Intent data = result.getData();

                                if (data.getData() != null) {

                                    if(currentRequest ==  ImageRequestType.WATERMARK ){

                                        watermark_logo_list.clear();

                                        watermark_logo_list.add(data.getData());

                                        handleImageSelection();

                                    }

                                    else if (currentRequest == ImageRequestType.REPLACE_PAGE) {

                                        ReplacePageUri=data.getData();

                                        handleReplaceBitmapClick();
                                    }

                                    else if (currentRequest == ImageRequestType.ADD_IMAGE) {

                                        PageItem item=new PageItem(data.getData(),-1);


                                        addImage_addImgLayout(item);

                                        if(activeOverlay.peek() != addImageOverlay){

                                            addImageLayout();
                                        }




                                    }

                                    else if (currentRequest == ImageRequestType.SWAP_IMG_ADD_IMAGE) {

                                        swapImg_addImg_uri =  new PageItem(data.getData(), -1) ;

                                        addImage_addImgLayout(swapImg_addImg_uri);


                                    }


                                }
                            }
                        }
                );


    }

    private void loadPage(PageItem item) throws IOException {

        if(modifiedIdSet.contains(item.getId())) {

            Bitmap cachedBitmap_watermark = modifiedBitmaps.get(item.getId());

            if (cachedBitmap_watermark != null &&  !item.isDirty() ) {

                edit_page_imageView.setImageBitmap(cachedBitmap_watermark);

            }

            else {


                edit_page_imageView.setImageResource(R.drawable.applyingchanges);


                handleModifiedPage(item.getId())

                        .thenAccept(bitmap -> {

                            requireActivity().runOnUiThread(() ->

                                    edit_page_imageView.setImageBitmap(bitmap));



                                    item.setDirty(false);

                                    modifiedBitmaps.put(item.getId() , bitmap);

                        })

                        .exceptionally(e -> {

                            Toast.makeText(requireContext(),"Something Went Wrong!!",Toast.LENGTH_LONG).show();

                            return null;

                        });

                }
        }

        else{ // for showing the normal bitmap pdf pages


            Bitmap bitmapCached= cachedBitmap.get(item.getId());

            if(bitmapCached!=null) {
                Glide.with(requireContext())
                        .asBitmap()
                        .load(bitmapCached)
                        .thumbnail(0.2f)
                        .into(edit_page_imageView);
                return;
            }

            if(item.getSource() == PageItem.Source.PDF) {

                GeneratePdfBitmap(item, false, null)
                        .thenAccept(bitmap -> {


                            Glide.with(requireContext())
                                    .asBitmap()
                                    .load(bitmap)
                                    .thumbnail(0.2f)
                                    .into(edit_page_imageView);

                        });

            }

            else if (item.getSource() == PageItem.Source.IMAGE) {

                GeneratePdfBitmap(item, true, item.getImageUri())
                        .thenAccept(bitmap -> {


                            Glide.with(requireContext())
                                    .asBitmap()
                                    .load(bitmap)
                                    .thumbnail(0.2f)
                                    .into(edit_page_imageView);

                        });

            }


        }



    }

    private void showWatermarkLayout() {

        watermarkOverlay.setVisibility(View.VISIBLE);

        activeOverlay.push(watermarkOverlay);

        watermark_logo_list.clear(); //  clearing up the cached image (if present)

        logoWatermarkBitmap=null;

        textWatermarkClick();

        btnText.setOnClickListener(v -> {

            if(!isImageWatermark) return;

            textWatermarkClick();

        });

        btnImage.setOnClickListener(v -> {

            if (isImageWatermark)return;

            imageWatermarkClick();

        });

        btnApplyWatermark.setOnClickListener(v -> {

            Bitmap image=null;
            String watermarkText = null;

            if(isImageWatermark){

                image=logoWatermarkBitmap;
                if (image==null) {
                    Toast.makeText(requireContext(),"Select Watermark Image",Toast.LENGTH_LONG).show();
                    return;
                }


            }
            else {

                watermarkText=edtText.getText().toString();

                if (watermarkText.trim().isEmpty()) {
                    Toast.makeText(requireContext(),"Enter Watermark text",Toast.LENGTH_LONG).show();
                    return;
                }


            }


            float opacity = opacitySlider.getValue();
            float size =  sizeSlider.getValue();

            int rotation=(int) rotationSlider.getValue();

            WatermarkOperation watermarkOperation=new WatermarkOperation(
                            requireContext(),
                            !watermark_logo_list.isEmpty()? watermark_logo_list.get(0):null,
                            isImageWatermark,
                            watermarkText,
                            currentPosition,
                            size,
                            rotation,
                            opacity );




            undoStack.add(watermarkOperation);

            isAnyOpPerformed=true;

            Long currentPageItemId=currentPageitem.getId();

            modifiedPagesMap
                    .computeIfAbsent(currentPageItemId, key -> new ArrayList<>())
                    .add(watermarkOperation);

            modifiedIdSet.add(currentPageItemId);

            try {
                currentPageitem.setDirty(true);

                loadPage(currentPageitem);


            }
            catch (IOException e) {

                Toast.makeText(requireContext(),"Something went wrong!",Toast.LENGTH_LONG).show();

            }

            awakeUndoRedo(false);

            ShowSaveBtn(true);

            hasUnsavedChanges=true; // save or discard layout

            watermarkOverlay.setVisibility(View.GONE);

            activeOverlay=null;




        });

        back_arrow_watermark.setOnClickListener(v -> {

            watermarkOverlay.setVisibility(View.GONE);
            activeOverlay=null;

        });

        btnSelectImage.setOnClickListener(v -> {

            currentRequest=ImageRequestType.WATERMARK;
            Intent intent = Image_picker.openImageFiles();
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false);
            pickImagesLauncher.launch(intent);

        });


        opacitySlider.addOnChangeListener(
                (slider, value, fromUser) -> {

                    float opacity = value;

                    int size = (int) sizeSlider.getValue();

                    int rotation = (int) rotationSlider.getValue();

                    modifyWatermark(opacity, size, rotation, isImageWatermark);


                }
        );

        sizeSlider.addOnChangeListener(
                (slider, value, fromUser) -> {

                    int size = Math.round(value);

                    float opacity = opacitySlider.getValue();

                    int rotation = (int) rotationSlider.getValue();

                    modifyWatermark(opacity, size, rotation, isImageWatermark);


                });

        rotationSlider.addOnChangeListener(
                (slider, value, fromUser) -> {

                    int rotation = Math.round(value);
                    float opacity = opacitySlider.getValue();
                    int size = (int) sizeSlider.getValue();

                    modifyWatermark(opacity, size, rotation, isImageWatermark);


                });



        edtText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                preview_watermark_text.setText(s.toString());
                if (edtText.getText().toString().trim().length() == 0) {
                    preview_watermark_text.setText("Watermark");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        change_img_tv.setOnClickListener(v ->{

            currentRequest=ImageRequestType.WATERMARK;
            Intent intent=Image_picker.openImageFiles();
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false);
            pickImagesLauncher.launch(intent);

        });


        // setting up the items of positions

        WatermarkPosition[] positions = WatermarkPosition.values();

        ArrayAdapter<WatermarkPosition> arrayAdapter = new ArrayAdapter<>(
                requireContext(),
                R.layout.dropdown_item,
                positions);

        dropdown_position.setAdapter(arrayAdapter);
        dropdown_position.setText("Center", false);

        dropdown_position.setOnItemClickListener((parent, view, position, id) -> {

            WatermarkPosition selected = (WatermarkPosition) parent.getItemAtPosition(position);

            currentPosition = selected;


            updateWatermarkPosition();
        });

        dropdown_position.setDropDownBackgroundResource(R.drawable.dropdown_bg);
        dropdown_position.setDropDownVerticalOffset(Dp_to_Px.dpToPx(8));
        dropdown_position.setOnClickListener(v -> dropdown_position.showDropDown());


    }

    // whenever the sliders changes it calls this method
    private void modifyWatermark(float opacity, int size, int rotation, boolean isImageWatermark){

        if (isImageWatermark) {

            float scale = size / 100f;

            watermark_logo_imageView.setAlpha(opacity);
            watermark_logo_imageView.setRotation(rotation);
            watermark_logo_imageView.setScaleX(scale);
            watermark_logo_imageView.setScaleY(scale);


            size_value.setText(size + "%");


        } else {

            preview_watermark_text.setAlpha(opacity);
            preview_watermark_text.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
            preview_watermark_text.setRotation(rotation);

            size_value.setText(size + "px");


        }
        DecimalFormat df = new DecimalFormat("#.##");
        opacity_value.setText(df.format(opacity));
        rotation_value.setText(rotation + "°");

    }

    private void setUpSlider(boolean isImageWatermark){

        if (isImageWatermark) {

            sizeSlider.setValueFrom(10);
            sizeSlider.setValueTo(300);
            sizeSlider.setValue(100);
            sizeSlider.setStepSize(1);

            size_value.setText("100%");
            size_textView.setText("Size (%)");

            // setting to default values

            watermark_logo_imageView.setScaleX(1);
            watermark_logo_imageView.setScaleY(1);
            selectedImageCard.setVisibility(View.GONE);


        }
        else {

            sizeSlider.setValueFrom(1);
            sizeSlider.setValueTo(64);
            sizeSlider.setStepSize(1);
            sizeSlider.setValue(24);


            size_textView.setText("Size (px)");
            preview_watermark_text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);

            watermark_logo_list.clear();
            logoWatermarkBitmap=null;
            selectedImageCard.setVisibility(View.GONE);



        }

        rotationSlider.setValue(0);
        opacitySlider.setValue(0.3f);
        opacity_value.setText("0.3");
        rotation_value.setText("0°");

    }

    private void textWatermarkClick(){



        edtText.setVisibility(View.VISIBLE);
        btnSelectImage.setVisibility(View.GONE);

        setUpSlider(false);

        isImageWatermark = false;

        size_value.setText("24px");


        watermark_logo_list.clear(); //  clearing up the cached image (if present)

        watermark_logo_imageView.setVisibility(View.GONE);
        preview_watermark_text.setVisibility(View.VISIBLE);

        btnText.setBackgroundResource(R.drawable.bg_segment_selected);
        btnText.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_color));
        btnText.setAlpha(1);


        btnImage.setBackgroundResource(R.drawable.bg_segment_container);
        btnImage.setTextColor(ContextCompat.getColor(requireContext(), R.color.darkGray));
        btnImage.setAlpha(.3f);

        sizeSlider.setValueFrom(1);
        sizeSlider.setValueTo(64);
        sizeSlider.setStepSize(1);
        sizeSlider.setValue(24);
        size_textView.setText(R.string.size_px);

        watermark_logo_list.clear();
        logoWatermarkBitmap=null;
        selectedImageCard.setVisibility(View.GONE);
        rotationSlider.setValue(0);
        opacitySlider.setValue(0.3f);
        opacity_value.setText("0.3");
        rotation_value.setText("0°");
        edtText.setText("");
        preview_watermark_text.setText(R.string.watermark);
        preview_watermark_text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        currentPosition=WatermarkPosition.CENTER;
        preview_watermark_text.setAlpha(0.3f);
        updateWatermarkPosition();


    }

    private void imageWatermarkClick(){



        edtText.setVisibility(View.GONE);
        btnSelectImage.setVisibility(View.VISIBLE);

        watermark_logo_imageView.setVisibility(View.VISIBLE);
        watermark_logo_imageView.setImageBitmap(null);
        preview_watermark_text.setVisibility(View.GONE);

        setUpSlider(true);
        isImageWatermark = true;
        size_value.setText("100%");

        btnImage.setBackgroundResource(R.drawable.bg_segment_selected);
        btnImage.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_color));
        btnImage.setAlpha(1f);

        btnText.setBackgroundResource(R.drawable.bg_segment_container);
        btnText.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.darkGray)
        );
        btnText.setAlpha(.3f);


    }

    //  for generating the  watermark image preview
    private Bitmap generateBitmapFromUri(Uri imageUri) throws IOException{



        Bitmap imageBitmap;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.Source source =
                    ImageDecoder.createSource(requireContext().getContentResolver(), imageUri);

            imageBitmap = ImageDecoder.decodeBitmap(source, (decoder, info, src) -> {
                decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                decoder.setMutableRequired(true);
            });
        } else {
            imageBitmap = MediaStore.Images.Media.getBitmap(
                    requireContext().getContentResolver(),
                    imageUri
            );

            imageBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
        }
        return imageBitmap;
    }

    private CompletableFuture<Bitmap> GeneratePdfBitmap(PageItem item, boolean isImageUri, Uri imageUri) {

        CompletableFuture<Bitmap> future = new CompletableFuture<>();

        executorService.execute(() -> {

            Bitmap bitmap;

            if(isImageUri){

                try {

                    bitmap= GenerateBitmapFromImgUri
                             .generateBitmapFromUri(requireContext(), imageUri, .7f);

                }

                catch (IOException e) {

                    throw new RuntimeException(e);
                }


            }

            else {

                bitmap = getBitmapFromPDF.renderHighQualPage(
                                item.getPdfPageIndex(),
                                pdfRenderer,
                                edit_page_imageView.getWidth());


            }




            handler.post(() -> {

                cachedBitmap.put(item.getId(), bitmap);
                future.complete(bitmap);
            });

        });

        return future;
    }

    private void handleImageSelection(){

        executorService.execute(() -> {

            Bitmap bitmap = null;

            try {

                bitmap = generateBitmapFromUri(watermark_logo_list.get(0));

            } catch (IOException e) {

                throw new RuntimeException(e);
            }

            Bitmap finalBitmap = bitmap;

            logoWatermarkBitmap=bitmap;

            handler.post(() -> {
                watermark_logo_imageView.setImageBitmap(finalBitmap);
                selected_img_preview.setImageBitmap(finalBitmap);

            });
        });

        selectedImageCard.setVisibility(View.VISIBLE);
        btnSelectImage.setVisibility(View.GONE);

    }

    private void updateWatermarkPosition(){

        final int margin = Dp_to_Px.dpToPx(12);

        watermarkPreviewContainer.post(() -> {

            int parentWidth = watermarkPreviewContainer.getWidth();
            int parentHeight = watermarkPreviewContainer.getHeight();

            View watermark = isImageWatermark
                    ? watermark_logo_imageView
                    : preview_watermark_text;

            watermark.setPivotX(0f);
            watermark.setPivotY(0f);

            float w = watermark.getWidth() * watermark.getScaleX();
            float h = watermark.getHeight() * watermark.getScaleY();


            switch (currentPosition) {

                case TOP_LEFT:
                    watermark.setX(margin);
                    watermark.setY(margin);
                    break;

                case TOP_CENTER:
                    watermark.setX((parentWidth - w) / 2f);
                    watermark.setY(margin);
                    break;

                case TOP_RIGHT:
                    watermark.setX(parentWidth - w - margin);
                    watermark.setY(margin);
                    break;

                case CENTER_LEFT:
                    watermark.setX(margin);
                    watermark.setY((parentHeight - h) / 2f);
                    break;

                case CENTER:
                    watermark.setX((parentWidth - w) / 2f);
                    watermark.setY((parentHeight - h) / 2f);
                    break;

                case CENTER_RIGHT:
                    watermark.setX(parentWidth - w - margin);
                    watermark.setY((parentHeight - h) / 2f);
                    break;

                case BOTTOM_LEFT:
                    watermark.setX(margin);
                    watermark.setY(parentHeight - h - margin);
                    break;

                case BOTTOM_CENTER:
                    watermark.setX((parentWidth - w) / 2f);
                    watermark.setY(parentHeight - h - margin);
                    break;

                case BOTTOM_RIGHT:
                    watermark.setX(parentWidth - w - margin);
                    watermark.setY(parentHeight - h - margin);
                    break;
            }
        });


    }

    private CompletableFuture<Bitmap> handleModifiedPage(long id) {

        List<PageOperation> operations = modifiedPagesMap.get(id);




        if (operations == null || operations.isEmpty()) {

            Toast.makeText(requireContext(),"Something Went Wrong!!",Toast.LENGTH_LONG).show();

            return CompletableFuture.completedFuture(null);
        }

        PageItem pageItem = null;

        for (PageItem item : pdfSelectedPages) {
            if (item.getId() == id) {
                pageItem = item;


                break;
            }
        }

        if (pageItem == null) {

            Toast.makeText(requireContext(),"Something Went Wrong!!",Toast.LENGTH_LONG).show();

            return CompletableFuture.completedFuture(null);
        }

        Bitmap cached = cachedBitmap.get(id);

        if (cached != null) {

            return applyOperations(cached, operations);
        }



        if (pageItem.getSource() == PageItem.Source.PDF) {



            return GeneratePdfBitmap(
                    pageItem,
                    false,
                    null
            )
            .thenCompose(bitmap -> {

                cachedBitmap.put(id, bitmap);

                return applyOperations(bitmap, operations);

            });
        }

        else if (pageItem.getSource() == PageItem.Source.IMAGE){

            return GeneratePdfBitmap(
                    null,
                    true,
                    pageItem.getImageUri()

            ).thenCompose(bitmap -> {

                cachedBitmap.put(id, bitmap);

                return applyOperations(bitmap, operations);

            });

        }

        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Bitmap> applyOperations(Bitmap bitmap,List<PageOperation> operations) {




        return CompletableFuture.supplyAsync(() -> {



            PageRenderContext renderContext = new PageRenderContext(bitmap);

            for (PageOperation operation : operations) {


                try {

                    operation.apply(renderContext);


                }
                catch (IOException e) {

                   Toast.makeText(requireContext(),"Couldn't apply the edit. Please try again.",Toast.LENGTH_LONG)
                           .show();

                   return  bitmap;



                }


            }


            return renderContext.getBitmap();

        }, executorService);
    }

    private void ShowSaveBtn(boolean show){

        saveChanges.setVisibility(show ? View.VISIBLE: View.GONE);
    }

    private void saveOrDiscardChanges(){

        saveChangesBtn_save_dis_layout.setOnClickListener(v ->{

            save_discard_layout.setVisibility(View.GONE);

            executorService.execute(() -> {

                saveChangesToPage();

                handler.post(() -> {

                    if(activeOverlay!=null) activeOverlay.pop().setVisibility(View.GONE);

                    else getParentFragmentManager().popBackStack();

                });

            });




        });

        discardChangesBtn_save_dis_layout.setOnClickListener(v -> {

            save_discard_layout.setVisibility(View.GONE);

            if(activeOverlay!=null){

                activeOverlay.pop().setVisibility(View.GONE);

            }
            else getParentFragmentManager().popBackStack();

        });

        cancelSave_discardLayout.setOnClickListener(v -> {

            save_discard_layout.setVisibility(View.GONE);

        });


    }

    private void onBackPressedBtn() {

        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (hasUnsavedChanges) {

                            // Show your save/discard overlay
                            save_discard_layout.setVisibility(View.VISIBLE);

                        } else {

                            // No changes, simply go back
                            setEnabled(false);
                            if(activeOverlay!=null){
                                activeOverlay.pop().setVisibility(View.GONE);
                            }
                            else  requireActivity().getOnBackPressedDispatcher().onBackPressed();


                        }
                    }
                });
    }

    private void awakeUndoRedo(boolean savedChangesClick){

        int tintColor = (undoStack.isEmpty()) ? R.color.grey : R.color.black;

        undo.setImageTintList(ContextCompat.getColorStateList(requireContext(), tintColor));

        tintColor = (redoStack.isEmpty()) ? R.color.grey : R.color.black;

        redo.setImageTintList(ContextCompat.getColorStateList(requireContext(), tintColor));


        if(savedChangesClick){

            undo.setImageTintList(ContextCompat.getColorStateList(requireContext(), R.color.grey));
            redo.setImageTintList(ContextCompat.getColorStateList(requireContext(), R.color.grey));


        }



    }

    private void setUpUndoRedo(){

        undo.setOnClickListener(v ->{

            if(undoStack.isEmpty()) {
            }

            else{
                PageOperation op=undoStack.pop();

//                int pageNum=op.getPageNumber();
//

//                try {
////                    loadPage(pageNum);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//
//                redoStack.push(op);



                awakeUndoRedo(false);


            }
        });

        redo.setOnClickListener(v ->{

            if(redoStack.isEmpty()) {
            }

            else{
//                EditOperation op=redoStack.pop();
//
//                int pageNum=op.getPageNumber();
//
//                OperationsPerformedPages.add(pageNum);
//
//                try {
//
//                    loadPage(pageNum);
//
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//
//                undoStack.push(op);
//                awake_Undo_redo(false);


            }

        });

    }

    private void saveChangesToPage(){


//        if( isAnyOpPerformed && !undoStack.isEmpty()){
//
//            awake_Undo_redo(true);
//
//            executorService.execute(() -> {
//
//                for(EditOperation op:undoStack){
//
//                    pdfOperationModel.applyOperation(op);
//
//                }
//
//            handler.post(() -> {
//                hasUnsavedChanges=false;
//            });
//
//            });







//        }


    }

    private void showCropLayout(){

        cropLayout.setVisibility(View.VISIBLE);

        activeOverlay.push(cropLayout);

        backArrowCropLayout.setOnClickListener(v -> {
            cropLayout.setVisibility(View.GONE);
        });

        PageItem item=pdfSelectedPages.get(currentPageIndex);

        if(modifiedIdSet.contains(item.getId())){


            Bitmap bitmap= modifiedBitmaps.get(item.getId());

            if(bitmap != null && !item.isDirty()){

                cropImageView.setBitmap(bitmap);

            }
            else{


                handleModifiedPage(item.getId())

                        .thenAccept(bitmap1 -> {

                            requireActivity().runOnUiThread(() ->

                                    cropImageView.setBitmap(bitmap1));

                            item.setDirty(false);

                            modifiedBitmaps.put(item.getId() , bitmap);

                        })

                        .exceptionally(e -> {

                            Toast.makeText(requireContext(),"Something Went Wrong!!",Toast.LENGTH_LONG).show();

                            return null;

                        });


            }

        }

        else {
            if (item.getSource() == PageItem.Source.IMAGE) {

                GeneratePdfBitmap(item, true, item.getImageUri())
                        .thenAccept(bitmap -> {

                            cropImageView.setBitmap(bitmap);


                        });

            }
            else {


                GeneratePdfBitmap(item, false, null)
                        .thenAccept(bitmap -> {

                            cropImageView.setBitmap(bitmap);


                        });

            }
        }

        cropImageView.setOnCropChangedListener(new CropImageView.OnCropChangedListener() {
            @Override
            public void onCropChanged(Rect rect) {
                cropInfo.setText(
                        rect.width() + " × " + rect.height() + " px"
                );
            }

            @Override
            public void isCropChanged(boolean isChanged) {

                if (isChanged) {
                    resetCrop.setVisibility(View.VISIBLE);
                } else {
                    resetCrop.setVisibility(View.GONE);
            }
        }
    });

        resetCrop.setOnClickListener(v ->{

            cropImageView.resetCrop();

        });

        applyBtnCropLayout.setOnClickListener(v -> {

            Rect coordinates=cropImageView.getBitmapCropRect();

            CropOperation cropOperation=new CropOperation(coordinates);

            addOperation(cropOperation);

            cropLayout.setVisibility(View.GONE);



        });



    }

    private  void  rotateBitmap(int degrees){

        RotateOperation rotateOperation= new RotateOperation(degrees);

        addOperation(rotateOperation);

        moreToolsClickLayout.setVisibility(View.GONE);
        moreToolsClickLayoutVisibility=false;

    }

    private void  addOperation(PageOperation operation){

        undoStack.add(operation);

        isAnyOpPerformed=true;

        Long currentPageItemId=currentPageitem.getId();

        modifiedPagesMap
                .computeIfAbsent(currentPageItemId, key -> new ArrayList<>())
                .add(operation);

        modifiedIdSet.add(currentPageItemId);

        try {

            currentPageitem.setDirty(true);

            loadPage(currentPageitem);


        } catch (IOException e) {

            Toast.makeText(requireContext(),"Something went wrong!",Toast.LENGTH_LONG).show();

        }

        awakeUndoRedo(false);

        ShowSaveBtn(true);

        hasUnsavedChanges=true; // save or discard layout

        activeOverlay=null;

    }

    private  void  handleReplaceBitmapClick(){

        Uri imageUri=ReplacePageUri;

        ReplaceOperation replaceOperation=new ReplaceOperation(requireContext(),imageUri);


        addOperation(replaceOperation);


    }

    private void exportLayout(){

        moreToolsVisibility();

        exportLayout.setVisibility(View.VISIBLE);

        activeOverlay.push(exportLayout);

        edtTxtExpPage.setText(
                currentPageitem.getSource() == PageItem.Source.PDF ?
                        "Page_"+ (currentPageitem.getPdfPageIndex()+1) :
                        "Page_"+ (currentPageitem.getImageFakeIndex()+1) );


        updateRadioBtnExpPage();

        edtTxtExpPage.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});

        back_arrow_export_page.setOnClickListener(v -> {
            exportLayout.setVisibility(View.GONE);
        });

        exportPdf.setOnClickListener(v -> {

            currentRadioBtn= RadioButtonExtract.PDF;
            updateRadioBtnExpPage();

        });

        exportPng.setOnClickListener(v -> {
            currentRadioBtn= RadioButtonExtract.PNG;
            updateRadioBtnExpPage();

        });

        exportJpg.setOnClickListener(v -> {
            currentRadioBtn= RadioButtonExtract.JPEG;
            updateRadioBtnExpPage();

        });

        exportPage.setOnClickListener(v -> {

            loadingBar_text.setText(R.string.exporting);

            loadingBar.setVisibility(View.VISIBLE);

            getBitmapForExtractPage(currentPageitem)
                    .thenAccept(bitmap -> {

                        ExtractOperation extractOperation=new ExtractOperation(
                                requireContext(),
                                bitmap,
                                currentRadioBtn,
                                edtTxtExpPage.getText().toString().trim());

                        executorService.execute(() -> {
                             Uri uri= extractOperation.getFileUri();

                             if(currentRadioBtn==RadioButtonExtract.PDF){

                                 SaveFile.savePDF(
                                         requireContext(),
                                         uri,
                                         GetFileName.getFileName(requireContext(),uri),
                                         Page_Count.getPdfPageCount(requireContext(),uri),
                                         Byte_MB.convertByte(File_Size.getFileSize(requireContext(),uri)),
                                         true
                                 );

                             }

                             else{

                                 SaveFile.saveImageFromUri(
                                         requireContext(),
                                         uri,
                                         GetFileName.getFileName(requireContext(),uri),
                                         currentRadioBtn.displayName
                                         );

                             }


                             handler.post(() -> {

                                 loadingBar.setVisibility(View.GONE);

                                 if(checkBoxExpPage.isChecked()) {

                                     Intent intent = new Intent(Intent.ACTION_SEND);

                                     intent.setType("*/*");      // or "image/png", "image/jpeg", "application/pdf"

                                     intent.putExtra(Intent.EXTRA_STREAM, uri);

                                     intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                                     requireContext().startActivity(intent);

                                 }




                             });



                        });



                    });



        });

    }

    private void  updateRadioBtnExpPage(){

        boolean asPDF , asPNG, asJPEG = false;

        asPDF = currentRadioBtn == RadioButtonExtract.PDF;
        asJPEG = currentRadioBtn == RadioButtonExtract.JPEG;
        asPNG = currentRadioBtn == RadioButtonExtract.PNG;
        radioPNG.setBackgroundResource(asPNG ? R.drawable.radio_selected : R.drawable.selector_radio_bg);
        radioJPEG.setBackgroundResource(asJPEG ? R.drawable.radio_selected : R.drawable.selector_radio_bg);
        radioPDF.setBackgroundResource(asPDF ? R.drawable.radio_selected : R.drawable.selector_radio_bg);
        exportPdf.setBackgroundResource(asPDF ? R.drawable.bg_segment_selected : R.drawable.selector_option_card);
        exportPng.setBackgroundResource(asPNG ? R.drawable.bg_segment_selected : R.drawable.selector_option_card);
        exportJpg.setBackgroundResource(asJPEG ? R.drawable.bg_segment_selected : R.drawable.selector_option_card);
        extension_exp_pdf.setText(currentRadioBtn.displayName);

    }

    private  CompletableFuture<Bitmap> getBitmapForExtractPage(PageItem item) {


        if (modifiedIdSet.contains(item.getId())) {

            Bitmap cached = modifiedBitmaps.get(item.getId());

            if (cached != null && !item.isDirty()) {
                return CompletableFuture.completedFuture(cached);
            }

            return handleModifiedPage(item.getId())
                    .thenApply(bitmap -> {

                        item.setDirty(false);
                        modifiedBitmaps.put(item.getId(), bitmap);

                        return bitmap;
                    });
        }

        if(item.getSource()== PageItem.Source.IMAGE){

            Bitmap Bitmap=cachedBitmap.get(item.getId());

            if(Bitmap!=null) {

                return CompletableFuture.completedFuture(Bitmap);

            }

            return GeneratePdfBitmap(item,true,item.getImageUri())
                    .thenApply(bitmap -> {

                        cachedBitmap.put(item.getId(), bitmap);

                        return bitmap;

                    });



        }

        else {

            Bitmap Bitmap=cachedBitmap.get(item.getId());

            if(Bitmap!=null) {

                return CompletableFuture.completedFuture(Bitmap);

            }


            return GeneratePdfBitmap(item,false,null)
                    .thenApply(bitmap -> {

                        cachedBitmap.put(item.getId(), bitmap);

                        return bitmap;

                    });


        }


    }

    private  void  addImageLayout(){


        float[] originalScaleFactor = new float[]{-1};


        activeOverlay.push(addImageOverlay);


        back_arrow_addImage.setOnClickListener(v -> {

            if (hasUnsavedChanges) {

                save_discard_layout.setVisibility(View.VISIBLE);

            }
            else {


                addImageOverlay.setVisibility(View.GONE);

            }


        });


        addImageView.setOnImageSelectionListener(image -> {

            if(image==null){
                swapImg_addImg.setText("Add Image");
                swapImg_addImg.setIconResource(R.drawable.add_btn);

            }
            else{

                swapImg_addImg.setText("Replace");
                swapImg_addImg.setIconResource(R.drawable.replace_page);

            }

        });


        cancelAddImage.setOnClickListener(v -> {

            addedImageUri.clear();
            addImageView.clearTrash();
            addImageOverlay.setVisibility(View.GONE);


        });


        swapImg_addImg.setOnClickListener(v -> {

            Intent intent = Image_picker.openImageFiles();
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);

            if (addImageView.isImgSelected()) {
                currentRequest = ImageRequestType.SWAP_IMG_ADD_IMAGE;
            } else {
                currentRequest = ImageRequestType.ADD_IMAGE;
            }

            pickImagesLauncher.launch(intent);
        });


        addImageOverlay.setVisibility(View.VISIBLE);





        PageItem item=currentPageitem;


        getBitmapForExtractPage(item)

                .thenAccept(bitmap -> {

                originalScaleFactor[0] = getBitmapFromPDF.getScaleFactor(item,pdfRenderer,requireContext());

                addImageView.setPageBitmap(bitmap);


        });


        flip_hor.setOnClickListener(v -> {

            addImageView.flipHorizontal();

        });


        flip_ver.setOnClickListener(v -> {
            addImageView.flipVertical();
        });


        applyAddImage.setOnClickListener(v -> {

            addImageObjectList.clear();

            hasUnsavedChanges = false;


            if(originalScaleFactor[0] == -1 || originalScaleFactor[0] == 0 ) {

                Toast.makeText(requireContext(), "Something went wrong!!",Toast.LENGTH_LONG)
                        .show();
                return;
            }


            List<ImageObject> imageList = addImageView.getImageList();


            Map<Long, PageItem> pageItemMap = new HashMap<>();


            for (PageItem item_ : addedImageUri) {

                pageItemMap.put(item_.getId(), item_);

            }


            for(int i=0;i<imageList.size();i++){

                ImageObject obj = imageList.get(i);

                long id = obj.getId();

                PageItem foundItem=pageItemMap.get(id);

                addImageObjectList.add( new AddImageObject(
                        i,
                        foundItem.getImageUri() ,
                        addImageView.convertViewToPdfBounds(obj.getBounds() , originalScaleFactor[0]) ,
                        obj.getRotation() ,
                        obj.isFlipHorizontal() ,
                        obj.isFlipVertical())
                );


            }


            AddImageOperation addImageOperation = new AddImageOperation(requireContext(), addImageObjectList);

            addOperation(addImageOperation);

            addImageOverlay.setVisibility(View.GONE);

            activeOverlay = null;



        });

    }

    private  void  addImage_addImgLayout(PageItem item){

        if(currentRequest == ImageRequestType.ADD_IMAGE){

            addedImageUri.add(item);

            hasUnsavedChanges=true;


            GeneratePdfBitmap(item,true,item.getImageUri())

                    .thenAccept(bitmap -> {

                        addImageView.setImageBitmap(bitmap, item.getId());

                    });



        }

        else if (currentRequest == ImageRequestType.SWAP_IMG_ADD_IMAGE){

            ImageObject img_item = addImageView.getActiveImage();

            hasUnsavedChanges=true;

            for( PageItem item_ : addedImageUri ){

                if(item_.getId() == img_item.getId() ){

                    item_.setImageUri(item.getImageUri());


                }

            }


            GeneratePdfBitmap(item,true,item.getImageUri())

                    .thenAccept(bitmap -> {

                        addImageView.setReplaceBitmap(bitmap);

                    });



        }




    }

    private  void  setUpColorPicker(){

        colorPicker.setOnColorChangedListener(new ColorPickerView.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {

            }

            @Override
            public void getHex(String hex) {

                hexTextBox.setText(hex);

            }
        });

        cancelColorPicker.setOnClickListener(v -> {

            if(activeOverlay.peek() instanceof ColorPickerView){

                colorPicker.setVisibility(View.GONE);
                activeOverlay.pop();

            }

        });

        applyTextColor.setOnClickListener(v -> {

            if(! (activeOverlay.peek() instanceof  ColorPickerView)) return;

            Log.d("apply color","apply color");

            colorPicker.setVisibility(View.GONE);

            activeOverlay.pop();


            if(activeOverlay.peek() == addTextOverlay){

                Log.d("addTextView", "addTextView");

                addTextTextColor = colorPicker.getSelectedColor();

                AddTextObject object = addTextView.getSelectedTextObject();

                if(object !=   null) {

                    object.setTextColor(addTextTextColor);

                    addTextView.updateTextObject(object);

                    colorPicker.post(() -> {

                        colorSelector.setCardBackgroundColor(addTextTextColor);

                    });

                }


            }

            else if (activeOverlay.peek() == eraserOverlay) {

                int color  = colorPicker.getSelectedColor();

                eraserColorPreview.setBackgroundColor(color);

                if(eraserView != null){

                    eraserView.setEraserColor(color);

                }



            }

        });

    }

    private  void  addTextLayout(){

        addTextOverlay.setVisibility(View.VISIBLE);

        activeOverlay.push( addTextOverlay );

        PageItem item = currentPageitem;

        initializeFontAdapter();

        getBitmapForExtractPage(item)
                .thenAccept(bitmap -> {

                    setUpAddTextEditTextBox();

                    finishAddTextEditTextBox();

                    addTextView.setPageBitmap(bitmap);

                    addTextEditText.post(() -> {

                        addNewText();

                    });

        });


        fontSelector.setOnClickListener(v ->{

            showFontPopup(v);

        });

        stylePreview.setTextColor(
                 ContextCompat.getColorStateList(
                         requireContext(),
                         R.color.black)
        );

        stylePreview.setTypeface(Typeface.DEFAULT);


        styleTitle.setTypeface(Typeface.DEFAULT);

        styleSelector.setOnClickListener(v -> {

            showStylesPopup(v);

        });

        colorSelector.setOnClickListener(v -> {

            colorPicker.setVisibility(View.VISIBLE);

            activeOverlay.push( colorPicker );

            colorPicker.post(() -> {

                colorPicker.setColor(Color.BLUE);

            });

        });



        addTextOpacitySlider.addOnChangeListener(
                (slider , value, fromUser) -> {


            DecimalFormat df = new DecimalFormat("0.00");

            float opacity = Float.parseFloat(df.format(value));

            AddTextObject addTextObject = addTextView.getSelectedTextObject();

            if(addTextObject ==  null) return;

            addTextObject.setOpacity(opacity);

            addTextView.updateTextObject(addTextObject);



        });


        addTextSizeSlider.setValue(Dp_to_Px.dpToPx(24));


        addTextSizeSlider.addOnChangeListener(
                (slider, value, fromUser) -> {

                    if (!fromUser) return;

                    AddTextObject object = addTextView.getSelectedTextObject();

                    if (object == null) return;

                    float validSize = addTextView.getLargestFittingTextSize(
                                    object,
                                    value
                    );

                    boolean cannotIncrease =
                            value > object.getTextSize()
                                    && validSize <= object.getTextSize();

                    if (cannotIncrease && !maxTextSizeToastShown) {

                        Toast.makeText(
                                getContext(),
                                "Maximum text size reached!",
                                Toast.LENGTH_SHORT
                        ).show();

                        maxTextSizeToastShown = true;
                    }

                    // User moved the slider back down.
                    // Allow the toast to appear again when they try increasing.
                    if (value < object.getTextSize()) {
                        maxTextSizeToastShown = false;
                    }

                    object.setTextSize(validSize);

                    addTextView.updateTextObject(object);
                });

        addTextView.setAddTextObjectListener(
                new AddTextView.addTextObjectListener() {
                    @Override
                    public void onTextSizeChanged(float newSize) {

                       addTextSizeSlider.setValue( (int) newSize );

                    }

                    @Override
                    public void objectSelected(AddTextObject object) {

                        addTextEditText.requestFocus();

                        selectTextObjectForEditing(object);

                        updateAddTextObjHelpers(object);


                    }

                    @Override
                    public  void  onPencilClick(AddTextObject object){

                        if(object == null) return;

                        selectTextObjectForEditing(object);

                        openKeyboard();

                    }

                    @Override
                    public  void  onTouchOutsidePencil(boolean isInsideSameTextBox){

                        if(!isInsideSameTextBox)  closeKeyboard();


                    }

                });


        // if user add  more text
        addMoreText_addtextLayout.setOnClickListener(v -> {

            if(activeOverlay.peek() !=  addTextOverlay) return;

            addNewText();


        });

        addTextResetBtn.setOnClickListener(v -> {

         AddTextObject addTextObject = addTextView.getSelectedTextObject();

         if(addTextObject != null){

             addTextView.resetTextObject(addTextObject);

         }

        });

        setUpKeyboard();


    }

    private void setUpAddTextEditTextBox() {

        addTextEditText.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        if (updatingEditText) {
                            return;
                        }

                        if (editingObject == null) {
                            return;
                        }

                        editingObject.setText(s.toString());
                        editingObject.setHint(false);

                        addTextView.updateTextObject(editingObject);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });



        addTextEditText.setFilters(
                new InputFilter[] {

                        new InputFilter() {

                            @Override
                            public CharSequence filter(
                                    CharSequence source,
                                    int start,
                                    int end,
                                    Spanned dest,
                                    int dstart,
                                    int dend) {

                                if (updatingEditText) {
                                    return null;
                                }

                                AddTextObject object = editingObject;

                                if (object == null) {
                                    return null;
                                }

                                // Always allow deletion
                                if (source.length() == 0) {
                                    return null;
                                }

                                String currentText =
                                        dest.toString();

                                String incomingText =
                                        source.subSequence(
                                                start,
                                                end
                                        ).toString();

                                String newText =
                                        currentText.substring(
                                                0,
                                                dstart
                                        )
                                                +
                                                incomingText
                                                +
                                                currentText.substring(
                                                        dend
                                                );

                                if (addTextView.canFitText(
                                        object,
                                        newText
                                )) {

                                    return null;
                                }

                                return "";
                            }
                        }
                });
    }

    private void finishAddTextEditTextBox() {

        addTextEditText.setOnEditorActionListener(
                (v, actionId, event) -> {

                    if (actionId == EditorInfo.IME_ACTION_DONE) {

                        finishTextEditing(editingObject);

                        return true;
                    }

                    return false;
                });
    }

    private void setUpKeyboard() {

        View root = requireActivity()
                .getWindow()
                .getDecorView();

        root.setOnApplyWindowInsetsListener((v, insets) -> {

            boolean currentlyVisible =
                    false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                currentlyVisible = insets.isVisible(WindowInsets.Type.ime());
            }

            // Keyboard changed from visible -> hidden
            if (keyboardVisible
                    && !currentlyVisible
                    && editingObject != null
                    && !finishingTextEditing) {

                AddTextObject object = editingObject;

                finishTextEditing(object);
            }

            keyboardVisible = currentlyVisible;

            return insets;
        });

        // Make sure the listener receives the current state.
        ViewCompat.requestApplyInsets(root);
    }

    private void selectTextObjectForEditing(AddTextObject object) {

        editingObject = object;

        updatingEditText = true;

        try {

            if (object == null) {

                addTextEditText.setText("");

                return;
            }

            String text = object.isHint()
                    ? ""
                    : object.getText();

            originalText = text;

            creatingNewText =  object.isHint() ? true : false;

            addTextEditText.setText(text);

            addTextEditText.setSelection(
                    addTextEditText.length()
            );

        } finally {

            updatingEditText = false;
        }
    }

    private void addNewText() {

        AddTextObject object = addTextView.addNewText();

        // Default font
        object.setFontFamily(addTextFontSelected);

        // Default weight
        object.setFontWeight(selectedWeight);

        // Default formatting
        object.setBold(isBold);
        object.setItalic(isItalic);
        object.setUnderline(isUnderline);
        object.setStrikeThrough(isStrike);

        addTextView.updateTextObject(object);


        addTextEditText.setSelection(addTextEditText.length());

        addTextEditText.setVisibility(View.VISIBLE);

        addTextEditText.requestFocus();

        selectTextObjectForEditing(object);

        updateAddTextObjHelpers(object);

        updatingEditText = false;

        openKeyboard();
    }

    private void openKeyboard() {

        addTextEditText.setVisibility(View.VISIBLE);

        addTextEditText.requestFocus();

        addTextEditText.post(() -> {

            InputMethodManager imm =
                    (InputMethodManager) requireContext()
                            .getSystemService(
                                    Context.INPUT_METHOD_SERVICE
                            );

            if (imm != null) {

                imm.showSoftInput(
                        addTextEditText,
                        InputMethodManager.SHOW_IMPLICIT
                );
            }
        });
    }

    private void closeKeyboard() {

        addTextEditText.clearFocus();

        InputMethodManager imm =
                (InputMethodManager) requireContext()
                        .getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

        if (imm != null) {

            imm.hideSoftInputFromWindow(
                    addTextEditText.getWindowToken(),
                    0
            );
        }
    }

    private void finishTextEditing(AddTextObject object) {

        if (object == null || finishingTextEditing) {
            return;
        }

        finishingTextEditing = true;

        try {

            boolean wasCreatingNewText = creatingNewText;



            String text = object.getText();

            if (text == null || text.trim().isEmpty()) {

                if (wasCreatingNewText) {

                    // New empty textbox -> remove it
                    addTextView.removeTextObject(object);

                    Log.d("if block","");

                }

                else {

                    // Existing textbox -> restore original text
                    object.setText(
                            originalText != null
                                    ? originalText
                                    : ""
                    );

                    addTextView.updateTextObject(object);
                }
            }

            editingObject = null;
            creatingNewText = false;
            originalText = null;


            addTextEditText.clearFocus();


            addTextEditText.setVisibility(View.GONE);

            InputMethodManager imm =
                    (InputMethodManager)
                            requireContext()
                                    .getSystemService(
                                            Context.INPUT_METHOD_SERVICE
                                    );

            if (imm != null) {

                imm.hideSoftInputFromWindow(
                        addTextEditText.getWindowToken(),
                        0
                );
            }

        }
        finally {

            finishingTextEditing = false;
        }
    }

    private void showFontPopup(View anchor) {

        View view = LayoutInflater.from(requireContext())
                .inflate(R.layout.popup_font, null);

        RecyclerView recyclerView =
                view.findViewById(R.id.rvFonts);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        recyclerView.setAdapter(fontAdapter);

        fontPopup = new PopupWindow(
                view,
                550,
                700,
                true
        );

        fontPopup.setElevation(10f);

        fontPopup.setOutsideTouchable(true);

        fontPopup.showAsDropDown(anchor);
    }

    private void initializeFontAdapter() {

        fonts = new ArrayList<>();

        fonts.add(new FontItem(
                "Poppins",
                loadFont("Poppins/poppins_regular.ttf")
        ));

        fonts.add(new FontItem(
                "Inter",
                loadFont("Inter/inter_regular.ttf")
        ));

        fonts.add(new FontItem(
                "Roboto",
                loadFont("Roboto/roboto_regular.ttf")
        ));

        fonts.add(new FontItem(
                "Montserrat",
                loadFont("Montserrat/montserrat_regular.ttf")
        ));

        fonts.add(new FontItem(
                "Lato",
                loadFont("Lato/lato_regular.ttf")
        ));

        fonts.add(new FontItem(
                "Nunito",
                loadFont("Nunito/nunito_regular.ttf")
        ));

        fonts.add(new FontItem(
                "Merriweather",
                loadFont("Merriweather/merriweather_regular.ttf")
        ));

        fonts.add(new FontItem(
                "Playfair Display",
                loadFont("PlayfairDisplay/playfairdisplay_regular.ttf")
        ));

        fonts.add(new FontItem(
                "JetBrains Mono",
                loadFont("JetBrainsMono/jetbrainsmono_regular.ttf")
        ));

        fonts.add(new FontItem(
                "Roboto Mono",
                loadFont("RobotoMono/robotomono_regular.ttf")
        ));

        fonts.add(new FontItem(
                "Pacifico",
                loadFont("Pacifico/pacifico_regular.ttf")
        ));

        fonts.add(new FontItem(
                "Inconsolata",
                loadFont("Inconsolata/inconsolata_regular.ttf")
        ));

        fonts.add(new FontItem(
                "Source Code Pro",
                loadFont("SourceCodePro/sourcecodepro_regular.ttf")
        ));

        fonts.add(new FontItem(
                "Asul",
                loadFont("Asul/asul_regular.ttf")
        ));

        fonts.add(new FontItem(
                "Bitcount Prop Double",
                loadFont("BitcountPropDouble/bitcountpropdouble_regular.ttf")
        ));

        fonts.add(new FontItem(
                "Sekuya",
                loadFont("Sekuya/sekuya_regular.ttf")
        ));

        fontAdapter = new FontAdapter(fonts, null);

        fontAdapter.setOnFontClickListener(item -> {

            addTextFontSelected = item.getFontName();

            AddTextObject object =
                    addTextView.getSelectedTextObject();

            if (object != null) {

                addTextView.changeFont(
                        object,
                        addTextFontSelected
                );

                addTextView.updateTextObject(object);
            }

            fontName.setText(
                    item.getFontName()
            );

            fontPopup.dismiss();

        });
    }

    private void showStylesPopup(View anchor) {

        View view = LayoutInflater.from(requireContext())
                    .inflate(R.layout.popup_styles, null);

        PopupWindow popup = new PopupWindow(
                view,
                550,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popup.setOutsideTouchable(true);
        popup.setElevation(12f);



        MaterialCardView cardLight = view.findViewById(R.id.cardLight);

        MaterialCardView cardRegular = view.findViewById(R.id.cardRegular);

        MaterialCardView cardMedium = view.findViewById(R.id.cardMedium);

        MaterialCardView cardSemiBold = view.findViewById(R.id.cardSemiBold);

        MaterialCardView cardBold = view.findViewById(R.id.cardBold);


        cardLight.setTag(300);
        cardRegular.setTag(400);
        cardMedium.setTag(500);
        cardSemiBold.setTag(600);
        cardBold.setTag(700);


        AddTextObject selectedObject = addTextView.getSelectedTextObject();

        if (selectedObject != null) {

            selectedWeight = selectedObject.getFontWeight();

            isBold = selectedObject.isBold();

            isItalic = selectedObject.isItalic();

            isUnderline = selectedObject.isUnderline();

            isStrike = selectedObject.isStrikeThrough();

        }


        // Select the correct weight card
        updateWeightCardSelection(
                cardLight,
                cardRegular,
                cardMedium,
                cardSemiBold,
                cardBold
        );

        View.OnClickListener weightListener = v -> {

            int newWeight = (int) v.getTag();

            selectedWeight = newWeight;


            AddTextObject object = addTextView.getSelectedTextObject();

            if (object != null) {

                object.setFontWeight(newWeight);

                addTextView.updateTextObject(object);

            }

            // Update popup UI
            updateWeightCardSelection(
                    cardLight,
                    cardRegular,
                    cardMedium,
                    cardSemiBold,
                    cardBold
            );
        };


        cardLight.setOnClickListener(weightListener);
        cardRegular.setOnClickListener(weightListener);
        cardMedium.setOnClickListener(weightListener);
        cardSemiBold.setOnClickListener(weightListener);
        cardBold.setOnClickListener(weightListener);


        // ============================================================
        // FORMATTING CARDS
        // ============================================================

        MaterialCardView cardFmtBold = view.findViewById(R.id.cardFmtBold);

        MaterialCardView cardFmtItalic = view.findViewById(R.id.cardFmtItalic);

        MaterialCardView cardFmtUnderline = view.findViewById(R.id.cardFmtUnderline);

        MaterialCardView cardFmtStrike = view.findViewById(R.id.cardFmtStrike);


        TextView tvFmtBold = view.findViewById(R.id.tvFmtBold);

        TextView tvFmtItalic = view.findViewById(R.id.tvFmtItalic);

        TextView tvFmtUnderline = view.findViewById(R.id.tvFmtUnderline);

        TextView tvFmtStrike = view.findViewById(R.id.tvFmtStrike);


        // Initial selection
        setSelected(
                cardFmtBold,
                tvFmtBold,
                isBold
        );

        setSelected(
                cardFmtItalic,
                tvFmtItalic,
                isItalic
        );

        setSelected(
                cardFmtUnderline,
                tvFmtUnderline,
                isUnderline
        );

        setSelected(
                cardFmtStrike,
                tvFmtStrike,
                isStrike
        );


        cardFmtBold.setOnClickListener(v -> {

            isBold = !isBold;


            // Update default
            // isBold is now the default for new objects.


            // Update selected object
            AddTextObject object = addTextView.getSelectedTextObject();

            if (object != null) {

                object.setBold(isBold);

                addTextView.updateTextObject(object);
            }


            setSelected(
                    cardFmtBold,
                    tvFmtBold,
                    isBold
            );

            updateStylePreview();
        });

        cardFmtItalic.setOnClickListener(v -> {

            isItalic = !isItalic;


            AddTextObject object = addTextView.getSelectedTextObject();

            if (object != null) {

                object.setItalic(isItalic);

                addTextView.updateTextObject(object);
            }


            setSelected(
                    cardFmtItalic,
                    tvFmtItalic,
                    isItalic
            );

            updateStylePreview();

        });

        cardFmtUnderline.setOnClickListener(v -> {

            isUnderline = !isUnderline;


            AddTextObject object =
                    addTextView.getSelectedTextObject();

            if (object != null) {

                object.setUnderline(isUnderline);

                addTextView.updateTextObject(object);
            }


            setSelected(
                    cardFmtUnderline,
                    tvFmtUnderline,
                    isUnderline
            );

            updateStylePreview();
        });

        cardFmtStrike.setOnClickListener(v -> {

            isStrike = !isStrike;


            AddTextObject object =
                    addTextView.getSelectedTextObject();

            if (object != null) {

                object.setStrikeThrough(isStrike);

                addTextView.updateTextObject(object);
            }


            setSelected(
                    cardFmtStrike,
                    tvFmtStrike,
                    isStrike
            );

            updateStylePreview();
        });


        // Initial preview
        updateStylePreview();


        popup.showAsDropDown(anchor);
    }

    private void setSelected(MaterialCardView card, TextView tv, boolean selected) {

        if (selected) {

            card.setCardBackgroundColor(
                    ContextCompat.getColor(
                            requireContext(),
                            R.color.blue_color
                    )
            );

            card.setStrokeColor(
                    ContextCompat.getColor(
                            requireContext(),
                            R.color.blue_color
                    )
            );

            tv.setTextColor(Color.WHITE);

        } else {

            card.setCardBackgroundColor(Color.WHITE);

            card.setStrokeColor(
                    ContextCompat.getColor(
                            requireContext(),
                            R.color.grey
                    )
            );

            tv.setTextColor(Color.BLACK);
        }
    }

    private void updateWeightCardSelection(
            MaterialCardView cardLight,
            MaterialCardView cardRegular,
            MaterialCardView cardMedium,
            MaterialCardView cardSemiBold,
            MaterialCardView cardBold
    ) {

        // Remove selection from all cards

        setSelected(
                cardLight,
                (TextView) cardLight.getChildAt(0),
                false
        );

        setSelected(
                cardRegular,
                (TextView) cardRegular.getChildAt(0),
                false
        );

        setSelected(
                cardMedium,
                (TextView) cardMedium.getChildAt(0),
                false
        );

        setSelected(
                cardSemiBold,
                (TextView) cardSemiBold.getChildAt(0),
                false
        );

        setSelected(
                cardBold,
                (TextView) cardBold.getChildAt(0),
                false
        );


        MaterialCardView selectedCard;


        switch (selectedWeight) {

            case 300:
                selectedCard = cardLight;
                break;

            case 500:
                selectedCard = cardMedium;
                break;

            case 600:
                selectedCard = cardSemiBold;
                break;

            case 700:
                selectedCard = cardBold;
                break;

            case 400:
            default:
                selectedWeight = 400;
                selectedCard = cardRegular;
                break;
        }


        TextView selectedText =
                (TextView) selectedCard.getChildAt(0);


        setSelected(
                selectedCard,
                selectedText,
                true
        );
    }

    private void updateStylePreview() {

        int typefaceStyle = Typeface.NORMAL;


        if (isBold && isItalic) {

            typefaceStyle = Typeface.BOLD_ITALIC;

            stylePreview.setText("BI");
        }


        else if (isBold) {

            typefaceStyle = Typeface.BOLD;

            stylePreview.setText("B");
        }


        else if (isItalic) {

            typefaceStyle = Typeface.ITALIC;

            stylePreview.setText("I");
        }

        else {

            stylePreview.setText("D");

        }


        stylePreview.setTypeface(
                stylePreview.getTypeface(),
                typefaceStyle
        );




        int flags = stylePreview.getPaintFlags();

        flags &= ~Paint.UNDERLINE_TEXT_FLAG;
        flags &= ~Paint.STRIKE_THRU_TEXT_FLAG;


        if (isUnderline) {

            flags |= Paint.UNDERLINE_TEXT_FLAG;

        }

        if (isStrike) {

            flags |= Paint.STRIKE_THRU_TEXT_FLAG;

        }


        stylePreview.setPaintFlags(flags);


        // Same style for title

        styleTitle.setTypeface(
                styleTitle.getTypeface(),
                typefaceStyle
        );

        styleTitle.setPaintFlags(flags);

        if (isBold) {

            styleTitle.setText("Bold");

        }

        else if (isItalic) {

            styleTitle.setText("Italic");

        }

        else if (isUnderline) {

            styleTitle.setText("Underline");

        }

        else if (isStrike) {

            styleTitle.setText("Strikethrough");

        }

        else {

            styleTitle.setText("Default");
        }


    }

    private  void  updateAddTextObjHelpers(AddTextObject object){

        if(object == null) return;

        addTextOpacitySlider.setValue(object.getOpacity());
        addTextSizeSlider.setValue( (int)  object.getTextSize());
        colorSelector.setCardBackgroundColor(object.getTextColor());

        String fontFamily = object.getFontFamily() != null ?
                object.getFontFamily() :
                addTextFontSelected;

        fontAdapter.setFontItem(fontFamily);
        fontName.setText(fontFamily);

        selectedWeight = Objects.requireNonNullElse(
                object.getFontWeight(),
                400
        );

        addTextFontSelected = fontFamily;

        isBold = object.isBold();
        isItalic = object.isItalic();
        isUnderline = object.isUnderline();
        isStrike = object.isStrikeThrough();



        updateStylePreview();




    }

    private Typeface loadFont(String path) {

        return Typeface.createFromAsset(
                requireContext().getAssets(),
                "fonts/" + path
        );
    }


    private void  eraserLayout(){

        activeOverlay.push( eraserOverlay );

        eraserOverlay.setVisibility(View.VISIBLE);

        PageItem item = currentPageitem;

        getBitmapForExtractPage(item)
                .thenAccept(bitmap -> {

                    eraserView.setPageBitmap(bitmap);

        });

        resetEraser.setOnClickListener(v -> {

            eraserView.resetEraser();

        });

        eraserSizeSlider.addOnChangeListener((
                (slider, value, fromUser) ->{

                     value = (int) value;

                    DecimalFormat df = new DecimalFormat("0.##");

                    String result = df.format(value);

                     eraseSizetv.setText(result);

                     eraserView.setEraserSize(value);

        })

        );

        eraserColorPreview.setOnClickListener(v -> {

            Log.d("color preview", "colorPreview");

            colorPicker.setVisibility(View.VISIBLE);
            colorPicker.bringToFront();

            colorPicker.post(() -> {

                colorPicker.setColor(Color.WHITE);

            });

            activeOverlay.push(colorPicker);

        });


        cancelEraserBtn.setOnClickListener(v -> {

            if(activeOverlay.peek() !=  eraserOverlay) return;

            activeOverlay.pop();

            eraserOverlay.setVisibility(View.GONE);


        });


    }

}