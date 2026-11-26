package com.harsh.pdfstudio.adapter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.LruCache;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.ui.ImageViewActivity;
import com.harsh.pdfstudio.utils.GenerateBitmapFromPDF;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Split_PDF_Adap_1 extends RecyclerView.Adapter<Split_PDF_Adap_1.ViewHolder> {

        private final ArrayList<Integer> pages;
        private final PdfRenderer pdfRenderer;

        private final GenerateBitmapFromPDF renderBitmap=new GenerateBitmapFromPDF();

         private final LruCache<Integer, Bitmap> bitmapCache;



        public Split_PDF_Adap_1(ArrayList<Integer> list,PdfRenderer renderer){
            this.pdfRenderer=renderer;
            this.pages=list;


            int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            int cacheSize = maxMemory / 8;

            bitmapCache = new LruCache<Integer, Bitmap>(cacheSize) {

                @Override
                protected int sizeOf(Integer key, Bitmap bitmap) {
                    return bitmap.getByteCount() / 1024;
                }
            };

        }


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int viewType){
            View v= LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.split_pdf_page_item_preview,parent,false);

            return new ViewHolder(v);

        }

        @Override

        public void onBindViewHolder(ViewHolder holder,int position){

            holder.pageNo.setText(pages.get(position)+"");


            ExecutorService executors= Executors.newSingleThreadExecutor();
            Handler handler=new Handler(Looper.getMainLooper());
            int pageNumber = pages.get(position);
            int pageIndex = pageNumber - 1;

            Bitmap cached = bitmapCache.get(pageIndex);

            if (cached != null) {
                holder.pdfPage.setImageBitmap(cached);
                holder.progressBar.setVisibility(View.GONE);
                return;
            }

            holder.pdfPage.setImageBitmap(null); // now wrong image flash file scrolling and till loading the actual bitmap
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.progressBar.bringToFront();

            executors.execute(() -> {

                Bitmap imgBitmap = renderBitmap.renderPage(pageIndex, pdfRenderer);

                bitmapCache.put(pageIndex, imgBitmap);

                handler.post(() -> {

                    if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) return;

                    int currentPageNumber = pages.get(holder.getAdapterPosition());

                    int currentPageIndex = currentPageNumber - 1;

                    if (currentPageIndex == pageIndex) {
                        holder.pdfPage.setImageBitmap(imgBitmap);
                        holder.progressBar.setVisibility(View.GONE);
                    }
                });
            });

//            holder.pdfPage.setOnClickListener(v -> {
//                Intent intent=new Intent(holder.pdfPage.getContext(), ImageViewActivity.class);
//                intent.putExtra()
//                holder.pdfPage.getContext().startActivity(intent);
//
//            });



        }

        @Override

        public int getItemCount(){
            return pages.size();
        }



    public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView pdfPage;
            TextView  pageNo;

            FrameLayout progressBar;

        public ViewHolder( @NonNull View v) {

            super(v);

            pdfPage=v.findViewById(R.id.pdf_page_imageview);
            pageNo=v.findViewById(R.id.pageNo);
            progressBar=v.findViewById(R.id.progressBar);

        }
    }

}
