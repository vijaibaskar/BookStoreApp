package com.example.android.bookstoreapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.android.bookstoreapp.R;
import com.example.android.bookstoreapp.data.BookStoreContract.BookEntry;

/**
 * {@link ContentProvider} for BookStore app.
 */
public class BookStoreProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = BookStoreProvider.class.getSimpleName();
    /** URI matcher code for the content URI for the bookstore table */
    private static final int BOOKS = 100;
    /** URI matcher code for the content URI for a single inventory in the bookstore table */
    private static final int BOOK_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(BookStoreContract.CONTENT_AUTHORITY, BookStoreContract.PATH_BOOKSTORE, BOOKS);
        sUriMatcher.addURI(BookStoreContract.CONTENT_AUTHORITY, BookStoreContract.PATH_BOOKSTORE + "/#", BOOK_ID);
    }

    private BookStoreDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {
        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        mDbHelper = new BookStoreDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case BOOK_ID:
                // For the PET_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.pets/pets/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(BookEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertBook(Uri uri, ContentValues values) {

        // Check that the name is not null
        String name = values.getAsString(BookEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            Toast.makeText(getContext(), R.string.blank_entry, Toast.LENGTH_SHORT).show();
            throw new IllegalArgumentException("Inventory requires a name");
        }

        //Check that the price is not null
        Integer price = values.getAsInteger(BookEntry.COLUMN_PRICE);
        if (price == null) {
            Toast.makeText(getContext(), R.string.blank_entry, Toast.LENGTH_SHORT).show();
            throw new IllegalArgumentException("Inventory requires a price");
        }

        //Check that the quantity is not null
        Integer quantity = values.getAsInteger(BookEntry.COLUMN_QUANTITY);
        if (quantity == null) {
            Toast.makeText(getContext(), R.string.blank_entry, Toast.LENGTH_SHORT).show();
            throw new IllegalArgumentException("Inventory requires a quantity");
        }

        String supplierName = values.getAsString(BookEntry.COLUMN_SUPPLIER_NAME);
        if (supplierName == null) {
            Toast.makeText(getContext(), R.string.blank_entry, Toast.LENGTH_SHORT).show();
            throw new IllegalArgumentException("Supplier requires a name");
        }

        String supplierPhone = values.getAsString(BookEntry.COLUMN_SUPPLIER_PHONE);
        if (supplierPhone == null) {
            Toast.makeText(getContext(), R.string.blank_entry, Toast.LENGTH_SHORT).show();
            throw new IllegalArgumentException("Supplier requires a phone number");
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long id = database.insert(BookEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return updateBook(uri, contentValues, selection, selectionArgs);
            case BOOK_ID:
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateBook(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateBook(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // Check that the name is not null
        if (values.containsKey(BookEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(BookEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Inventory requires a name");
            }
        }

        //Check that the price is not null
        if (values.containsKey(BookEntry.COLUMN_PRICE)) {
            Integer price = values.getAsInteger(BookEntry.COLUMN_PRICE);
            if (price == null) {
                throw new IllegalArgumentException("Inventory requires a price");
            }
        }

        //Check that the quantity is not null
        if (values.containsKey(BookEntry.COLUMN_QUANTITY)) {
            Integer quantity = values.getAsInteger(BookEntry.COLUMN_QUANTITY);
            if (quantity == null) {
                throw new IllegalArgumentException("Inventory requires a quantity");
            }

        if (values.containsKey(BookEntry.COLUMN_SUPPLIER_NAME)) {
            String supplierName = values.getAsString(BookEntry.COLUMN_SUPPLIER_NAME);
            if (supplierName == null) {
                Toast.makeText(getContext(), R.string.blank_entry, Toast.LENGTH_SHORT).show();
                throw new IllegalArgumentException("Supplier requires a name");
            }
        }

        if (values.containsKey(BookEntry.COLUMN_SUPPLIER_PHONE)) {
            String supplierPhone = values.getAsString(BookEntry.COLUMN_SUPPLIER_PHONE);
            if (supplierPhone == null) {
                Toast.makeText(getContext(), R.string.blank_entry, Toast.LENGTH_SHORT).show();
                throw new IllegalArgumentException("Supplier requires a phone number");
            }
        }

            if (values.size() == 0) {
                return 0;
            }
        }

        // Otherwise, get writable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsUpdated = database.update(BookEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated !=0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;

    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete (Uri uri, String selection, String[]selectionArgs){

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case BOOK_ID:
                // Delete a single row given by the ID in the URI
                selection = BookEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted =  database.delete(BookEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted !=0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public String getType (Uri uri){

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case BOOKS:
                return BookEntry.CONTENT_LIST_TYPE;
            case BOOK_ID:
                return BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}