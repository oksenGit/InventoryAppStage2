package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventory.data.InventoryContract;

import java.net.URI;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InvetoryCursorAdapter extends CursorAdapter {

    @BindView(R.id.item_image)
    ImageView image;

    @BindView(R.id.item_name)
    TextView name;

    @BindView(R.id.item_summary)
    TextView summary;

    @BindView(R.id.item_sale)
    Button sale;

    public InvetoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup,false);
    }

    @Override
    public void bindView(final View view, Context context, Cursor cursor) {
        ButterKnife.bind(this, view);

        int idColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        int imageColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_IMAGE);
        int supplierColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
        int phoneColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);

        final int id = cursor.getInt(idColumnIndex);
        final String currentName = cursor.getString(nameColumnIndex);
        final Double currentPrice = cursor.getDouble(priceColumnIndex);
        final int currentQuantity = cursor.getInt(quantityColumnIndex);
        final String currentImage = cursor.getString(imageColumnIndex);
        final String currentSupplier = cursor.getString(supplierColumnIndex);
        final String currentSupplierPhone = cursor.getString(phoneColumnIndex);

        if(currentImage==null){
            image.setImageResource(R.drawable.noimage);
        }

        name.setText(currentName);

        String summaryString = "$"+currentPrice+" - "+view.getResources().getString(R.string.qty)+currentQuantity;
        summary.setText(summaryString);

        sale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentQuantity>0){
                    ContentValues values = new ContentValues();
                    Uri uri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);
                    values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME, currentName);
                    values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE, currentPrice);
                    values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, currentQuantity-1);
                    values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_IMAGE, currentImage);
                    values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, currentSupplier);
                    values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, currentSupplierPhone);
                    int rowsAffected = view.getContext().getContentResolver().update(uri, values, null, null);
                }
            }
        });
    }

}
