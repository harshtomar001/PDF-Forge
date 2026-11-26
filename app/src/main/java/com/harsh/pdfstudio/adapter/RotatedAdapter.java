package com.harsh.pdfstudio.adapter;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.card.MaterialCardView;
import com.harsh.pdfstudio.R;

import java.util.ArrayList;

public class RotatedAdapter extends RecyclerView.Adapter<RotatedAdapter.VH> {

    private ArrayList<Uri> list;

    private  ArrayList<Integer> originalIndex =null;
    private float rotation = 0f;
    String Mode=null;
    Boolean threeDot=null;

    public  RotatedAdapter(ArrayList<Uri> list){

        this.list = new ArrayList<>(list);


    }

    public  RotatedAdapter(ArrayList<Uri> list, String mode,Boolean threeDot,ArrayList<Integer> originalIndex){

        this.list = new ArrayList<>(list);
        this.Mode=mode;
        this.threeDot=threeDot;
        this.originalIndex=originalIndex;

    }



    @Override

    public VH onCreateViewHolder( @NonNull ViewGroup parent,int viewType){

        View v= LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.rotated_prev_item_page,parent,false);

        return new VH(v);
    }

    @Override

    public void onBindViewHolder(@NonNull VH holder,int position){
        Uri uri=list.get(position);

        int itemType=getItemViewType(position);



        if (itemType==-1) holder.pageNo.setVisibility(View.GONE);
        else{

            if (itemType==1) {
            holder.cardView.setStrokeWidth(0);
            holder.imagePreview.setVisibility(View.GONE);
            holder.textView.setVisibility(View.VISIBLE);
            holder.pageNo.setVisibility(View.GONE);
            }
            else{
               holder.pageNo.setText(originalIndex.get(position)+"");
            }

        }




        holder.imagePreview.setImageURI(null);

        Glide.with(holder.imagePreview.getContext())
                .load(uri)
                .thumbnail(0.2f)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(holder.imagePreview);

        holder.imagePreview.setRotation(rotation);


    }

    @Override

    public int getItemCount(){
        return list.size();
    }

    @Override

    public int getItemViewType(int position){

        if (Mode==null) return -1 ;
        else{
            if (threeDot && position==list.size()-2){
                return 1;
            }
            else{
                return 0;
            }
        }

    }



    public void rotation(float rotate){

        rotation=rotate;
        notifyDataSetChanged();

    }

    public void updateList(ArrayList<Uri> newList){

        this.list.clear();
        this.list.addAll(newList);

        notifyDataSetChanged();


    }







    static class VH extends RecyclerView.ViewHolder{
        ImageView imagePreview;
        MaterialCardView cardView;

        TextView  textView;
        TextView pageNo;

        public  VH(View view){
            super(view);
            imagePreview=view.findViewById(R.id.img_preview);
            cardView=view.findViewById(R.id.card_view_img);
            textView=view.findViewById(R.id.three_dot);
            pageNo=view.findViewById(R.id.page_no);



        }
    }
}
