package com.harsh.pdfstudio.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.harsh.pdfstudio.adapter.MergePdfAdapter.PdfViewHolder;
import com.harsh.pdfstudio.adapter.MergePdfAdapter;
import com.harsh.pdfstudio.controllers.PdfLoaderController;
import com.harsh.pdfstudio.model.MergePdfItem;
import com.harsh.pdfstudio.model.MergePdfOptions;
import com.harsh.pdfstudio.model.MergePdfViewModel;
import com.harsh.pdfstudio.utils.MergePdfTouchHelperCallback;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.databinding.BottomSheetFileOrderBinding;
import com.harsh.pdfstudio.databinding.BottomSheetPageMarginBinding;
import com.harsh.pdfstudio.databinding.BottomSheetPageSizeBinding;
import com.harsh.pdfstudio.databinding.DialogMergeHelpBinding;
import com.harsh.pdfstudio.databinding.FragmentMergePdfInputBinding;

import com.harsh.pdfstudio.ui.MergePdfActivity.PageSizeMode;
import com.harsh.pdfstudio.ui.MergePdfActivity.PageMargin;

import java.util.List;


public class MergePdfInputFragment extends Fragment {


    public interface UserClicks {
        void onAddPdfClick();

        void onBackClick();

    }

    private FragmentMergePdfInputBinding binding;

    private UserClicks clicksListener;

    private MergePdfAdapter adapter;

    private ItemTouchHelper itemTouchHelper;

    PdfLoaderController pdfLoaderController;

    private PageSizeMode selectedPageSize = PageSizeMode.ORIGINAL;

    private PageMargin selectedPageMargin = PageMargin.NONE;

    MergePdfViewModel  viewModel ;

