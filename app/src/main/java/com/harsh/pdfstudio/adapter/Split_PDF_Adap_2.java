package com.harsh.pdfstudio.adapter;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.model.FileItem;
import com.harsh.pdfstudio.utils.Byte_MB;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Split_PDF_Adap_2 extends RecyclerView.Adapter<Split_PDF_Adap_2.ViewHolder> {

    ArrayList<FileItem> list;

    private final Set<Uri> uriSet=new HashSet<>();

    public Split_PDF_Adap_2(ArrayList<FileItem> list){

        this.list=list;
        fill_Uri_To_Set(list);


    }

    private itemClicked listener;

    public void onItemClicked(itemClicked listener){

        this.listener=listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,int viewType){

        View v= LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.split_pdf_pdf_item,parent,false);

        return new ViewHolder(v);


    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,int position){

        FileItem item=list.get(position);

        holder.selectBtn.setChecked(uriSet.contains(item.getUri()));

        holder.pdfName.setText(item.getName());

        holder.metaData.setText(String.format("Page %d   •  %s",item.getPageCount(), Byte_MB.convertByte(item.getFileSize())));

        holder.itemView.setOnClickListener(v -> {

            listener.pdfPreviewClick(item.getUri());

        });

        holder.moreBtn.setOnClickListener(v -> {

            int pos=holder.getAdapterPosition();

            listener.moreOptionClick(item.getUri(),pos,v);

        });

        holder.selectBtn.setOnClickListener(v -> {

            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            Uri uri = list.get(pos).getUri();

            boolean isChecked;

            if (uriSet.contains(uri)) {
                Log.d("already contains",uri+"");
                uriSet.remove(uri);   // unselect
                holder.selectBtn.setChecked(false);
                isChecked=false;
            }
            else {
                uriSet.add(uri);      // select again
                holder.selectBtn.setChecked(true);
                isChecked=true;
                Log.d("not contains",uri+"");
            }

//            Log.d("isChecked",isChecked+"");
            listener.checkBoxClick(uri,isChecked);




        });

    }

    @Override
    public int getItemCount(){
        return list.size();
    }

    public void updateList(ArrayList<FileItem> newList) {
        this.list = newList;
        fill_Uri_To_Set(list);
    }

    public void  remove_singleton_item(int posInList){
        list.remove(posInList);
        notifyItemRemoved(posInList);
        notifyItemChanged(posInList,list);
    }

    public  void fill_Uri_To_Set(ArrayList<FileItem> list){

        for (FileItem item:list){
            uriSet.add(item.getUri());
        }

        notifyDataSetChanged();


    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView pdfName;
        TextView metaData;
        ImageView moreBtn;
        CheckBox selectBtn;

        public ViewHolder(@NonNull View view){
            super(view);

            pdfName=view.findViewById(R.id.pdfName_split_pdf);
            metaData=view.findViewById(R.id.pdfMeta);
            moreBtn=view.findViewById(R.id.moreBtn);
            selectBtn=view.findViewById(R.id.checkBtn);


        }

    }

    public interface  itemClicked{
        void  pdfPreviewClick(Uri uri);
        void  checkBoxClick(Uri uri,boolean checked);
        void   moreOptionClick(Uri uri,int position,View view);

    }
}