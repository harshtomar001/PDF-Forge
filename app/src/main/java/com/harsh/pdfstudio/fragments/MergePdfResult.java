package com.harsh.pdfstudio.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.harsh.pdfstudio.controllers.PdfLoaderController;
import com.harsh.pdfstudio.databinding.FragmentMergeResultBinding;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.model.MergePdfOptions;
import com.harsh.pdfstudio.model.MergePdfViewModel;
import com.harsh.pdfstudio.ui.MergePdfActivity;
import com.harsh.pdfstudio.ui.PdfViewerActivity;
import com.harsh.pdfstudio.utils.Byte_MB;
import com.harsh.pdfstudio.utils.SaveFile;
import com.harsh.pdfstudio.utils.ShareFiles;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class MergePdfResult  extends Fragment {

    private FragmentMergeResultBinding binding;

    MergePdfViewModel  viewModel;

    private enum FileStorageState{
        DOWNLOADED,
        CACHE_STORAGE
    }

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    Handler handler = new Handler(Looper.getMainLooper());

    private FileStorageState  fileStorageState;

    private PdfLoaderController loaderController;



    public static MergePdfResult newInstance() {

        MergePdfResult fragment = new MergePdfResult();

        return fragment;
    }


    @Override
    public void onAttach(@NonNull Context context) {

        super.onAttach(context);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentMergeResultBinding
                .inflate(inflater,
                        container,
                        false
                );

        loaderController = new PdfLoaderController(binding.loaderRing);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity())
                .get(MergePdfViewModel.class);


        if (binding.toolbar != null) {

            ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar,
                    (v, insets) -> {

                        Insets systemBars =
                                insets.getInsets(WindowInsetsCompat.Type.statusBars());

                        v.setPadding(
                                systemBars.left,
                                (int) (systemBars.top / 2f),
                                v.getPaddingRight(),
                                 systemBars.bottom
                        );

                        return insets;
                    });
        }

        setupClickListener();

        if (viewModel != null) {

            viewModel.getMergeResult().observe(
                    getViewLifecycleOwner(),
                    fileItem -> {
                        if (fileItem != null) {

                            fileStorageState = FileStorageState.CACHE_STORAGE;
                            setupMetaData(fileItem);


                        }
                    });




        }





    }

    private void setupClickListener(){

        if (binding != null) {

            binding.btnBack.setOnClickListener(v -> {

                FileItem item = viewModel.getMergeResult().getValue();

                try {
                    if (item.getUri() == null) return;

                    if ( viewModel != null ) viewModel.clearAllData();

                    if (fileStorageState != FileStorageState.DOWNLOADED){

                        try {
                            int deleted = requireContext()
                                    .getContentResolver()
                                    .delete(item.getUri(),
                                            null,
                                            null
                                    );

                        } catch (SecurityException e) {

                        } catch (Exception e) {

                        }

                    }


                }
                catch (Exception e) {

                    e.printStackTrace();
                }

                finally {

                    requireActivity().finish();

                }




            });

            binding.btnOpenPdf.setOnClickListener(v -> {

                if (viewModel != null && viewModel.getMergeResult().getValue() != null){

                    Uri uri = viewModel.getMergeResult().getValue().getUri();

                    if (uri != null){

                        Intent intent = new Intent(requireContext() , PdfViewerActivity.class);
                        intent.putExtra("pdf_uri" , uri);
                        intent.putExtra("mode" ,  "preview");
                        startActivity(intent);

                    }

                }

            });

            binding.btnMergeAnother.setOnClickListener(v ->{

                if (viewModel != null){

                    viewModel.clearAllData();

                    MergePdfActivity activity = (MergePdfActivity) requireActivity();

                    activity.startNewMerge();

                }

            });

            binding.btnShare.setOnClickListener(v -> {

                if (viewModel != null && viewModel.getMergeResult().getValue() != null){

                    Uri pdfUri = viewModel.getMergeResult().getValue().getUri();

                    Intent intent = ShareFiles.sharePDF();
                    intent.putExtra(Intent.EXTRA_STREAM, pdfUri);
                    startActivity(Intent.createChooser(intent, "Share PDF"));

                }

            });

            binding.btnSave.setOnClickListener(v -> {

                if (viewModel != null && viewModel.getMergeResult().getValue() != null){

                    FileItem item = viewModel.getMergeResult().getValue();

                    showLoader();

                    executorService.execute(() -> {

                        SaveFile.savePDF(requireContext() ,
                                item.getUri() ,
                                item.getName() ,
                                item.getPageCount() ,
                                Byte_MB.convertByte(item.getFileSize()),
                                true);



                        handler.post(() -> {

                           fileStorageState =  FileStorageState.DOWNLOADED;
                           hideLoader();

                        });


                    });




                }

            });

        }


    }

    private void  showLoader(){

        if (binding != null){


            binding.loaderLayout.setFocusable(true);
            binding.loaderLayout.bringToFront();
            binding.loaderLayout.requestFocus();
            binding.loaderLayout.setClickable(true);
            binding.loaderLayout.setVisibility(View.VISIBLE);

            if(loaderController != null)

                loaderController.showLoader();

        }


    }

    private void hideLoader(){

        if (binding != null){

            binding.loaderLayout.setVisibility(View.GONE);

            if(loaderController != null) loaderController.hideLoader();

        }


    }

    private void setupMetaData(FileItem fileItem){

        if (binding != null && fileItem != null) {

            MergePdfOptions mergePdfOptions = viewModel.getMergePdfOptions().getValue();

            binding.tvPdfPages.setText(fileItem.getPageCount()+" Pages");
            binding.tvPdfName.setText(fileItem.getName());
            binding.tvPdfSize.setText(Byte_MB.convertByte(fileItem.getFileSize()));

            List<?> pdfs = viewModel.getPdfs().getValue();

            if (pdfs != null) {

                binding.tvFilesMerged.setText(pdfs.size() + "");

            }
            
            binding.tvTotalPages.setText(fileItem.getPageCount()+"");

            if (mergePdfOptions != null) {

                if (mergePdfOptions.getPageSizeMode() != null) {

                    Log.d("page mode", mergePdfOptions.getPageSizeMode().toString());

                    binding.tvPageSize.setText(mergePdfOptions.getPageSizeMode().toString());

                }

                if (mergePdfOptions.getMargin() != null) {

                    binding.tvMargins.setText(mergePdfOptions.getMargin().toString());

                }
            }

        }

    }


}
