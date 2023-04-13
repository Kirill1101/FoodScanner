package com.ksr.foodscanner;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ksr.foodscanner.Models.Product;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    RelativeLayout root;
    private Uri fileUri;
    private FirebaseAuth auth;
    private DatabaseReference dbRef;
    private FirebaseUser currentUser;
    private FirebaseRecyclerAdapter<String, TaskViewHolder> adapter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Используется для инициализации бд и старта прослушивания адаптера.
     */
    @Override
    protected void onStart() {
        super.onStart();

        // Инициализация Firebase.
        auth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference("Users");
        currentUser = auth.getCurrentUser();

        // Установка RecyclerViewAdapter.
        RecyclerView recyclerView = findViewById(R.id.restrictionsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        FirebaseRecyclerOptions<String> options =
                new FirebaseRecyclerOptions.Builder<String>()
                        .setQuery(dbRef.child(currentUser.getUid()).child("restriction"), String.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<String, TaskViewHolder>(options) {
            @Override
            protected void onBindViewHolder(TaskViewHolder holder, int position, String model) {
                holder.restrictionView.setText(model);
                Data.addRestriction(model);
                holder.removeRestrictionButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatabaseReference itemRef = getRef(holder.getBindingAdapterPosition());
                        itemRef.removeValue();
                        Data.removeRestriction(model);
                    }
                });
            }

            @Override
            public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.restrictions_list_item, parent, false);
                return new TaskViewHolder(view);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
    }

    /**
     * Адаптер заканичвает прослушивание.
     */
    @Override
    protected void onStop() {
        adapter.stopListening();
        super.onStop();
    }

    /**
     * Создание формы, назначение конпок.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Установка слушателя на кнопку "Сканировать".
        Button buttonScan = findViewById(R.id.buttonScan);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pictureActionIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                fileUri = getOutputMediaFileUri();
                pictureActionIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(pictureActionIntent, 0);
            }
        });

        // Установка слушателя на кнопку "Добавить ограничение".
        Button buttonAddRestriction = findViewById(R.id.buttonAddRestriction);
        buttonAddRestriction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                root = findViewById(R.id.root_element_home);
                showAddRestrictionWindows();
            }
        });

        // Установка слушателя на кнопку "История сканирований".
        Button buttonToHistory = findViewById(R.id.buttonToHistory);
        buttonToHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, ScanHistoryActivity.class));
                finish();
            }
        });
    }

    /**
     * Возвращает данные, необходимые для получения изоюражения.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        this.getContentResolver().notifyChange(fileUri, null);
        ContentResolver cr = this.getContentResolver();
        Bitmap bitmap;
        try {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, fileUri);
            findBarcodeOnPhoto(bitmap);
        } catch (Exception e) {
            Toast.makeText(this, "Ошибка", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Показывает окно с добавлением пищевых ограничений.
     */
    private void showAddRestrictionWindows() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Добавьте новое ограничение");

        LayoutInflater inflater = LayoutInflater.from(this);
        View signInWindow = inflater.inflate(R.layout.add_restriction, null);
        dialog.setView(signInWindow);

        final MaterialEditText restriction = signInWindow.findViewById(R.id.restrictionField);

        dialog.setNegativeButton("Назад", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("Добавить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (TextUtils.isEmpty(Objects.requireNonNull(restriction.getText()).toString())) {
                    Snackbar.make(root, "Введите ограничение", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (Objects.requireNonNull(restriction.getText()).toString().length() >= 20) {
                    Snackbar.make(root, "Должно быть < 20 символов",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (Objects.requireNonNull(restriction.getText()).toString().length() < 3) {
                    Snackbar.make(root, "Должно быть > 3 символов",
                            Snackbar.LENGTH_SHORT).show();
                    return;
                }

                dbRef.child(currentUser.getUid()).child("restriction").push()
                        .setValue(Objects.requireNonNull(restriction.getText().toString()));
            }
        });
        dialog.show();
    }

    /**
     * Поиск штрихкода на фото.
     * @param bitmap
     */
    private void findBarcodeOnPhoto(Bitmap bitmap) {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.EAN_13)
                .build();

        // Проверки.
        if (!barcodeDetector.isOperational()) {
            Toast toast = Toast.makeText(this,
                    "Не удалось настроить детектор", Toast.LENGTH_LONG);
            toast.show();
            return;
        }
        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Barcode> barcodes = barcodeDetector.detect(frame);
        if (barcodes.size() == 0) {
            Toast toast = Toast.makeText(this,
                    "На фотографии не был обнаружен штрихкод", Toast.LENGTH_LONG);
            toast.show();
            return;
        } else if (barcodes.size() > 1) {
            Toast toast = Toast.makeText(this,
                    "На фотографии было обнаружено несколько штрихкодов", Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        // Получаю штрихкод и отправляю в метод для поиска его в базе.
        Barcode thisCode = barcodes.valueAt(0);
        String barcode = thisCode.rawValue;
        findBarcodeInDatabase(barcode);
    }

    /**
     * Поиск штрихкода в бд.
     * @param barcode
     */
    private void findBarcodeInDatabase(String barcode) {
        DatabaseHelper sqlHelper = new DatabaseHelper(getApplicationContext());
        sqlHelper.create_db();
        SQLiteDatabase db = sqlHelper.open();

        String selection = "barcode = ?";
        String[] selectionArgs = new String[]{barcode};
        Cursor c = db.query("goods", null, selection, selectionArgs, null, null, null);

        String[] args = {barcode};
        //Cursor cursor = db.rawQuery("SELECT * FROM goods WHERE barcode=?", args);

        ArrayList<String> data = new ArrayList<>();

        if (c.moveToFirst()) {
            for (int i = 0; i < 8; ++i) {
                String info = c.getString(i);
                data.add(info);
            }
        } else {
            Toast.makeText(this, "Продукта с таким штрихкодом нет в базе данных",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Product product = new Product(data.get(0), data.get(1), data.get(2), data.get(3),
                data.get(4), data.get(5), data.get(6), data.get(7));
        if (!Data.barcodes.contains(data.get(0))) {
            dbRef.child(currentUser.getUid()).child("barcodes").push()
                    .setValue(Objects.requireNonNull(data.get(0)));
            Data.addBarcodes(barcode);
            Data.products.add(product);
        }
        showWindowWithProductInfo(product);
    }

    /**
     * Открывает окно с информаацией о прдукте.
     * @param product
     */
    private void showWindowWithProductInfo(Product product) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Информация о продукте");

        LayoutInflater inflater = LayoutInflater.from(this);
        View signInWindow = inflater.inflate(R.layout.product_info, null);
        dialog.setView(signInWindow);

        final ImageView imageViewProduct = signInWindow.findViewById(R.id.imageViewProduct);

        final TextView titleView = signInWindow.findViewById(R.id.titleView);
        final TextView compoundView = signInWindow.findViewById(R.id.compoundView);
        final TextView fatsView = signInWindow.findViewById(R.id.fatsView);
        final TextView proteinsView = signInWindow.findViewById(R.id.proteinsView);
        final TextView carbohydratesView = signInWindow.findViewById(R.id.carbohydratesView);
        final TextView caloriesView = signInWindow.findViewById(R.id.caloriesView);
        final TextView findResultView = signInWindow.findViewById(R.id.findResultView);

        // Заполоняю информацию о продукте.
        titleView.setText(product.getTitle());
        compoundView.setText("Состав: " + product.getCompound());
        fatsView.setText("Жиры: " + product.getFats());
        proteinsView.setText("Белки: " + product.getProteins());
        carbohydratesView.setText("Углеводы: " + product.getCarbohydrates());
        caloriesView.setText("Калории: " + product.getCalories());

        // Устанавливаю изображение.
        String nameImage = "product_images/" + product.getBarcode() + ".jpg";
        try (InputStream inputStream = getApplicationContext().getAssets().open(nameImage)) {
            Drawable drawable = Drawable.createFromStream(inputStream, null);
            imageViewProduct.setImageDrawable(drawable);
            imageViewProduct.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String compound = product.getCompound();
        // Ищу совпадения.
        ArrayList<String> eq = new ArrayList<>();
        for (String restriction : Data.restrictions) {
            if (compound.toLowerCase().contains(restriction.toLowerCase())) {
                eq.add(restriction);
            }
        }

        Resources resources = getResources();
        // Проверяю есть ли ограничения.
        if (eq.isEmpty()) {
            findResultView.setText("Ограничения не были найдены");
            int textColor = resources.getColor(R.color.light_green,  null);
            findResultView.setTextColor(textColor);
        } else {
            StringBuilder result = new StringBuilder("Ограничения были найдены:");
            for (String restriction : eq) {
                result.append(" ").append(restriction);
            }
            findResultView.setText(result.toString());
            int textColor = resources.getColor(R.color.pink_red,  null);
            findResultView.setTextColor(textColor);
        }


        dialog.setNegativeButton("Назад", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.setPositiveButton("К истории сканирования", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(HomeActivity.this, ScanHistoryActivity.class));
                finish();
            }
        });
        dialog.show();
    }

    /**
     * Получает мидиафайл.
     * @return
     */
    private Uri getOutputMediaFileUri() {
        return FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", getOutputMediaFile());
    }

    /**
     * Получает мидиафайл.
     * @return
     */
    private File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
        mediaFile.deleteOnExit();

        return mediaFile;
    }

    private static class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView restrictionView;
        Button removeRestrictionButton;

        public TaskViewHolder(View itemView) {
            super(itemView);
            restrictionView = itemView.findViewById(R.id.restrictionView);
            removeRestrictionButton = itemView.findViewById(R.id.removeRestrictionButton);
        }
    }
}