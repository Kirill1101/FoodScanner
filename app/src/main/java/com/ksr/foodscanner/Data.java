package com.ksr.foodscanner;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.ksr.foodscanner.Models.Product;

import java.util.ArrayList;
import java.util.Objects;

public class Data {
    static ArrayList<String> barcodes = new ArrayList<>();
    static ArrayList<Product> products = new ArrayList<>();
    static ArrayList<String> restrictions = new ArrayList<>();

    public static Product findProductInDB(String barcode, Context context) {
        DatabaseHelper sqlHelper = new DatabaseHelper(context);
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
        }

        Product product = new Product(data.get(0), data.get(1), data.get(2), data.get(3),
                data.get(4), data.get(5), data.get(6), data.get(7));
        if (!Data.barcodes.contains(data.get(0))) {
            Data.addBarcodes(barcode);
            Data.products.add(product);
        }
        return product;
    }

    public static Product findProductByBarcode(String barcode, Context context) {
        for (Product product : products) {
            if (product.getBarcode().equals(barcode)) {
                return product;
            }
        }
        return findProductInDB(barcode, context);
    }

    public static boolean addBarcodes(String barcode) {
        if (restrictions.contains(barcode)) {
            return false;
        }
        restrictions.add(barcode);
        return true;
    }

    public static boolean addRestriction(String restriction) {
        if (restrictions.contains(restriction)) {
            return false;
        }
        restrictions.add(restriction);
        return true;
    }

    public static boolean removeRestriction(String restriction) {
        return restrictions.remove(restriction);
    }
}
