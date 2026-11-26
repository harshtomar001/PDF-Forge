package com.harsh.pdfstudio.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.ui.PdfViewerActivity;
import com.harsh.pdfstudio.utils.Byte_MB;
import com.harsh.pdfstudio.utils.SaveFile;

import java.util.ArrayList;

public class PDF_Created_Adapter extends RecyclerView.Adapter<PDF_Created_Adapter.view_holder> {

    ArrayList<FileItem> list ;

    private  onRemoveListener listener;

    public void setOnRemoveListener(onRemoveListener listener){
        this.listener=listener;
    }

    public PDF_Created_Adapter(ArrayList<FileItem> list){
        this.list=list;
    }


    @Override

    public  view_holder onCreateViewHolder(@NonNull ViewGroup parent,int viewType){

        View v= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_file_creation_pdf,parent,false);
        return new view_holder(v);
    }


    @Override

    public  void onBindViewHolder(@NonNull view_holder holder,int position){

        FileItem item=list.get(position);
        String file_name= item.getName();
        int pageCount=item.getPageCount();
        Uri uri=item.getUri();
        String filesize= Byte_MB.convertByte(item.getFileSize());

        holder.fileName.setText(file_name.length()<34?file_name+".pdf":file_name.substring(0,33)+"...");

        holder.fileSize.setText(holder.itemView.getContext().getString(R.string.fileSize)+" "+filesize);

        holder.filePages.setText(holder.itemView.getContext().getString(R.string.pagesCount)+" "+pageCount);

        holder.fileName.setOnClickListener( v ->{
            Intent intent=new Intent(v.getContext(), PdfViewerActivity.class);
            intent.putExtra("pdf_uri",uri);
            intent.putExtra("mode","preview");
            v.getContext().startActivity(intent);
        });

        holder.delete.setOnClickListener( v ->{

            list.remove(position);
            notifyItemRemoved(position);
            listener.onRemove();

        });

        holder.download.setOnClickListener( v ->{


            Context context = v.getContext();

            EditText input = new EditText(context);
            input.setHint("Enter PDF name");

            new AlertDialog.Builder(context)
                    .setTitle("Save PDF")
                    .setView(input)

                    .setPositiveButton("Save", (dialog, which) -> {

                        String fileName = input.getText().toString().trim();

                        if(fileName.isEmpty()){
                            fileName = file_name;
                        }

                        SaveFile.savePDF(context, uri, fileName,pageCount,filesize);
                    })

                    .setNegativeButton("Cancel", null)
                    .show();


        });
    }


    @Override
    public  int getItemCount(){
        return  list.size();
    }

    static  class view_holder extends RecyclerView.ViewHolder{

        TextView fileName;
        TextView fileSize;
        TextView filePages;
        ImageView download;
        ImageView delete;

        view_holder(@NonNull View itemView){
            super(itemView);

            fileName=itemView.findViewById(R.id.file_name_creation_pdfs);

            fileSize=itemView.findViewById(R.id.file_size_creation_pdfs);

            filePages=itemView.findViewById(R.id.file_pages_creation_pdfs);

            download=itemView.findViewById(R.id.download_creation_pdfs);

            delete=itemView.findViewById(R.id.delete_creation_pdfs);


        }
    }

    public interface onRemoveListener{
        void onRemove();
    }

}


