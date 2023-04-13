package com.ksr.foodscanner;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ksr.foodscanner.Models.Product;

import java.io.IOException;
import java.io.InputStream;

public class ScanHistoryActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private FirebaseUser currentUser;
    private FirebaseRecyclerAdapter<String, ScanHistoryActivity.TaskViewHolder> adapter;

    /**
     * Инициализация, установка прослушиваний.
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Инициализация Firebase.
        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Users");
        currentUser = auth.getCurrentUser();

        // Установка RecyclerViewAdapter.
        RecyclerView recyclerView = findViewById(R.id.productList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        FirebaseRecyclerOptions<String> options =
                new FirebaseRecyclerOptions.Builder<String>()
                        .setQuery(dbRef.child(currentUser.getUid()).child("barcodes"), String.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<String, ScanHistoryActivity.TaskViewHolder>(options) {
            @Override
            protected void onBindViewHolder(ScanHistoryActivity.TaskViewHolder holder, int position, String model) {
                holder.barcodeViewInAll.setText(model);
                Product product = Data.findProductByBarcode(model, getApplicationContext());
                holder.titleViewInAll.setText(product.getTitle());
                String nameImage = "product_images/" + model + ".jpg";
                try (InputStream inputStream = getApplicationContext().getAssets().open(nameImage)) {
                    Drawable drawable = Drawable.createFromStream(inputStream, null);
                    holder.imageViewProductInAll.setImageDrawable(drawable);
                    holder.imageViewProductInAll.setScaleType(ImageView.ScaleType.FIT_CENTER);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public ScanHistoryActivity.TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.product_list_item, parent, false);
                return new ScanHistoryActivity.TaskViewHolder(view);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
    }

    /**
     * Адаптер заканчивает прослушивание.
     */
    @Override
    protected void onStop() {
        adapter.stopListening();
        super.onStop();
    }

    /**
     * Назначение кнопки.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_history);

        Button buttonToHome = findViewById(R.id.buttonToHome);
        buttonToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ScanHistoryActivity.this, HomeActivity.class));
                finish();
            }
        });
    }

    private static class TaskViewHolder extends RecyclerView.ViewHolder {

        ImageView imageViewProductInAll;
        TextView titleViewInAll, barcodeViewInAll;

        public TaskViewHolder(View itemView) {
            super(itemView);
            imageViewProductInAll = itemView.findViewById(R.id.imageViewProductInAll);
            titleViewInAll = itemView.findViewById(R.id.titleViewInAll);
            barcodeViewInAll = itemView.findViewById(R.id.barcodeViewInAll);
        }
    }
}