package com.harsh.pdfstudio.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.model.PageItem;
import com.harsh.pdfstudio.operations.EditOperation;
import com.harsh.pdfstudio.utils.Dp_to_Px;
import com.harsh.pdfstudio.utils.GenerateBitmapFromPDF;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class EditPDF_adap extends RecyclerView.Adapter<EditPDF_adap.ViewHolder> {

    List<PageItem> list=new ArrayList<>();

    private PdfRenderer pdfRenderer;

    private final GenerateBitmapFromPDF renderBitmap=new GenerateBitmapFromPDF();

    private final LruCache<Long, Bitmap> bitmapCache;
    private final LruCache<Integer,Bitmap> bitmapModfifiedPagesCache;

    ExecutorService executorService= Executors.newSingleThreadExecutor();
    Handler handler=new Handler(Looper.getMainLooper());

    HashSet<Long> selectedPages =new HashSet<>();


    HashMap<Integer, List<EditOperation>> operations_modifiedPages_map;
    Context context;
    private  onPDFPageClicks listener;
    private  HashMap<Long,PageItem> selectedPagesMap=new HashMap<>(); //  to easily remove the selected page item O(1)
    HashMap<Long, Integer> positionMap = new HashMap<>(); //  to map the adapter position  to the page id



    public void onPDFPage(onPDFPageClicks listener) {
        this.listener=listener;
    }

    public EditPDF_adap(PdfRenderer pdfRenderer, Context context){


        this.pdfRenderer=pdfRenderer;
        this.context=context;

        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        int cacheSize = maxMemory / 8;

        bitmapCache = new LruCache<Long, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(Long key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

        bitmapModfifiedPagesCache  = new LruCache<Integer, Bitmap>(cacheSize) {

            @Override
            protected int sizeOf(Integer key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder( ViewGroup viewGroup,int viewType){
        View v= LayoutInflater.from(viewGroup.getContext())
                            .inflate(R.layout.edit_pdf_recylerview_item,
                                    viewGroup,
                                    false
                            );

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder,int position){

        PageItem item=list.get(position);

        holder.pageNo.setText(item.getPdfPageIndex()+1+"");

        modifySelectedPageUi(holder, item.getIsSelected());

        Bitmap cached = bitmapCache.get(item.getId());

        if (cached != null) {

            holder.pagePreview.setImageBitmap(cached);

        }

        else {

            holder.pagePreview.setImageBitmap(null);

            long itemId = item.getId();

            if (item.getSource() == PageItem.Source.PDF) {

                executorService.execute(() -> {

                    Bitmap bitmap = renderBitmap.renderPage(item.getPdfPageIndex(), pdfRenderer);

                    bitmapCache.put(itemId, bitmap);

                    handler.post(() -> {

                        int current = holder.getAdapterPosition();

                        if (current == RecyclerView.NO_POSITION)
                            return;

                        if (list.get(current).getId() == itemId) {

                            holder.pagePreview.setImageBitmap(bitmap);


                        }

                    });
                });

            }

            else if(item.getSource()==PageItem.Source.IMAGE){

                holder.pageNo.setText(item.getImageFakeIndex()+1+"");

                Glide.with(context)
                        .load(item.getImageUri())
                        .thumbnail(0.3f)
                        .into(holder.pagePreview);

        }







        }

        holder.moreOpt.setOnClickListener(v -> {

            listener.moreOptionClick(v);

        });

        holder.itemView.setOnClickListener(v -> {

            int currentPosition = holder.getAdapterPosition();

            if (currentPosition == RecyclerView.NO_POSITION)
                return;

            PageItem item_ = list.get(currentPosition);

            long id = item_.getId();
            int pageNumber = item_.getPdfPageIndex() + 1;

            int operation;

            if (selectedPages.contains(id)) {

                selectedPages.remove(id);
                selectedPagesMap.remove(id);

                item_.setIsSelected(false);

                operation = -1;

            }
            else {

                selectedPages.add(id);
                selectedPagesMap.put(id, item_);

                item_.setIsSelected(true);

                operation = 1;
            }

            notifyItemChanged(currentPosition);

            listener.pageClick(operation, pageNumber, item_);

        });




    }

    @Override
    public int getItemCount(){
        return list.size();
    }

    public void setFileList(List<PageItem> list){
        this.list=list;

        positionMap.clear();

        for (int i = 0; i < list.size(); i++) {
            positionMap.put(list.get(i).getId(), i);
        }

        notifyDataSetChanged();
    };

    public void itemsExchange(int fromPos, int toPos) {

        if (fromPos == RecyclerView.NO_POSITION || toPos == RecyclerView.NO_POSITION)
            return;

        PageItem movedItem = list.remove(fromPos);
        list.add(toPos, movedItem);

        // Update position map
        positionMap.clear();

        for (int i = 0; i < list.size(); i++) {
            positionMap.put(list.get(i).getId(), i);
        }

        notifyItemMoved(fromPos, toPos);
    }

    public void modifySelectedPageUi(ViewHolder holder, boolean isSelected){

        if(isSelected){
            holder.cardView.setStrokeColor(ContextCompat.getColor(
                    holder.itemView.getContext(),R.color.purple_
            ));
            holder.cardView.setStrokeWidth(Dp_to_Px.dpToPx(2));

            holder.moreOpt.setImageResource(R.drawable.blue_tick);
            holder.moreOpt.setImageTintMode(null);
            holder.moreOpt.clearColorFilter();

            holder.moreOpt.setClickable(false);
            holder.moreOpt.setEnabled(false);

        }
        else{

            holder.cardView.setStrokeColor(ContextCompat.getColor(
                    holder.itemView.getContext(),R.color.default_
            ));

            holder.cardView.setStrokeWidth(Dp_to_Px.dpToPx(1));

            holder.moreOpt.setImageResource(R.drawable.three_dot_vertical);
            holder.moreOpt.setImageTintList(
                    ColorStateList.valueOf(
                            ContextCompat.getColor(
                                    holder.itemView.getContext(),
                                    R.color.darkGray
                            )
                    )
            );
            holder.moreOpt.setClickable(true);
            holder.moreOpt.setEnabled(true);

        }
    }

    public void setModifiedPageToHolder(ViewHolder holder,int pagePosition){


        Bitmap cachedbitmap=bitmapModfifiedPagesCache.get(pagePosition);

        if(cachedbitmap!=null){

                int currentPosition = holder.getAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION) return;

                int currentPagePosition = list.get(currentPosition).getPdfPageIndex();

                if (pagePosition == currentPagePosition) {
                    holder.pagePreview.setImageBitmap(cachedbitmap);

                }
        }
        else {

            List<EditOperation> listOfOp = operations_modifiedPages_map.get(pagePosition);

//            executorService.execute(() -> {
//
//
//                for (EditOperation op : listOfOp) {
//
//                    switch (op.getOperationsType()) {
//
//                        case WATERMARK:
//
//                            WatermarkPdfOperations watermarkOp = (WatermarkPdfOperations) op;
//
//                            int pageNum = watermarkOp.getPageNumber();
//
//                            Bitmap originalBitmap = bitmapCache.get(pageNum);
//
//                            if (originalBitmap == null) {
//
//                                originalBitmap = renderBitmap.renderPage(pageNum, pdfRenderer);
//                                bitmapCache.put(pageNum, originalBitmap);
//
//                            }
//
//                            try {
//                                Bitmap bitmap = WatermarkedBitmap.getWatermarkedBitmap(context,
//                                        originalBitmap,
//                                        watermarkOp.getWatermarkImageUri(),
//                                        watermarkOp.isWatermarkImage(),
//                                        watermarkOp.getWatermarkText(),
//                                        watermarkOp.getCurrentPosition(),
//                                        watermarkOp.getRotation(),
//                                        watermarkOp.getSize(),
//                                        watermarkOp.getOpacity());
//
//                                bitmapModfifiedPagesCache.put(pageNum, bitmap);
//
//                                handler.post(() -> {
//
//                                    int currentPosition = holder.getAdapterPosition();
//                                    if (currentPosition == RecyclerView.NO_POSITION) return;
//
//                                    int currentPagePosition = list.get(currentPosition).getPdfPageIndex();
//
//                                    if (pagePosition == currentPagePosition) {
//
//                                        holder.pagePreview.setImageBitmap(bitmap);
//
//                                    }
//
//                                });
//
//
//                            } catch (IOException e) {
//                                throw new RuntimeException(e);
//                            }
//                            break;
//
//                    }
//                }
//
//
//            });
        }



    }

    public void removeSelectedPages(){

        //  to remove all the selected page from everywhere

        for(long id:selectedPages){

            PageItem item=selectedPagesMap.get(id);

            if(item!=null){

                item.setIsSelected(false);

                Integer adapterPosition=positionMap.get(item.getId());

                if (adapterPosition != null) {
                    notifyItemChanged(adapterPosition);
                }

            }

        }


        selectedPages.clear();
        selectedPagesMap.clear();




    }

    public void updatePage(int pageNumber) {

        int position = list.indexOf(pageNumber);

        handler.post(() ->{
            if (position != RecyclerView.NO_POSITION) {
                notifyItemChanged(position);
            }

        } );


    }

    public  void informAdapterAboutModifiedPages(Stack<EditOperation> op_pages_stack, HashMap<Integer, List<EditOperation>> op_pages_list){

//        executorService.execute(() -> {
//
//            for (EditOperation op : op_pages_stack) {
//
//                modifiedPagesSet.add(op.getPageNumber());
//
//                switch (op.getOperationsType()) {
//
//                    case WATERMARK:
//
//                        WatermarkPdfOperations watermarkOp =
//                                (WatermarkPdfOperations) op;
//
//                        int pageNum=watermarkOp.getPageNumber();
//
//                        Bitmap originalBitmap=bitmapCache.get(pageNum);
//
//                        if(originalBitmap==null){
//
//                            originalBitmap=renderBitmap.renderPage(pageNum,pdfRenderer);
//                            bitmapCache.put(pageNum,originalBitmap);
//
//                        }
//
//                        try {
//
//
//
//                                Bitmap bitmap= WatermarkedBitmap.getWatermarkedBitmap(context,
//                                        originalBitmap,
//                                        watermarkOp.getWatermarkImageUri(),
//                                        watermarkOp.isWatermarkImage(),
//                                        watermarkOp.getWatermarkText(),
//                                        watermarkOp.getCurrentPosition(),
//                                        watermarkOp.getRotation(),
//                                        watermarkOp.getSize(),
//                                        watermarkOp.getOpacity());
//
//                                bitmapModfifiedPagesCache.put(pageNum,bitmap);
//
//                                updatePage(pageNum);
//
//
//
//
//
//
//                        } catch (IOException e) {
//                            throw new RuntimeException(e);
//                        }
//                        break;
//
//                }
//            }
//
//
//
//        });
       operations_modifiedPages_map=op_pages_list;




    }




    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView pagePreview;
        ImageView moreOpt;
        TextView pageNo;
        MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            pagePreview = itemView.findViewById(R.id.pdf_image_view);

            moreOpt = itemView.findViewById(R.id.moreOption);

            pageNo = itemView.findViewById(R.id.pageNo);

            cardView=itemView.findViewById(R.id.parent_box);

        }
    }

    public interface onPDFPageClicks{
        void moreOptionClick(View anchorView);
        void pageClick(int operation,int selectedPageNo,PageItem item);

        void onDoubleTapPage();



    }

}
