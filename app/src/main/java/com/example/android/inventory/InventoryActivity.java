package com.example.android.inventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.inventory.data.InventoryContract.InventoryEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InventoryActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int INVENTORY_LOADER = 1;
    @BindView(R.id.list)
    ListView listView;

    @BindView(R.id.inventory_add)
    TextView addProductButton;

    @BindView(R.id.empty_text)
    TextView emptyText;

    private InventoryCursorAdapter cursorAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        ButterKnife.bind(this);

        cursorAdapter = new InventoryCursorAdapter(this,null);
        listView.setAdapter(cursorAdapter);

        addProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryActivity.this, ProductActivity.class);
                intent.putExtra("mode",0);
                startActivity(intent);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Log.i("Inventory", "CLICK CLICK");
                Intent intent = new Intent(InventoryActivity.this, ProductActivity.class);
                intent.setData(ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id));
                intent.putExtra("mode",2);
                startActivity(intent);
                cursorAdapter.notifyDataSetChanged();
            }
        });

        listView.setEmptyView(emptyText);

        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {"*"};
        return new CursorLoader(this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        cursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cursorAdapter.notifyDataSetChanged();
    }
}
