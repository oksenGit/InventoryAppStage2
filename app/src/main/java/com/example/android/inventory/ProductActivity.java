package com.example.android.inventory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.InventoryContract;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProductActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG = "Product Activity";
    private static final int PICK_IMAGE_REQUEST = 101;
    //Possible Modes for Product Activity
    private static final int MODE_ADD = 0;  //When the Activity is opened from "Add Product" in Inventory Activity
    private static final int MODE_EDIT = 1; //When Edit Button is pressed in the Product Activity when it's on MODE_DETAILS
    private static final int MODE_DETAILS = 2; //When List Item is pressed in Inventory Activity

    private int currentMode; //We ge its value from intent parameters as Details and Edit Uri is the same
    private Uri productUri; // We need to get the product ID from that Uri
    private Uri productImageUri;
    String imageString;
    boolean imageLoadedFromDatabase = false;

    //Binding all the Views in this activity and decide what to show
    //depending on our current mode using the function modeSetter
    //which will work on displaying only what should be displayed
    @BindView(R.id.product_edit)
    ImageView editButton;
    @BindView(R.id.product_delete)
    ImageView deleteButton;
    @BindView(R.id.product_save)
    TextView saveText;
    @BindView(R.id.product_add_edit_image)
    FloatingActionButton add_edit_ImageButton;
    @BindView(R.id.product_title)
    TextView titleText;
    @BindView(R.id.product_image)
    ImageView productimage;
    @BindView(R.id.product_name_edit)
    EditText nameEdit;
    @BindView(R.id.product_name_text)
    TextView nameText;
    @BindView(R.id.product_price_edit)
    EditText priceEdit;
    @BindView(R.id.product_price_text)
    TextView priceText;
    @BindView(R.id.product_qty_edit)
    EditText qtyEdit;
    @BindView(R.id.product_qty_text)
    TextView qtyText;
    @BindView(R.id.product_qty_up)
    ImageButton qtyUp;
    @BindView(R.id.product_qty_down)
    ImageButton qtyDown;
    @BindView(R.id.product_supplier_edit)
    EditText productSupplierEdit;
    @BindView(R.id.product_supplier_text)
    TextView productSupplierText;
    @BindView(R.id.product_phone_edit)
    EditText productPhoneEdit;
    @BindView(R.id.product_phone_text)
    TextView productPhoneText;
    @BindView(R.id.product_call)
    FloatingActionButton callButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        productUri = intent.getData();
        currentMode = intent.getIntExtra("mode", MODE_ADD);

        if (productUri != null)
            Log.i(LOG, productUri.toString());
        Log.i(LOG, currentMode + "");
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductActivity.this, ProductActivity.class);
                intent.setData(productUri);
                intent.putExtra("mode", MODE_EDIT);
                startActivity(intent);
            }
        });

        saveText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (saveProduct()) finish();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteDialog();
            }
        });

        qtyUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qtyControl(1);
            }
        });

        qtyDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qtyControl(-1);
            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentDailer = new Intent(Intent.ACTION_DIAL);
                intentDailer.setData(Uri.parse("tel:" + productPhoneText.getText().toString()));
                startActivity(intentDailer);
            }
        });

        add_edit_ImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, PICK_IMAGE_REQUEST);
            }
        });

        modeSetter(currentMode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            productImageUri = data.getData();
            imageString = productImageUri.toString();
            Log.i(LOG, "RESULT URI:" + productImageUri.toString());
            Picasso.get().load(productImageUri).into(productimage);
            getSupportLoaderManager().initLoader(1, null, ProductActivity.this);
        }
    }

    //Function created flexible to any increment or decrement
    //but in our case we will only give it 1 or -1 as input
    void qtyControl(int amount) {
        int currentQty = Integer.parseInt(qtyText.getText().toString());
        if (currentQty + amount >= 0) {
            currentQty += amount;
            qtyText.setText(currentQty + "");
            ContentValues values = new ContentValues();
            Uri uri = productUri;
            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, currentQty);
            getContentResolver().update(uri, values, null, null);
        }
    }

    void modeSetter(int mode) {
        switch (mode) {
            case MODE_ADD:
                editButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
                add_edit_ImageButton.setImageResource(R.drawable.add);
                titleText.setText(getResources().getString(R.string.add_product));
                nameText.setVisibility(View.GONE);
                priceText.setVisibility(View.GONE);
                qtyText.setVisibility(View.GONE);
                qtyDown.setVisibility(View.GONE);
                qtyUp.setVisibility(View.GONE);
                productSupplierText.setVisibility(View.GONE);
                productPhoneText.setVisibility(View.GONE);
                callButton.setVisibility(View.GONE);
                break;

            case MODE_EDIT:
                editButton.setVisibility(View.GONE);
                add_edit_ImageButton.setImageResource(R.drawable.edit);
                titleText.setText(getResources().getString(R.string.edit_product));
                nameText.setVisibility(View.GONE);
                deleteButton.setVisibility(View.GONE);
                priceText.setVisibility(View.GONE);
                qtyText.setVisibility(View.GONE);
                qtyDown.setVisibility(View.GONE);
                qtyUp.setVisibility(View.GONE);
                productSupplierText.setVisibility(View.GONE);
                productPhoneText.setVisibility(View.GONE);
                callButton.setVisibility(View.GONE);
                getSupportLoaderManager().initLoader(1, null, ProductActivity.this);
                break;

            case MODE_DETAILS:
                saveText.setVisibility(View.GONE);
                add_edit_ImageButton.setVisibility(View.GONE);
                titleText.setText(getResources().getString(R.string.product_details));
                nameEdit.setVisibility(View.GONE);
                priceEdit.setVisibility(View.GONE);
                qtyEdit.setVisibility(View.GONE);
                productSupplierEdit.setVisibility(View.GONE);
                productPhoneEdit.setVisibility(View.GONE);
                getSupportLoaderManager().initLoader(1, null, ProductActivity.this);
        }
    }

    boolean saveProduct() {
        String nameString = nameEdit.getText().toString().trim();
        String priceString = priceEdit.getText().toString().trim();
        String qtyString = qtyEdit.getText().toString().trim();
        String supplierString = productSupplierEdit.getText().toString().trim();
        String phoneString = productPhoneEdit.getText().toString().trim();

        if (TextUtils.isEmpty(nameString) ||
                TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(qtyString) ||
                TextUtils.isEmpty(supplierString) ||
                TextUtils.isEmpty(phoneString)) {
            Toast.makeText(getApplicationContext(), "All Fields are Requeried execpt the image", Toast.LENGTH_SHORT).show();

            return false;
        } else {

            ContentValues values = new ContentValues();
            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME, nameString);

            if (!TextUtils.isEmpty(priceString))
                values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE, Double.parseDouble(priceString));

            if (!TextUtils.isEmpty(qtyString))
                values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, Integer.parseInt(qtyString));

            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME, supplierString);
            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE, phoneString);


            if (productImageUri != null) {
                Log.i(LOG, "FROM SAVE:" + productImageUri.toString());
                values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_IMAGE, productImageUri.toString());
            }
            if (currentMode == MODE_ADD) {
                Uri newUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);
                if (newUri == null)
                    Toast.makeText(this, getResources().getString(R.string.insertion_failed), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, getResources().getString(R.string.insertion_success), Toast.LENGTH_SHORT).show();

            } else {
                int rowsAffected = getContentResolver().update(productUri, values, null, null);
                if (rowsAffected == 0)
                    Toast.makeText(this, getResources().getString(R.string.update_failed), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, getResources().getString(R.string.update_success), Toast.LENGTH_SHORT).show();
            }
            return true;
        }

    }

    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_DayNight_Dialog_MinWidth);
        builder.setMessage(R.string.delete_confirm);
        builder.setPositiveButton(R.string.delete_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.delete_no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    void deleteProduct() {
        if (productUri != null) {
            int rowDeleted = getContentResolver().delete(productUri, null, null);
            if (rowDeleted == 0)
                Toast.makeText(this, getResources().getString(R.string.delete_failed), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, getResources().getString(R.string.delete_success), Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {"*"};

        return new CursorLoader(this,
                productUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int imageColIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_IMAGE);
            int nameColIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
            int qtyColIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierColIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_NAME);
            int phoneColIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);

            if (!imageLoadedFromDatabase) {
                imageString = cursor.getString(imageColIndex);
                imageLoadedFromDatabase = true;
            }
            if (!TextUtils.isEmpty(imageString) && currentMode != MODE_ADD) {
                productImageUri = Uri.parse(imageString);
            }
            String name = cursor.getString(nameColIndex);
            Double price = cursor.getDouble(priceColIndex);
            int qty = cursor.getInt(qtyColIndex);
            String supplier = cursor.getString(supplierColIndex);
            String phone = cursor.getString(phoneColIndex);

            if (currentMode == MODE_EDIT) {
                nameEdit.setText(name);
                priceEdit.setText(price + "");
                qtyEdit.setText(qty + "");
                productSupplierEdit.setText(supplier);
                productPhoneEdit.setText(phone);
            } else {
                nameText.setText(name);
                priceText.setText(price + "");
                qtyText.setText(qty + "");
                productSupplierText.setText(supplier);
                productPhoneText.setText(phone);
            }
            if (productImageUri != null && !TextUtils.isEmpty(imageString)) {
                Log.i(LOG, productImageUri.toString() + "FROM LOAD");
                Picasso.get().load(productImageUri).into(productimage);
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        nameEdit.setText("");
        priceEdit.setText("");
        qtyEdit.setText("");
        productSupplierEdit.setText("");
        productPhoneEdit.setText("");
        productimage.setImageBitmap(null);
    }

}
