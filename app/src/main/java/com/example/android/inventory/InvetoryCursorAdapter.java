package com.example.android.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventory.data.InventoryContract;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoProvider;

import java.io.IOException;
import java.io.InputStream;
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

        final int id = cursor.getInt(idColumnIndex);
        final String currentName = cursor.getString(nameColumnIndex);
        final Double currentPrice = cursor.getDouble(priceColumnIndex);
        final int currentQuantity = cursor.getInt(quantityColumnIndex);
        final String currentImage = cursor.getString(imageColumnIndex);
        Uri imageUri = null;
        if(!TextUtils.isEmpty(currentImage))
           imageUri = Uri.parse(currentImage);
        if(imageUri == null || TextUtils.isEmpty(currentImage)){
            image.setImageResource(R.drawable.noimage);
        } else {
            Log.i("TAG","image Uri = " + imageUri.toString());
            Picasso.get().load(imageUri).into(image);

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
                    values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, currentQuantity-1);

                    int rowsAffected = view.getContext().getContentResolver().update(uri, values, null, null);
                }
            }
        });

    }

}
