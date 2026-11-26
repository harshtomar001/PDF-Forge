package com.harsh.pdfstudio.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.ui.PdfViewerActivity;
import java.util.ArrayList;



public class MergePDF_Adapter extends RecyclerView.Adapter<MergePDF_Adapter.view_holder> {

    private final ArrayList<FileItem> list;

    private  onPdfRemove listener;

//    TextView upload_btn;
    public MergePDF_Adapter(ArrayList<FileItem> list){
        this.list=list;
//        this.upload_btn=btn;

    }

    public void setOnremovePDf(onPdfRemove listener){
        this.listener=listener;
    }

    @NonNull
    @Override

    public  view_holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        View v= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.merge_pdf_item_file,parent,false);

        return new view_holder(v);
    }

    @Override

    public void onBindViewHolder(@NonNull view_holder holder ,int position){

        FileItem item=list.get(position);

        String pdfName=item.getName();

        holder.fileName.setText( pdfName.length()<22?pdfName: pdfName.substring(0,21) + "..." );

        holder.itemView.setOnClickListener(v ->{
            Context context=v.getContext();
            Intent intent=new Intent(context, PdfViewerActivity.class);
            intent.putExtra("pdf_uri",item.getUri());
            intent.putExtra("mode","view");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(intent);

        });

        holder.remove_btn.setOnClickListener( v ->{
            int pos=holder.getAdapterPosition();
            FileItem pdf=list.get(pos);
            list.remove(pos);
            notifyItemRemoved(pos);
            notifyItemChanged(pos,list);
            listener.onRemove(pdf.getPageCount(), pdf.getFileSize());

        });

    }


    @Override
    public int getItemCount(){
        return  list.size();
    }





    static class  view_holder extends RecyclerView.ViewHolder{
        TextView fileName;
        ImageView remove_btn;
        view_holder(@NonNull View itemView){
            super(itemView);
            fileName=itemView.findViewById(R.id.fileName_merge_pdf);
            remove_btn=itemView.findViewById(R.id.remove_file_merge_pdf);
        }
    }

    public interface onPdfRemove{
        void onRemove(int pages,long size);
    }



}
