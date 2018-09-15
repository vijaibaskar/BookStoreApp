package com.example.android.bookstoreapp.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.AppBarLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.bookstoreapp.R;
import com.example.android.bookstoreapp.data.BookStoreContract.BookEntry;

public class BookStoreCursorAdapter extends CursorAdapter {

    public BookStoreCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    public void bindView(View view, final Context context, Cursor cursor) {
        TextView nameTextView = view.findViewById(R.id.name);
        TextView priceTextView = view.findViewById(R.id.price_value);
        final TextView quantityTextView = view.findViewById(R.id.quantity_value);

        int idColumnIndex = cursor.getColumnIndex(BookEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);

        final int bookId = cursor.getInt(idColumnIndex);
        final String bookName = cursor.getString(nameColumnIndex);
        final String bookPrice = cursor.getString(priceColumnIndex);
        final String bookQuantity = cursor.getString(quantityColumnIndex);

        nameTextView.setText(bookName);
        priceTextView.setText(bookPrice);
        quantityTextView.setText(bookQuantity);

        Button saleButton = view.findViewById(R.id.sale_button);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri currentUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, bookId);

                ContentValues values = new ContentValues();
                values.put(BookEntry.COLUMN_PRODUCT_NAME, bookName);
                values.put(BookEntry.COLUMN_PRICE, bookPrice);


                if(Integer.valueOf(bookQuantity) > 0 ) {
                    values.put(BookEntry.COLUMN_QUANTITY, Integer.valueOf(bookQuantity) - 1);
                }

                context.getContentResolver().update(currentUri, values, null, null);
                quantityTextView.setText(bookQuantity);
            }
        });

    }
}
