package com.example.gunlukapp.adapter;



import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gunlukapp.MemoryListener;
import com.example.gunlukapp.R;
import com.example.gunlukapp.entities.Memory;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

public class MyMemoryAdapter extends RecyclerView.Adapter<MyMemoryAdapter.MemoryViewHolder>{
    private List<Memory> diaryList;
    private MemoryListener memListener;


    public MyMemoryAdapter(List<Memory> diaryList, MemoryListener memListener) {
        this.memListener = memListener;
        this.diaryList = diaryList;
    }

    @NonNull
    @Override
    public MemoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MemoryViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.list_item,
                        parent,
                        false
                )
        );
    }


    @Override
    public void onBindViewHolder(@NonNull MemoryViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.setMemory(diaryList.get(position));
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                memListener.onMemoryClicked(diaryList.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return diaryList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class MemoryViewHolder extends RecyclerView.ViewHolder {
        TextView itemTitle, itemDate, itemLocation;
        RoundedImageView itemImg;
        LinearLayout itemLayout;

        public MemoryViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTitle = itemView.findViewById(R.id.itemTitle);
            itemDate = itemView.findViewById(R.id.itemDate);
            itemImg = itemView.findViewById(R.id.memoryImg);
            itemLocation = itemView.findViewById(R.id.itemLocation);
            itemLayout = itemView.findViewById(R.id.layoutItem);
        }

        void setMemory(Memory mem){
            itemTitle.setText(mem.getTitle());
            itemDate.setText(mem.getDate());

            if(mem.getLocation().equals("Enter Location") || mem.getLocation().equals("")){
                itemLocation.setVisibility(View.GONE);
            }else{
                itemLocation.setVisibility(View.VISIBLE);
                itemLocation.setText(mem.getLocation());
            }




            if(mem.getImgPath() != null){
                itemImg.setImageBitmap(BitmapFactory.decodeFile(mem.getImgPath()));
                itemImg.setVisibility(View.VISIBLE);
            }else{
                itemImg.setVisibility(View.GONE);
            }
        }
    }
}
