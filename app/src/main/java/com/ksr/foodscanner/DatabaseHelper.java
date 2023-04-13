package com.ksr.foodscanner;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static String DB_PATH = null;
    private static String DB_NAME = "foodScannerDB.db";
    private final Context myContext;
    private SQLiteDatabase myDataBase;

    private static final int SCHEMA = 1;
    static final String TABLE = "goods";
    static final String COLUMN_BARCODE = "barcode";
    static final String COLUMN_TITLE = "title";
    static final String COLUMN_DESCRIPTION = "description";
    static final String COLUMN_PROTEINS = "proteins";
    static final String COLUMN_FATS = "fats";
    static final String COLUMN_CARBOHYDRATES = "carbohydrates";
    static final String COLUMN_CALORIES = "calories";

    DatabaseHelper(Context context) {
        super(context, DB_NAME, null, SCHEMA);
        this.myContext = context;
        DB_PATH = context.getFilesDir().getPath() + DB_NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    void create_db() {
        File file = new File(DB_PATH);
        if (!file.exists()) {
            //получаем локальную бд как поток
            try (InputStream myInput = myContext.getAssets().open(DB_NAME);
                 // Открываем пустую бд
                 OutputStream myOutput = new FileOutputStream(DB_PATH)) {

                // побайтово копируем данные
                byte[] buffer = new byte[1024];
                int length;
                while ((length = myInput.read(buffer)) > 0) {
                    myOutput.write(buffer, 0, length);
                }
                myOutput.flush();
            } catch (IOException ex) {
                Log.d("DatabaseHelper", ex.getMessage());
            }
        }
    }

    public SQLiteDatabase open() throws SQLException {
        return SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READONLY);
    }
}
