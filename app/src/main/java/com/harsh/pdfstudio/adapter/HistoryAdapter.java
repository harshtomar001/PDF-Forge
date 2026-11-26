package com.harsh.pdfstudio.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.model.RecentFileItem;
import com.harsh.pdfstudio.ui.PdfViewerActivity;
import com.harsh.pdfstudio.utils.DeletePDF;
import com.harsh.pdfstudio.utils.GetFirstPage;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.os.Handler;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    // adding listener

    private  onRemoveFile listener;

    public  void setOnRemoveFile(onRemoveFile listener){

        this.listener=listener;

    }

    private   ArrayList<RecentFileItem> recentFileItems;

    private Context context;

    private final ExecutorService executor= Executors.newFixedThreadPool(2);

    private final Handler handler = new  Handler(Looper.getMainLooper());


    public HistoryAdapter(ArrayList<RecentFileItem> recentFileItems, Context context){
        this.recentFileItems=recentFileItems;
        this.context=context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pdf_item_file, parent ,false);

        return  new ViewHolder(v);
    }



    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        RecentFileItem recentFileItem=recentFileItems.get(position);

        String name=
                recentFileItem.getPdfName().length() > 24 ?
                        recentFileItem.getPdfName().substring(0,23) :
                        recentFileItem.getPdfName();


        String size=recentFileItem.getPdfSize();
        int pages=recentFileItem.getPdfPages();
        Uri uri= Uri.parse(recentFileItem.getPdfUri());
        String date=recentFileItem.getPdfDate();


        holder.pdfName.setText(name);

        holder.pdfDate.setText(date);

        holder.pagesCount.setText(pages+ " Pages");

        holder.pdfSize.setText(size);


        executor.execute( () -> {

            Bitmap bitmap= GetFirstPage.getFirstPageBitmap(context,uri);
            handler.post( () -> {
                holder.imageView.setImageBitmap(bitmap);
                holder.waterMark.setVisibility(View.VISIBLE);

            });


        });


        holder.viewPdf.setOnClickListener(v -> {

            Intent intent=new Intent(context, PdfViewerActivity.class);
            intent.putExtra("pdf_uri",uri);
            intent.putExtra("mode","view");
            v.getContext().startActivity(intent);


        });

        holder.deletePdf.setOnClickListener(v -> {

            int pos=holder.getAdapterPosition();
            recentFileItems.remove(pos);
            notifyItemRemoved(pos);
            notifyItemRangeChanged(pos,recentFileItems.size());
            DeletePDF.Remove_PDF_From_Storage(context,pos);
            listener.onRemove();
        });

    }

    @Override
    public int getItemCount() {
        return recentFileItems.size();
    }
    public void updateList(ArrayList<RecentFileItem> list){
        this.recentFileItems=list;
    }


    static  class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView  pdfName;
        TextView pdfSize;
        TextView pagesCount;
        TextView pdfDate;
        ImageView viewPdf;
        ImageView deletePdf;

        TextView waterMark;

         ViewHolder(@NonNull View itemView) {
             super(itemView);


            imageView=itemView.findViewById(R.id.pagePreview);
            pdfName=itemView.findViewById(R.id.pdfName);
            pdfSize=itemView.findViewById(R.id.pdfSize);
            pdfDate=itemView.findViewById(R.id.pdfDate);
            pagesCount=itemView.findViewById(R.id.pdfPages);
            viewPdf=itemView.findViewById(R.id.viewPDF);
            deletePdf=itemView.findViewById(R.id.deletePDF);
            waterMark=itemView.findViewById(R.id.textPDF);

         }

    }


    public  interface  onRemoveFile{
        void onRemove();

    }


}