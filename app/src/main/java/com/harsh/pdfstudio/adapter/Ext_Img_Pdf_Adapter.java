package com.harsh.pdfstudio.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.model.ImageItem;
import com.harsh.pdfstudio.ui.ImageViewActivity;
import com.harsh.pdfstudio.utils.Byte_MB;
import com.harsh.pdfstudio.utils.File_Size;
import java.util.ArrayList;

public class Ext_Img_Pdf_Adapter extends RecyclerView.Adapter<Ext_Img_Pdf_Adapter.VH> {

    ArrayList<ImageItem> list;

    private imageRemoved listener;



    public Ext_Img_Pdf_Adapter(ArrayList<ImageItem> list){
            this.list=list;
    }

    public void  setOnRemoveImgClick(imageRemoved listener){
        this.listener=listener;
    }




    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent,int viewType){

        View v= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.img_prev_item_ext_pdf,parent,false);

        return  new VH(v);

    }

    @Override
    public void onBindViewHolder(@NonNull VH holder,int position){

        ImageItem item=list.get(position);
        Glide.with(holder.image.getContext()).load(item.getUri()).thumbnail(0.2f).into(holder.image);
        holder.imgFormat.setText(item.getFormat().toUpperCase());
        holder.imgSize.setText(Byte_MB.convertByte(item.getSize()));

        holder.image.setOnClickListener(v -> {

            Intent intent=new Intent(v.getContext(), ImageViewActivity.class);
            intent.putExtra("image_uri",item.getUri());
            v.getContext().startActivity(intent);

        });
        holder.remove.setOnClickListener(v -> {

            int pos=holder.getBindingAdapterPosition();

            Long fileSize= File_Size.getFileSize(v.getContext(),list.get(pos).getUri());
            list.remove(pos);
            notifyItemRemoved(pos);
            notifyItemChanged(pos,list);

            listener.onImageRemove(fileSize);


        });

    }

    @Override
    public int getItemCount(){

        return list.size();
    }










    class VH extends RecyclerView.ViewHolder{

        ImageView image;
        ImageView remove;
        TextView  imgSize;
        TextView imgFormat;


        VH(@NonNull View itemView){
            super(itemView);

            image=itemView.findViewById(R.id.image_preview_1);
            remove=itemView.findViewById(R.id.removeBtn_1);
            imgSize=itemView.findViewById(R.id.image_size_1);
            imgFormat=itemView.findViewById(R.id.image_format_1);



        }


    }

    public interface imageRemoved{
        void onImageRemove(long size);
    }
}