    @Override
    public void onAttach(@NonNull Context context) {

        super.onAttach(context);

        if (context instanceof UserClicks) {

            clicksListener = (UserClicks) context;

        }
        else {

            throw new IllegalStateException(
                    context + " must implement MergePdfInputFragment.UserClicks");

        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                              @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {

        binding = FragmentMergePdfInputBinding
                .inflate(inflater,
                        container,
                        false
                );

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        viewModel =   new ViewModelProvider(requireActivity())
                .get(MergePdfViewModel.class);


        if (binding.toolbarContainer!= null) {

            ViewCompat.setOnApplyWindowInsetsListener(binding.toolbarContainer,

                    (v, insets) -> {

                        Insets systemBars =
                                insets.getInsets(WindowInsetsCompat.Type.statusBars());

                        v.setPadding(
                                systemBars.left,
                                (int) (systemBars.top / 2f ) ,
                                v.getPaddingRight(),
                                systemBars.bottom
                        );

                        return insets;
                    });
        }

        initializeViews();
        initializeRecyclerView();
        initializeClickListeners();

        if (binding != null){

            binding.tvPageSizeValue.setText(getPageSizeText(selectedPageSize));
            binding.tvPageMarginValue.setText(getPageMarginText(selectedPageMargin));

        }

        if (viewModel != null) {


            viewModel.getPdfs().observe(
                    getViewLifecycleOwner(),
                    files -> {


                adapter.submitList(files);

                updateUi(files);


            });

            viewModel.getState().observe(
                    getViewLifecycleOwner(),
                    state -> {

                        switch (state) {

                            case IDLE:
                                hideLoader();
                                break;

                            case MERGING:
                                showLoader();
                                break;

                            case SUCCESS:
                                hideLoader();
                                break;

                            case ERROR:
                                hideLoader();
                                break;
                        }
                    }
            );



        }


    }


    private void initializeViews() {

        pdfLoaderController =  new PdfLoaderController(R.animator.loader_page_stack_bounce ,
                binding.loaderLayout.page1,
                binding.loaderLayout.page2,
                binding.loaderLayout.page3
                );

        binding.loaderLayout.loaderText.setText("Merging PDFs...");


    }

    private void initializeRecyclerView() {

        adapter = new MergePdfAdapter(new MergePdfAdapter.Listener() {
            @Override
            public void onItemDeleted(MergePdfItem item) {

                if (viewModel == null) return;

                viewModel.removePdf(item);

            }

            @Override
            public void onItemMoved(int fromPosition, int toPosition) {

                viewModel.swapPDFS(fromPosition ,  toPosition);


            }

            @Override
            public void onDragHandleTouched(PdfViewHolder viewHolder) {


                if (itemTouchHelper != null) {

                    itemTouchHelper.startDrag(viewHolder);

                }

            }

        });

        binding.selectedPdfRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        binding.selectedPdfRecyclerView.setAdapter(adapter);

        itemTouchHelper = new ItemTouchHelper(new MergePdfTouchHelperCallback(adapter));

        itemTouchHelper.attachToRecyclerView(binding.selectedPdfRecyclerView);
    }


    private void initializeClickListeners() {


        binding.ivBack.setOnClickListener(v -> clicksListener.onBackClick());

        binding.ivHelp.setOnClickListener(v -> {


            showHelpDialog();

        });

        binding.btnBrowseFiles.setOnClickListener(v -> clicksListener.onAddPdfClick());

        binding.btnClearAll.setOnClickListener(v -> {

            if (viewModel ==  null) return;

            viewModel.clearPdfs();


        });

        binding.rowFileOrder.setOnClickListener(v -> showFileOrderBottomSheet());

        binding.rowPageSize.setOnClickListener(v -> showPageSizeBottomSheet());

        binding.rowPageMargins.setOnClickListener(v -> showPageMarginBottomSheet());

        binding.btnMergePdfs.setOnClickListener(v -> {

            if (viewModel ==  null) return;

            viewModel.merge(requireContext() , viewModel.getMergePdfOptions().getValue());

        });


    }


    private void showPageSizeBottomSheet() {

        BottomSheetPageSizeBinding sheetBinding =
                BottomSheetPageSizeBinding.inflate(
                        LayoutInflater.from(requireContext())
                );

        BottomSheetDialog dialog =
                new BottomSheetDialog(requireContext());

        dialog.setContentView(sheetBinding.getRoot());


        View[] rows = {
                sheetBinding.rowOriginal,

                sheetBinding.rowFitSmallestWidth,
                sheetBinding.rowFitSmallestHeight,
                sheetBinding.rowFitSmallestBoth,

                sheetBinding.rowFitLargestWidth,
                sheetBinding.rowFitLargestHeight,
                sheetBinding.rowFitLargestBoth,

                sheetBinding.rowA4,
                sheetBinding.rowA3,
                sheetBinding.rowLetter,
                sheetBinding.rowLegal
        };


        RadioButton[] radios = {
                sheetBinding.radioOriginal,

                sheetBinding.radioFitSmallestWidth,
                sheetBinding.radioFitSmallestHeight,
                sheetBinding.radioFitSmallestBoth,

                sheetBinding.radioFitLargestWidth,
                sheetBinding.radioFitLargestHeight,
                sheetBinding.radioFitLargestBoth,

                sheetBinding.radioA4,
                sheetBinding.radioA3,
                sheetBinding.radioLetter,
                sheetBinding.radioLegal
        };


        PageSizeMode[] options = {
                PageSizeMode.ORIGINAL,

                PageSizeMode.FIT_SMALLEST_WIDTH,
                PageSizeMode.FIT_SMALLEST_HEIGHT,
                PageSizeMode.FIT_SMALLEST_BOTH,

                PageSizeMode.FIT_LARGEST_WIDTH,
                PageSizeMode.FIT_LARGEST_HEIGHT,
                PageSizeMode.FIT_LARGEST_BOTH,

                PageSizeMode.A4,
                PageSizeMode.A3,
                PageSizeMode.LEGAL,
                PageSizeMode.LETTER
        };


        // Get current state from ViewModel
        MergePdfOptions currentOptions =
                viewModel.getMergePdfOptions().getValue();


        PageSizeMode currentPageSize = PageSizeMode.ORIGINAL;


        if (currentOptions != null
                && currentOptions.getPageSizeMode() != null) {

            currentPageSize =
                    currentOptions.getPageSizeMode();
        }


        // Mark currently selected option
        markSelectedRow(
                rows,
                radios,
                indexOf(options, currentPageSize)
        );


        // Handle selection
        for (int i = 0; i < rows.length; i++) {

            final int index = i;

            rows[i].setOnClickListener(v -> {

                PageSizeMode selectedPageSize = options[index];


                // ViewModel owns the state
                viewModel.setPageSizeMode(selectedPageSize);


                // Update BottomSheet UI
                markSelectedRow(
                        rows,
                        radios,
                        index
                );


                // Update current Fragment UI
                binding.tvPageSizeValue.setText(getPageSizeText(selectedPageSize));


                dialog.dismiss();
            });
        }


        dialog.show();
    }


    private void showPageMarginBottomSheet() {

        BottomSheetPageMarginBinding sheetBinding =
                BottomSheetPageMarginBinding.inflate(
                        LayoutInflater.from(requireContext())
                );

        BottomSheetDialog dialog =
                new BottomSheetDialog(requireContext());

        dialog.setContentView(sheetBinding.getRoot());


        View[] rows = {
                sheetBinding.rowMarginNone,
                sheetBinding.rowMarginSmall,
                sheetBinding.rowMarginMedium,
                sheetBinding.rowMarginLarge
        };


        RadioButton[] radios = {
                sheetBinding.radioMarginNone,
                sheetBinding.radioMarginSmall,
                sheetBinding.radioMarginMedium,
                sheetBinding.radioMarginLarge
        };


        PageMargin[] options = {
                PageMargin.NONE,
                PageMargin.SMALL,
                PageMargin.MEDIUM,
                PageMargin.LARGE
        };


        // Get current state from ViewModel
        MergePdfOptions currentOptions = viewModel.getMergePdfOptions().getValue();


        PageMargin currentMargin = PageMargin.NONE;


        if (currentOptions != null
                && currentOptions.getMargin() != null) {

            currentMargin = currentOptions.getMargin();

        }


        // Mark currently selected option
        markSelectedRow(
                rows,
                radios,
                indexOf(options, currentMargin)
        );


        // Handle selection
        for (int i = 0; i < rows.length; i++) {

            final int index = i;

            rows[i].setOnClickListener(v -> {

                PageMargin selectedMargin = options[index];


                // ViewModel owns the state
                viewModel.setPageMargin(selectedMargin);


                // Update BottomSheet UI
                markSelectedRow(
                        rows,
                        radios,
                        index
                );


                // Update current Fragment UI
                binding.tvPageMarginValue.setText(getPageMarginText(selectedMargin));

                dialog.dismiss();

            });
        }


        dialog.show();
    }


    private void markSelectedRow(View[] rows, RadioButton[] radios, int selectedIndex) {

        for (int i = 0; i < rows.length; i++) {
            boolean isSelected = i == selectedIndex;
            rows[i].setSelected(isSelected);
            radios[i].setChecked(isSelected);
        }

    }

    private <T> int indexOf(T[] array, T value) {

        for (int i = 0; i < array.length; i++) {

            if (array[i] == value) return i;

        }

        return 0;
    }


    private void showFileOrderBottomSheet() {

        BottomSheetFileOrderBinding sheetBinding =
                BottomSheetFileOrderBinding.inflate(LayoutInflater.from(requireContext()));

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());

        dialog.setContentView(sheetBinding.getRoot());


        final ItemTouchHelper[] touchHelperHolder = new ItemTouchHelper[1];


        MergePdfAdapter sheetAdapter = new MergePdfAdapter(new MergePdfAdapter.Listener() {
            @Override
            public void onItemDeleted(MergePdfItem item) {

                viewModel.removePdf(item);

            }

            @Override
            public void onItemMoved(int fromPosition, int toPosition) {

                viewModel.swapPDFS(fromPosition ,  toPosition);

            }

            @Override
            public void onDragHandleTouched(MergePdfAdapter.PdfViewHolder viewHolder) {

                if (touchHelperHolder[0] != null) {

                    touchHelperHolder[0].startDrag(viewHolder);

                }

            }
        });

        if (viewModel != null) {

            viewModel.getPdfs().observe(getViewLifecycleOwner(), files -> {

                sheetAdapter.submitList(files);


            });
        }

        sheetBinding.fileOrderRecyclerView
                .setLayoutManager(new LinearLayoutManager(
                        requireContext()));

        sheetBinding.fileOrderRecyclerView.setAdapter(sheetAdapter);

        ItemTouchHelper sheetTouchHelper =
                new ItemTouchHelper(new MergePdfTouchHelperCallback(sheetAdapter));

        touchHelperHolder[0] = sheetTouchHelper;

        sheetTouchHelper.attachToRecyclerView(sheetBinding.fileOrderRecyclerView);

        sheetBinding.btnFileOrderDone.setOnClickListener(v -> {

            dialog.dismiss();

        });



        dialog.show();
    }


    private void showHelpDialog() {

        DialogMergeHelpBinding sheetBinding =
                DialogMergeHelpBinding.inflate(LayoutInflater.from(requireContext()));
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.setContentView(sheetBinding.getRoot());
        sheetBinding.btnHelpGotIt.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

    }

    private void updateUi(List<MergePdfItem> list) {

        if (binding == null) return;

        binding.tvSelectedFilesCount.setText(
                getString(
                        R.string.merge_selected_files_format,
                        list.size()
                )
        );

        boolean hasFiles = !list.isEmpty();

        binding.selectedPdfRecyclerView.setVisibility(hasFiles ? View.VISIBLE : View.GONE);
        binding.emptyStateContainer.setVisibility(hasFiles ? View.GONE : View.VISIBLE);

        binding.btnMergePdfs.setEnabled(list.size() >= 2);


    }

    private String getPageSizeText(PageSizeMode mode) {

        if (mode == null) {
            return getString(R.string.page_size_original);
        }

        switch (mode) {

            case ORIGINAL:
                return getString(R.string.page_size_original);

            case FIT_SMALLEST_WIDTH:
                return getString(
                        R.string.page_size_fit_smallest_width
                );

            case FIT_SMALLEST_HEIGHT:
                return getString(
                        R.string.page_size_fit_smallest_height
                );

            case FIT_SMALLEST_BOTH:
                return getString(
                        R.string.page_size_fit_smallest
                );

            case FIT_LARGEST_WIDTH:
                return getString(
                        R.string.page_size_fit_largest_width
                );

            case FIT_LARGEST_HEIGHT:
                return getString(
                        R.string.page_size_fit_largest_height
                );

            case FIT_LARGEST_BOTH:
                return getString(
                        R.string.page_size_fit_largest
                );

            case A4:
                return getString(R.string.page_size_a4);

            case A3:
                return getString(R.string.page_size_a3);

            case LEGAL:
                return getString(R.string.page_size_legal);

            case LETTER:
                return getString(R.string.page_size_letter);

            default:
                return "";
        }
    }

    private String getPageMarginText(PageMargin margin) {

        switch (margin) {

            case NONE:
                return getString(R.string.page_margin_none);

            case SMALL:
                return getString(R.string.page_margin_small);

            case MEDIUM:
                return getString(R.string.page_margin_medium);

            case LARGE:
                return getString(R.string.page_margin_large);

            default:
                return "";
        }
    }


    private void hideLoader(){

      if (pdfLoaderController ==  null ) return;

      if (binding != null){


          binding.loaderLayout.getRoot().setVisibility(View.GONE);


      }

      pdfLoaderController.hideLoader();


    }

    private void showLoader(){

        if (pdfLoaderController ==  null ) return;

        if (binding != null){

            binding.loaderLayout.getRoot().setFocusable(true);
            binding.loaderLayout.getRoot().setClickable(true);
            binding.loaderLayout.getRoot().requestFocus();
            binding.loaderLayout.getRoot().bringToFront();
            binding.loaderLayout.getRoot().setVisibility(View.VISIBLE);


        }

        pdfLoaderController.showLoader();


    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        itemTouchHelper = null;
        adapter = null;
        binding = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        clicksListener = null;
    }

}
