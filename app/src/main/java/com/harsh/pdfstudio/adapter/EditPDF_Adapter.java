package com.harsh.pdfstudio.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.ui.PdfViewerActivity;
import com.harsh.pdfstudio.utils.Byte_MB;
import java.util.ArrayList;

public class EditPDF_Adapter extends RecyclerView.Adapter<EditPDF_Adapter.VHolder>{

   private final ArrayList<FileItem> list;
    private OnPdfRemovedListener listener;

    public void setOnPdfRemovedListener(OnPdfRemovedListener listener) {
        this.listener = listener;
    }


    public  EditPDF_Adapter(ArrayList<FileItem> list){
        this.list=list;
    }

    @NonNull
    @Override

    public  VHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        View v= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.edit_pdf_item_file,parent,false);

        return new VHolder(v);
    }

    @Override

    public  void onBindViewHolder(@NonNull VHolder holder,int position){

        FileItem item=list.get(position);

        String FileName= item.getName();

        int pageCount=item.getPageCount();

        Uri uri=item.getUri();

        String formatted_fileSize= Byte_MB.convertByte(item.getFileSize());

        holder.fileName.setText(FileName.length()<24?FileName : FileName.substring(0,23));

        holder.fileSize.setText(holder.itemView.getContext().getString(R.string.fileSize) + " " + formatted_fileSize);

        holder.pagesCount.setText(holder.itemView.getContext().getString(R.string.pagesCount) +" "+pageCount);

        holder.itemView.setOnClickListener( v ->{
            Context context=v.getContext();
            Intent intent=new Intent(context, PdfViewerActivity.class);
            intent.putExtra("pdf_uri",uri);
            intent.putExtra("mode","view");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(intent);
        });

        holder.removeFile.setOnClickListener(v ->{

            list.clear();
            notifyDataSetChanged();

            listener.onPdfRemoved();

        });



    }


    @Override

    public int getItemCount(){
        return  list.size();
    }



    static  class VHolder extends RecyclerView.ViewHolder{

        ImageView removeFile;
        TextView fileName;
        TextView fileSize;
        TextView pagesCount;

        VHolder(@NonNull View itemView){

            super(itemView);
            fileName=itemView.findViewById(R.id.file_name_edit_pdf);
            removeFile=itemView.findViewById(R.id.cross_sign_edit_pdf);
            fileSize=itemView.findViewById(R.id.file_size_edit_pdf);
            pagesCount=itemView.findViewById(R.id.pages_edit_pdf);

        }


    }
    public  interface OnPdfRemovedListener {
        void onPdfRemoved(); // a method the activity will implement
    }
}

