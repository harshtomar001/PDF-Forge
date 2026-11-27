package com.harsh.pdfstudio.fragments;

import static android.app.Activity.RESULT_OK;
import static android.os.Looper.getMainLooper;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.adapter.EditPDF_adap;
import com.harsh.pdfstudio.model.EditPdfViewModel;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.model.PageItem;
import com.harsh.pdfstudio.model.SharingPages_VM_edt_pdf;
import com.harsh.pdfstudio.model.Sharing_data_adap_edt_pdf;
import com.harsh.pdfstudio.model.Store_pdf_VM_edt_pdf_inp_frag;
import com.harsh.pdfstudio.operations.EditOperation;
import com.harsh.pdfstudio.utils.Byte_MB;
import com.harsh.pdfstudio.utils.Drag_n_Drop;
import com.harsh.pdfstudio.utils.Image_picker;
import com.vertlix.utils.file.File_Size;
import com.vertlix.utils.file.GetFileName;
import com.vertlix.utils.pdf.PDF_Picker;
import com.vertlix.utils.pdf.Page_Count;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Edit_pdf_Inp_frag extends Fragment {

    public Edit_pdf_Inp_frag(){
        super(R.layout.edit_pdf_inp_frag);
    }

    boolean returnFromEdit_Pdf_Edit_Page=false;

    ActivityResultLauncher<Intent> pdfLauncher;

    ActivityResultLauncher<Intent> pickImagesLauncher;

    ConstraintLayout no_pdf_layout;

    View pdf_preview_layout;

    LinearLayout  btns_related_recyclerview;

    LinearLayout tipLayout;

    MaterialButton saveChangesBtn;

    RecyclerView recyclerView;

    EditPDF_adap adapter;

    MaterialButton uploadPDFBtn;

    TextView pdfName ,clearSelectedPages;

    TextView pdfMetaData;

    FileItem[] pdfList=new FileItem[1];

    LinearLayout pdfTools;

    LinearLayout totalPageSelected;

    MaterialCardView parentCard;

    MaterialButton addImages;

    TextView total_pages_text;

    TextView page_no_text,showPreviewBtn;

    LinearLayout editTextTool;

    LinearLayout replacePageTool,deletePages,moreOption;

    LinearLayout extractPageTool;

    LinearLayout watermarkTool;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    Handler handler = new Handler(getMainLooper());

    TreeSet<Integer> selectedPagesForUi =new TreeSet<>();

    PdfRenderer pdfRenderer;

    EditPdfViewModel pdfOperationsModel;

    Store_pdf_VM_edt_pdf_inp_frag storePdf_edit_pdf;

    EditPDF_adap  storedAdapter;

    Set<Integer> modifiedPagesSet=new HashSet<>();

    HashMap<Integer, List<EditOperation>> modifiedPagesOperations;

    Stack<EditOperation> modifiedPagesStack;

    Sharing_data_adap_edt_pdf shareData_adap;

    List<PageItem> pdfPages=new ArrayList<>();

    SharingPages_VM_edt_pdf sharingData_viewModel;

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedBundle){

        super.onViewCreated(view, savedBundle);

        initializePage(view);

        showPreviewBtn.setText(R.string.preview);
        showPreviewBtn.setPadding(55,10,55,10);
        showPreviewBtn.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.color_primary));

        showPreviewBtn(false);

        sharingData_viewModel =new ViewModelProvider(requireActivity())
                .get(SharingPages_VM_edt_pdf.class);


        uploadPDFBtn.setOnClickListener(v ->{

            Intent intent=PDF_Picker.pick_pdf();
            pdfLauncher.launch(intent);

        });


        editTextTool.setOnClickListener(v ->{

            sharingData_viewModel.getSelectedIDs().observe(getViewLifecycleOwner(), ids ->{
                if(ids.isEmpty()) return;
            });

            Edit_Pdf_Edit_Page editPdfEditPageFrag=new Edit_Pdf_Edit_Page(pdfRenderer);


            returnFromEdit_Pdf_Edit_Page=true;

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.Fragment_container , editPdfEditPageFrag)
                    .addToBackStack(null)
                    .commit();


        });


        clearSelectedPages.setOnClickListener(v ->{

            modifyUiForPageSelected(false,false);
            selectedPagesForUi.clear();
            adapter.removeSelectedPages();
            sharingData_viewModel.clearSelectedPageItems();


        });


        deletePages.setOnClickListener(v ->{

            selectedPagesForUi.clear();



        });

        shareData_adap=new ViewModelProvider(requireActivity()).get(Sharing_data_adap_edt_pdf.class);

        pdfOperationsModel = new ViewModelProvider(requireActivity()).get(EditPdfViewModel.class);


         //  to store the selected pdf file
        storePdf_edit_pdf=new ViewModelProvider(requireActivity()).get(Store_pdf_VM_edt_pdf_inp_frag.class);

        storedAdapter=storePdf_edit_pdf.adapter;


        if(storePdf_edit_pdf.hasPdfLoaded){


             recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 3));
             recyclerView.setAdapter(storedAdapter);
             loadUi(true);

         }

        else loadUi(false);


        if(returnFromEdit_Pdf_Edit_Page){

             boolean hasWork=pdfOperationsModel.isAnyJobDone();

             if(hasWork) handleModifiedPage();



         }


        handle_uploads();


        addImages.setOnClickListener(v -> {
            Intent intent= Image_picker.openImageFiles();
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
            pickImagesLauncher.launch(intent);


        });




    }

    private void initializePage(View view){

        no_pdf_layout = view.findViewById(R.id.initial_no_pdf_sel_upl_pdf);
        pdf_preview_layout = view.findViewById(R.id.preview_box);
        tipLayout=view.findViewById(R.id.tip_layout);
        btns_related_recyclerview=view.findViewById(R.id.btns__);
        saveChangesBtn=view.findViewById(R.id.saveChangesBtn);
        recyclerView=view.findViewById(R.id.recyclerView_edit_pdf);
        uploadPDFBtn=view.findViewById(R.id.uploadPdfBtn);
        pdfName=view.findViewById(R.id.pdf_file_name_edit_pdf);
        pdfMetaData=view.findViewById(R.id.metadata_edit_pdf);
        pdfTools=view.findViewById(R.id.toolsRow);
        addImages=view.findViewById(R.id.addImageBtn);
        totalPageSelected=view.findViewById(R.id.total_page_layout);
        parentCard=view.findViewById(R.id.parent_of_save_pdf_tools_layout);
        total_pages_text=view.findViewById(R.id.total_pages_text);
        page_no_text=view.findViewById(R.id.page_no_textView);
        editTextTool=view.findViewById(R.id.edit_text);
//        replacePageTool=view.findViewById(R.id.replace_page);
        extractPageTool=view.findViewById(R.id.extract_pages);
        watermarkTool=view.findViewById(R.id.waterMark);
        clearSelectedPages=view.findViewById(R.id.clearSelectedPages);
        deletePages=view.findViewById(R.id.delete_pages);
        moreOption=view.findViewById(R.id.moreOption);
        showPreviewBtn=view.findViewById(R.id.preview_or_save_edit_pdf);

    }

    private void loadUi(boolean isPdf_uploaded){

        no_pdf_layout.setVisibility(isPdf_uploaded ? View.GONE : View.VISIBLE);

        pdf_preview_layout.setVisibility(isPdf_uploaded ? View.VISIBLE : View.GONE);

        tipLayout.setVisibility(isPdf_uploaded ? View.VISIBLE : View.GONE);

        btns_related_recyclerview.setVisibility(isPdf_uploaded ? View.VISIBLE : View.GONE);

        saveChangesBtn.setVisibility(isPdf_uploaded ? View.VISIBLE : View.GONE);

        pdfTools.setVisibility(isPdf_uploaded ? View.VISIBLE : View.GONE);

        totalPageSelected.setVisibility(isPdf_uploaded ? View.VISIBLE : View.GONE);

        recyclerView.setVisibility(isPdf_uploaded ? View.VISIBLE : View.GONE);




    }

    private void handle_uploads() {

        pdfLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),

                result -> {

                    if (result.getResultCode()!=RESULT_OK) return;

                    Intent data=result.getData();

                    if (result.getData()!=null){

                        executor.execute(() ->{

                            Uri uri=data.getData();

                            String fileName= GetFileName.getFileName(requireContext(),uri);

                            int pageCount= Page_Count.getPdfPageCount(requireContext(),uri);

                            long fileSize= File_Size.getFileSize(requireContext(),uri);


                            pdfList[0]=new FileItem(fileName,uri,fileSize,pageCount);

                            handler.post(() -> {
                                loadData();
                                modifyUiForPageSelected(false,false);
                            });

                        } );

                    }

                    else{
                        Toast.makeText(requireContext(),"Something Wrong",Toast.LENGTH_SHORT).show();
                    }




                });

        pickImagesLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == RESULT_OK && result.getData() != null) {

                                if (result.getResultCode() != Activity.RESULT_OK) return;
                                Intent data = result.getData();
                                if (data == null) return;

                                // Multiple images selected
                                if (data.getClipData() != null) {

                                    ClipData clipData = data.getClipData();

                                    for (int i = 0; i < clipData.getItemCount(); i++) {

                                        Uri uri = clipData.getItemAt(i).getUri();

                                        pdfPages.add(new PageItem(uri,pdfPages.size()));
                                        adapter.setFileList(pdfPages);

                                    }

                                }
                                // Single image selected
                                else if (data.getData() != null) {

                                    Uri uri = data.getData();

                                    pdfPages.add(new PageItem(uri,pdfPages.size()));
                                    adapter.setFileList(pdfPages);
                                }

                            }
                        }
                );
    }

    private void loadData(){

        FileItem item=pdfList[0];

        pdfName.setText(item.getName());
        pdfMetaData.setText(String.format("%d Pages • %s",item.getPageCount(),
                Byte_MB.convertByte(item.getFileSize())));

        loadUi(true);
        settingUpAdapter();

    }

    private void settingUpAdapter(){

        Uri uri=pdfList[0].getUri();

        try{

           ParcelFileDescriptor pfd=requireContext().getContentResolver().openFileDescriptor(uri,"r");
           pdfRenderer=new PdfRenderer(pfd);

           int pages=pdfList[0].getPageCount();

           for(int i=0;i<pages;i++){
               pdfPages.add(new PageItem(i));
           }

           shareData_adap.setFileList(pdfPages);

           adapter=new EditPDF_adap(pdfRenderer,requireContext());
            setItemTouchHelper();

           storePdf_edit_pdf.adapter=adapter;
           storePdf_edit_pdf.currentPdf=pdfList[0];
           storePdf_edit_pdf.hasPdfLoaded=true;
           storePdf_edit_pdf.pdfSize=pdfList[0].getPageCount();
           storePdf_edit_pdf.pdfRenderer=pdfRenderer;

            adapter.setFileList(pdfPages);


            adapter.onPDFPage(new EditPDF_adap.onPDFPageClicks() {
                                  @Override
                                  public void moreOptionClick(View anchorView) {

                                      PopupMenu popupMenu=new PopupMenu(requireContext(),anchorView, Gravity.END,0,R.style.PopupMenuStyle);

                                      popupMenu.getMenu().add(0,1,0,"Edit Page");
                                      popupMenu.getMenu().add(0,2,1,"Crop Page");
                                      popupMenu.getMenu().add(0,3,2,"Rotate Page");
                                      popupMenu.getMenu().add(0,4,3,"Delete Page");

                                      Menu menu = popupMenu.getMenu();
                                      for (int i = 0; i < menu.size(); i++) {
                                          MenuItem menuItem = menu.getItem(i);

                                          SpannableString spanString = new SpannableString(menuItem.getTitle());
                                          spanString.setSpan(
                                                  new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.darkGray)),
                                                  0,
                                                  spanString.length(),
                                                  0
                                          );

                                          spanString.setSpan(
                                                  new AbsoluteSizeSpan(15, true), // 15sp
                                                  0,
                                                  spanString.length(),
                                                  0
                                          );

                                          menuItem.setTitle(spanString);
                                      }



                                      popupMenu.show();


                                  }

                                  @Override
                                  public void pageClick(int operation, int pageNum,PageItem item) {

                                      if(operation==1){
                                          selectedPagesForUi.add(pageNum);
                                          sharingData_viewModel.addSelectedPageItem(item);



                                      }

                                      if(operation==-1){
                                          selectedPagesForUi.remove(Integer.valueOf(pageNum));
                                          sharingData_viewModel.removeSelectedPageItem(item);

                                      }


                                      StringJoiner joiner = new StringJoiner(", ");

                                      for (Integer num : selectedPagesForUi) {
                                          joiner.add(String.valueOf(num));
                                      }

                                      String pages = joiner.toString();

                                      page_no_text.setText(pages);

                                      total_pages_text.setText(selectedPagesForUi.size()+" Pages Selected");

                                      modifyUiForPageSelected(selectedPagesForUi.size()>0,selectedPagesForUi.size()>1);

                                  }

                                  @Override
                                  public void onDoubleTapPage(){}
                              }
            );

            recyclerView.setLayoutManager(new GridLayoutManager(requireContext(),3));

            recyclerView.setAdapter(adapter);



            shareData_adap.getFileList().observe(getViewLifecycleOwner(), list -> {

                adapter.setFileList(list);

            });


        }
        catch (Exception e){
            throw new RuntimeException();
        }
    }

    private void modifyUiForPageSelected(boolean hasSelection, boolean hasMultipleSelection){

        addImages.setVisibility( hasSelection ? View.GONE : View.VISIBLE );
        totalPageSelected.setVisibility( hasSelection ? View.VISIBLE : View.GONE );
        pdfTools.setVisibility( hasSelection ? View.VISIBLE : View.GONE);

        parentCard.setCardBackgroundColor(
                hasSelection
                        ? ContextCompat.getColor(requireContext(), R.color.white)
                        : ContextCompat.getColor(requireContext(), R.color.default_2)
        );

        extractPageTool.setVisibility( hasMultipleSelection ? View.VISIBLE : View.GONE);
        watermarkTool.setVisibility( hasMultipleSelection ? View.GONE : View.VISIBLE );


    }

    private void setItemTouchHelper(){

        Drag_n_Drop drag_n_drop=new Drag_n_Drop(adapter);

        ItemTouchHelper touchHelper = new ItemTouchHelper(drag_n_drop.callback);
        touchHelper.attachToRecyclerView(recyclerView);

    }

    private void showPreviewBtn(boolean show){
        showPreviewBtn.setVisibility(show ? View.VISIBLE:View.GONE);
    }

    private void handleModifiedPage(){

        modifiedPagesStack=pdfOperationsModel.getStackOfModifiedPage();

        for(EditOperation op:modifiedPagesStack){

            modifiedPagesSet.add(op.getPageNumber());

        }

        modifiedPagesOperations=pdfOperationsModel.getPdfOperationsList();

        storedAdapter.informAdapterAboutModifiedPages(modifiedPagesStack,modifiedPagesOperations);





    }


}