package com.example.android.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

public class ProductInfoProvider extends ContentProvider {

    /** URI matcher code for the content URI for the pets table */
    private static final int PRODUCTS = 100;

    /** URI matcher code for the content URI for a single pet in the pets table */
    private static final int PRODUCT_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static{
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    private InventoryDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(InventoryContract.InventoryEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return InventoryContract.InventoryEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return InventoryContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Product requires a name");
        }

        // Check that the Price is valid
        Double price = values.getAsDouble(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
        if (price == null || price < 0) {
            throw new IllegalArgumentException("Product requires valid price");
        }

        // Check that the Quantity is valid
        Integer quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Product requires valid quantity");
        }

        // Check that the Supplier Name is valid
        String supplierName = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
        if (supplierName == null || supplierName.isEmpty()) {
            throw new IllegalArgumentException("Product requires valid Supplier");
        }

        // Check that the Supplier Phone is valid
        String supplierPhone = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);
        if (supplierPhone == null || supplierPhone.isEmpty()) {
            throw new IllegalArgumentException("Product requires valid Supplier phone");
        }

        //Images can be Null


        SQLiteDatabase database = dbHelper.getWritableDatabase();

        long id = database.insert(InventoryContract.InventoryEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e("ProductProvider", "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(InventoryContract.InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // check that the name value is not null.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        // check that the price value is valid.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE)) {
            Double price = values.getAsDouble(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
            if (price == null || price < 0) {
                throw new IllegalArgumentException("Product requires valid price");
            }
        }

        // check that the quantity value is valid.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantity = values.getAsInteger(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity == null || quantity < 0) {
                throw new IllegalArgumentException("Product requires valid quantity");
            }
        }

        // check that the supplier value is valid.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME)) {
            String supplierName = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            if (supplierName == null || supplierName.isEmpty()) {
                throw new IllegalArgumentException("Product requires valid Supplier");
            }
        }

        // check that the supplier phone value is valid.
        if (values.containsKey(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME)) {
            String supplierPhone = values.getAsString(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);
            if (supplierPhone == null || supplierPhone.isEmpty()) {
                throw new IllegalArgumentException("Product requires valid Supplier phone");
            }
        }

        //Image can be null

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        int rowsUpdated = database.update(InventoryContract.InventoryEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }
}
