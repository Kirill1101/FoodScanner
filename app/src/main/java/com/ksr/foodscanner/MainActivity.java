package com.ksr.foodscanner;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ksr.foodscanner.Models.Product;
import com.ksr.foodscanner.Models.User;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    Button buttonSignIn, buttonRegister;
    FirebaseAuth auth;
    FirebaseDatabase firebaseDB;
    DatabaseReference users;

    RelativeLayout root;

    /**
     * В этом методе происходит инициализация полей и установка слушателей.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonSignIn = findViewById(R.id.buttonSignIn);
        buttonRegister = findViewById(R.id.buttonRegister);

        root = findViewById(R.id.root_element_main);

        auth = FirebaseAuth.getInstance();
        firebaseDB = FirebaseDatabase.getInstance();
        users = firebaseDB.getReference("Users");

        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSignInWindows();
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRegisterWindows();
            }
        });
    }

    /**
     * Метод, который открывает окно для авторизации.
     */
    private void showSignInWindows() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Вход");
        dialog.setMessage("Введите данные для входа");

        LayoutInflater inflater = LayoutInflater.from(this);
        View signInWindow = inflater.inflate(R.layout.sign_in, null);
        dialog.setView(signInWindow);

        final MaterialEditText email = signInWindow.findViewById(R.id.emailField);
        final MaterialEditText password = signInWindow.findViewById(R.id.passField);

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("Войти", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (TextUtils.isEmpty(Objects.requireNonNull(email.getText()).toString())) {
                    Snackbar.make(root, "Введите ваше почту", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (Objects.requireNonNull(password.getText()).toString().length() < 5) {
                    Snackbar.make(root, "Введите пароль длинной 6 и более символов", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                auth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                finish();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Snackbar.make(root, "Ошибка авторизации " + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        });
        dialog.show();
    }

    /**
     * Метод, который открывает окно для регистрации.
     */
    private void showRegisterWindows() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Регистрация");
        dialog.setMessage("Введите данные для регистрации");

        LayoutInflater inflater = LayoutInflater.from(this);
        View registerWindow = inflater.inflate(R.layout.register, null);
        dialog.setView(registerWindow);

        final MaterialEditText email = registerWindow.findViewById(R.id.emailField);
        final MaterialEditText password = registerWindow.findViewById(R.id.passField);
        final MaterialEditText name = registerWindow.findViewById(R.id.nameField);

        dialog.setNegativeButton("Отменить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("Продолжить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (TextUtils.isEmpty(Objects.requireNonNull(email.getText()).toString())) {
                    Snackbar.make(root, "Введите ваше почту", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(Objects.requireNonNull(name.getText()).toString())) {
                    Snackbar.make(root, "Введите ваше имя", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (Objects.requireNonNull(password.getText()).toString().length() < 5) {
                    Snackbar.make(root, "Введите пароль длинной 6 и более символов", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                // Регистрация польователя
                auth.createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                User user = new User(name.getText().toString(),
                                        email.getText().toString(), password.getText().toString(),
                                        new ArrayList<String>(Arrays.asList("Глютен", "Арахис", "Молоко")));

                                String uId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                                users.child(Objects.requireNonNull(uId))
                                        .setValue(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                                finish();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NotNull Exception e) {
                                        Snackbar.make(root, "Ошибка регистрации " + e.getMessage(),
                                                Snackbar.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
            }
        });
        dialog.show();
    }
}