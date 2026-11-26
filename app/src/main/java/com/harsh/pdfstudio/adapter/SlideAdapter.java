package com.harsh.pdfstudio.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.harsh.pdfstudio.R;
import java.util.List;

public class SlideAdapter extends RecyclerView.Adapter<SlideAdapter.SlideViewHolder> {

    List<Integer> images;

    public interface  onClickListener{

        void  onItemClick(int item);

    }

    private  onClickListener listener;

    public  SlideAdapter(List<Integer> images,onClickListener listener){
        this.images=images;
        this.listener = listener;

    }

    @Override

    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            return  new SlideViewHolder(LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.slider_item,parent,false));

    }

    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {

        int item=images.get(position);

        listener.onItemClick(item);
        holder.imageView.setImageResource(item);

    }

    @Override
    public int getItemCount() {
        return images.size();
    }


    public static class SlideViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;


            SlideViewHolder(@NonNull View itemView) {
                super(itemView);
                imageView=itemView.findViewById(R.id.slideImageView);
            }
        }


}
