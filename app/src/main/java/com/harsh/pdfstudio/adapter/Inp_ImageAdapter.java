package com.harsh.pdfstudio.adapter;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.harsh.pdfstudio.R;
import com.harsh.pdfstudio.ui.ImageViewActivity;
import com.harsh.pdfstudio.utils.Byte_MB;
import com.harsh.pdfstudio.utils.File_Size;
import java.util.ArrayList;

public class Inp_ImageAdapter extends RecyclerView.Adapter<Inp_ImageAdapter.ViewHolder> {

    ArrayList<Uri> list;

    private   onImageRemove listener;

    public Inp_ImageAdapter(ArrayList<Uri> list) {

        this.list=list;
    }

    public void setOnRemove(onImageRemove listener){
        this.listener=listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.image_preview_item,parent,false);

        return new  ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,int position){
        Uri pos=list.get(position);

        String fileSize= Byte_MB.convertByte(File_Size.getFileSize(holder.size.getContext(), pos));

        holder.size.setText(fileSize);

        Glide.with(holder.imageView.getContext())
                .load(pos)
                .thumbnail(.2f)
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            Intent intent=new Intent(v.getContext(), ImageViewActivity.class);
            intent.putExtra("image_uri",pos);
            v.getContext().startActivity(intent);

        });

        holder.remove.setOnClickListener(v ->{
            int index=holder.getBindingAdapterPosition();

            if (index == RecyclerView.NO_POSITION) {
                return;
            }
            Long size=File_Size.getFileSize(holder.remove.getContext(), list.get(index));

            list.remove(index);
            notifyItemRemoved(index);
            notifyItemChanged(index,list);
            listener.imageRemove(size);

        });

    }

    @Override
    public int getItemCount(){
        return list.size();
    }





     static class ViewHolder extends RecyclerView.ViewHolder {

            ImageView imageView;
            ImageView remove;

            TextView size;


             ViewHolder(@NonNull View itemView) {

                super(itemView);

                imageView=itemView.findViewById(R.id.image_preview);
                remove=itemView.findViewById(R.id.removeBtn);
                size=itemView.findViewById(R.id.image_size);
            }









    }

    public interface onImageRemove{

        void  imageRemove(Long size);
    }

}