package com.harsh.pdfstudio.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.adapter.Split_PDF_Adap_2;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.model.SharingViewModel_sp_pdf;
import com.harsh.pdfstudio.ui.PdfViewerActivity;
import com.harsh.pdfstudio.utils.Byte_MB;
import com.harsh.pdfstudio.utils.File_Size;
import com.harsh.pdfstudio.utils.GetFileName;
import com.harsh.pdfstudio.utils.SaveFile;
import com.vertlix.utils.file.ShareFiles;
import com.vertlix.utils.pdf.Page_Count;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SplitPdf_Out_Frag extends Fragment {

    public SplitPdf_Out_Frag(){
        super(R.layout.split_pdf_out_frag);
    }

    RecyclerView recyclerView;
    Split_PDF_Adap_2 adapter;
    TextView generatedFiles;
    TextView selectAllText;
    CheckBox selectAllBtn;
    LinearLayout mul_pdfs_btns_layout;
    MaterialButton download_all_mul_pdfs;
    MaterialButton share_all_mul_pdfs;
    MaterialButton downloadPDF;
    MaterialButton sharePDF;
    MaterialCardView singleton_pdf_card;
    ConstraintLayout  mul_pdf_card;
    TextView totalGeneratedFiles;
    TextView singleton_pdfName;
    TextView singleton_parentPDFName;
    TextView singleton_pages_range;
    TextView singleton_total_pages;
    TextView singleton_pdfSize;
    ArrayList<FileItem> list_of_multiple_pdfs;  //
    HashSet<Uri> finalUri=new HashSet<>();
    boolean areMultiplePDfs;
    Uri singleton_Pdf_Uri;
    String pdfName;
    int pdfPage;
    String pdfSize;
    ExecutorService executorService= Executors.newSingleThreadExecutor();
    Handler handler=new Handler(Looper.getMainLooper());



    @Override
    public void onViewCreated(@NonNull View view, Bundle savedBundle){
        super.onViewCreated(view, savedBundle);


            totalGeneratedFiles = view.findViewById(R.id.generatedChip);

        // for single split pdfs

            downloadPDF = view.findViewById(R.id.downloadPdfBtn);
            sharePDF = view.findViewById(R.id.sharePdfBtn);
            singleton_pdf_card = view.findViewById(R.id.generatedPdfCard);
            singleton_pdfName = view.findViewById(R.id.generatedPdfName);
            singleton_pages_range = view.findViewById(R.id.pagesValue);
            singleton_total_pages = view.findViewById(R.id.totalPagesValue);
            singleton_parentPDFName = view.findViewById(R.id.sourcePdfName);
            singleton_pdfSize = view.findViewById(R.id.fileSizeValue);








        // for multiple split pdfs
            mul_pdf_card = view.findViewById(R.id.multiplesSplitPdfCard);
            recyclerView = view.findViewById(R.id.recyclerViewSplitPDF);
            generatedFiles = view.findViewById(R.id.totalGeneratedFiles);
            selectAllText = view.findViewById(R.id.selectAll_Text);
            selectAllBtn = view.findViewById(R.id.selectAll_checkBox);
            mul_pdfs_btns_layout = view.findViewById(R.id.layout_btns_mul_split_pdfs);
            download_all_mul_pdfs = view.findViewById(R.id.downloadPdfBtn_mul_split);
            share_all_mul_pdfs = view.findViewById(R.id.sharePdfBtn_mul_split);



        Bundle bundle=getArguments();

        if (bundle!=null){

            areMultiplePDfs=bundle.getBoolean("multiple_Pdfs");

            list_of_multiple_pdfs=new ArrayList<>();

            adapter=new Split_PDF_Adap_2(list_of_multiple_pdfs);



            if (areMultiplePDfs){

                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

                loadData_recyclerView();

                SharingViewModel_sp_pdf viewModel = new ViewModelProvider(requireActivity()).get(SharingViewModel_sp_pdf.class);

                viewModel.getFileList().observe(getViewLifecycleOwner(), files -> {
                    list_of_multiple_pdfs=files;
                    fillUriset();
                    adapter.updateList(files);

                });



            }

            else {

                singleton_Pdf_Uri=bundle.getParcelable("Singleton_pdfUri");

                int  from_page = Integer.parseInt(bundle.getString("fromPage"));

                int  to_page = Integer.parseInt(bundle.getString("toPage"));

                String original_pdf_name=bundle.getString("originalPdfName");

                load_data_singleton_pdf(singleton_Pdf_Uri,from_page,to_page,original_pdf_name);




            }

        }


        // download and share btn for singleton pdf

        downloadPDF.setOnClickListener(v -> {


            executorService.execute(() -> {

                if (singleton_Pdf_Uri==null) return;

                SaveFile.savePDF(requireContext(),singleton_Pdf_Uri,pdfName,pdfPage,pdfSize);

                handler.post(() -> {

                    Toast.makeText(
                            requireContext(),
                                    "File saves to Downloads",
                                    Toast.LENGTH_LONG
                            )
                            .show();
                });

            });


        });

        sharePDF.setOnClickListener(v -> {

            Intent intent= ShareFiles.sharePDF();
            intent.putExtra(Intent.EXTRA_STREAM, singleton_Pdf_Uri);

            startActivity(Intent.createChooser(intent, "Share PDF"));

        });

        // for multiple pdfs

        download_all_mul_pdfs.setOnClickListener(v -> {


            executorService.execute(() -> {
                for (FileItem item:list_of_multiple_pdfs){



                    SaveFile.savePDF(requireContext(),
                            item.getUri(),
                            item.getName(),
                            item.getPageCount(),
                            Byte_MB.convertByte(item.getFileSize()),
                            false
                    );



                }

                handler.post(() -> {
                    Toast.makeText(requireContext(),"PDFs save to Downloads",Toast.LENGTH_LONG)
                            .show();
                });
            });

        });

        share_all_mul_pdfs.setOnClickListener(v -> {

            ArrayList<Uri> uris = new ArrayList<>(finalUri);


            Intent intent= com.harsh.pdfstudio.utils.ShareFiles.shareMultiplesPDFs();

            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,uris);

            startActivity(Intent.createChooser(intent,"Share PDFs"));

        });

        load_ui_for_multiple_pdfs_for_single_pdfs(areMultiplePDfs);


    }

    private void load_ui_for_multiple_pdfs_for_single_pdfs(boolean areMultiplePDfs){


        mul_pdf_card.setVisibility( areMultiplePDfs ? View.VISIBLE : View.GONE);
        mul_pdfs_btns_layout.setVisibility( areMultiplePDfs ? View.VISIBLE : View.GONE);

        singleton_pdf_card.setVisibility( areMultiplePDfs ? View.GONE : View.VISIBLE);
        downloadPDF.setVisibility( areMultiplePDfs ? View.GONE : View.VISIBLE );
        sharePDF.setVisibility( areMultiplePDfs ? View.GONE : View.VISIBLE);


    }

    private  void loadData_recyclerView(){


        recyclerView.post(() -> {

            View firstItem = recyclerView.getChildAt(0);

            if (firstItem != null) {

                int itemHeight = firstItem.getHeight()+80;
                int itemCount = adapter.getItemCount();

                int totalHeight = itemHeight * itemCount;

                ViewGroup.LayoutParams params = recyclerView.getLayoutParams();

                params.height = Math.min(totalHeight, dpToPx(400));


                recyclerView.setLayoutParams(params);
            }
        }); // dynamic height of the recyclerView


        adapter.onItemClicked(new Split_PDF_Adap_2.itemClicked() {
            @Override
            public void pdfPreviewClick(Uri uri) {
                Intent intent = new Intent(requireContext(), PdfViewerActivity.class);
                intent.putExtra("pdf_uri",uri);
                intent.putExtra("mode","preview");
                startActivity(intent);
            }

            @Override
            public void checkBoxClick(Uri uri,boolean isChecked) {

                if (isChecked) finalUri.add(uri);

                else finalUri.remove(uri);

            }

            @Override
            public void moreOptionClick(Uri uri,int position,View view) {



                PopupMenu popupMenu=new PopupMenu(requireContext(),view, Gravity.END,0,R.style.PopupMenuStyle);
                popupMenu.getMenuInflater().inflate(R.menu.split_menu,popupMenu.getMenu());

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

                popupMenu.setOnMenuItemClickListener(item -> {

                    int id=item.getItemId();

                    if (id==R.id.downloadPDF){

                        executorService.execute(() -> {

                            SaveFile.savePDF(requireContext(),
                                    uri,
                                    com.vertlix.utils.file.GetFileName.getFileName(requireContext(),uri),
                                    Page_Count.getPdfPageCount(requireContext(),uri),
                                    Byte_MB.convertByte(com.vertlix.utils.file.File_Size.getFileSize(requireContext(),uri))
                            );



                        });



                    }
                    else if (id==R.id.sharePDF) {

                        Intent intent=ShareFiles.sharePDF();
                        intent.putExtra(Intent.EXTRA_STREAM,uri);
                        startActivity(Intent.createChooser(intent, "Share PDF"));
                        return true;


                    }
                    else if (id==R.id.deletePDF) {

                        adapter.remove_singleton_item(position);
                        if (finalUri.contains(uri)) finalUri.remove(uri);
                        return  true;

                    }

                    return false;


                });


                popupMenu.show();

            }
        });

        recyclerView.setAdapter(adapter);

    }

    private  void load_data_singleton_pdf(Uri pdfUri,int from_page,int to_page,String original_PDF_Name){

        singleton_pages_range.setText(String.format("%d – %d",from_page,to_page));

        singleton_parentPDFName.setText("Split from: "+original_PDF_Name);


        pdfPage=Page_Count.getPdfPageCount(requireContext(),pdfUri);

        singleton_total_pages.setText(pdfPage+" Pages");


        pdfSize = Byte_MB.convertByte(File_Size.getFileSize(requireContext(),pdfUri));

        singleton_pdfSize.setText(pdfSize);

        pdfName=GetFileName.getFileName(requireContext(),pdfUri);

        singleton_pdfName.setText(pdfName);



    }

    private int dpToPx(int dp) {

        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private  void fillUriset(){

        for (FileItem item:list_of_multiple_pdfs){
            finalUri.add(item.getUri());
        }
    }

}