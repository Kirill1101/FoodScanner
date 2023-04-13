package com.ksr.foodscanner;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {

    /**
     * Создается пустое Activity, и в зависимости от того, зарегистрирован ли пользователь,
     * запускается нужное окно.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Intent intent;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, HomeActivity.class);
        }

        startActivity(intent);
        finish();
    }
}