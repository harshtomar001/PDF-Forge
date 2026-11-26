package com.harsh.pdfstudio.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.harsh.pdfstudio.R;

import java.util.ArrayList;

public class ImageOutAdapter extends RecyclerView.Adapter<ImageOutAdapter.ViewHolder> {
    private ArrayList<Uri> list;

    public ImageOutAdapter(ArrayList<Uri> list){
        this.list=list;

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_img_item_file_out, parent, false);
        return new ViewHolder(view);

    }
    @Override

    public void  onBindViewHolder(@NonNull ViewHolder holder, int position) {




    }

    @Override

    public int getItemCount() {
        return list.size();
    }







      static  class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ImageView download;
        ImageView delete;




        public ViewHolder(@NonNull View itemView){
            super(itemView);


            image=itemView.findViewById(R.id.photo);
            delete=itemView.findViewById(R.id.delete);
            download =itemView.findViewById(R.id.download);

        }




        }

}
