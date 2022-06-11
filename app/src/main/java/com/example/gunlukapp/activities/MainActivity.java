package com.example.gunlukapp.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.gunlukapp.MemoryListener;
import com.example.gunlukapp.R;
import com.example.gunlukapp.activities.CreateMemoryActivity;
import com.example.gunlukapp.adapter.MyMemoryAdapter;
import com.example.gunlukapp.database.AppDatabase;
import com.example.gunlukapp.entities.Memory;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MemoryListener {

    public static final int REQUEST_CODE_ADD_MEMORY = 1;
    public static final int REQUEST_CODE_UPDATE_MEMORY = 2;
    public static final int REQUEST_CODE_SHOW_MEMORY=3;

    RecyclerView recyclerView;
    List<Memory> memoryList;
    MyMemoryAdapter myAdapter;
    ImageView addBtn;

    private int memoryClickedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addBtn = findViewById(R.id.addBtn);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                    new Intent(getApplicationContext(), CreateMemoryActivity.class),
                        REQUEST_CODE_ADD_MEMORY
                );
            }
        });

        recyclerView = findViewById(R.id.diaryRecyclerView);
        recyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        );

        memoryList = new ArrayList<>();
        myAdapter = new MyMemoryAdapter(memoryList, this);
        recyclerView.setAdapter(myAdapter);
        listMemories(REQUEST_CODE_SHOW_MEMORY,false);
    }

    @Override
    public void onMemoryClicked(Memory memory, int position) {
        memoryClickedPosition = position;
        Intent intent = new Intent(getApplicationContext(),CreateMemoryActivity.class);
        intent.putExtra("isViewOrUpdate",true);
        intent.putExtra("memory", memory);
        startActivityForResult(intent, REQUEST_CODE_UPDATE_MEMORY);
    }

    private void listMemories(final int requestCode, final boolean isItemDeleted){
        @SuppressLint("StaticFieldLeak")
        class listMemoriesTask extends AsyncTask<Void, Void, List<Memory>> {


            @Override
            protected List<Memory> doInBackground(Void... voids) {
                return AppDatabase.getDatabase(getApplicationContext()).memDao().getMemories();
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void onPostExecute(List<Memory> memories) {
                super.onPostExecute(memories);
                if(requestCode == REQUEST_CODE_SHOW_MEMORY) {
                    memoryList.addAll(memories);
                    myAdapter.notifyDataSetChanged();
                }else if(requestCode == REQUEST_CODE_ADD_MEMORY){
                    memoryList.add(0,memories.get(0));
                    myAdapter.notifyItemInserted(0);
                }else if(requestCode == REQUEST_CODE_UPDATE_MEMORY){
                    memoryList.remove(memoryClickedPosition);
                    if(isItemDeleted){
                        myAdapter.notifyItemRemoved(memoryClickedPosition);
                    }else{
                        memoryList.add(memoryClickedPosition, memories.get(memoryClickedPosition));
                        myAdapter.notifyItemChanged(memoryClickedPosition);
                    }
                }
            }
        }

        new listMemoriesTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_ADD_MEMORY && resultCode == RESULT_OK){
            listMemories(REQUEST_CODE_SHOW_MEMORY,false);
        }else if(requestCode == REQUEST_CODE_UPDATE_MEMORY && resultCode == RESULT_OK){
            if (data != null){
                listMemories(REQUEST_CODE_UPDATE_MEMORY,data.getBooleanExtra("isMemoryDeleted",false));
            }
        }
    }
}