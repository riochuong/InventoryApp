package inventoryapp.jd.com.inventoryapp;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InventoryAppActivity extends AppCompatActivity
        implements LoaderCallbacks<Cursor>, View.OnClickListener, ListView.OnItemClickListener {

    private static final int DATA_LOADER = 1;

    private static final String SORT_ORDER_BY_NAME =
            InventoryDBContract.InventoryEntry.COLUMN_ITEM_NAME + " ASC";
    // bind view here
    @BindView(R.id.main_list_view_item)
    ListView mListItemView;
    @BindView(R.id.add_item_float_btn)
    FloatingActionButton addButton;
    private InventoryCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_app);
        ButterKnife.bind(this);
        addButton.setOnClickListener(this);
        mListItemView.setOnItemClickListener(this);
        mCursorAdapter = new InventoryCursorAdapter(this, null);
        mListItemView.setAdapter(mCursorAdapter);
        mListItemView.setEmptyView(findViewById(R.id.empty_list_item));
        this.getSupportLoaderManager().initLoader(DATA_LOADER, null, this);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case DATA_LOADER:
                // get all table data sorted by item name
                return new CursorLoader(
                        this,
                        InventoryDBContract.InventoryEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        SORT_ORDER_BY_NAME
                );
            default:
                Log.e(AppConst.TAG, "Failled to recognized loader id");
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(AppConst.TAG, "Finish loading new data");
        mCursorAdapter.changeCursor(data);
    }

    /*
     * Invoked when the CursorLoader is being reset. For example, this is
     * called if the data in the provider changes and the Cursor becomes stale.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // clear up old data
        Log.d(AppConst.TAG, "Cursor Reset");
        mCursorAdapter.changeCursor(null);

    }

    // for floating button .....
    @Override
    public void onClick(View view) {
        Log.d(AppConst.TAG, "Adding new item ");
        Intent launchDetaiItemView = new Intent(this, ItemDetailViewActivity.class);
        launchDetaiItemView.putExtra(AppConst.ADD_NEW_ITEM, true);
        startActivity(launchDetaiItemView);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Log.d(AppConst.TAG, "Item is Clicked at " + position);
        Intent launchItemView = new Intent(this, ItemDetailViewActivity.class);
        Cursor cursor = mCursorAdapter.getCursor();
        long itemId = cursor.getLong(cursor.
                getColumnIndexOrThrow(InventoryDBContract.InventoryEntry._ID));
        launchItemView.setData(Uri.withAppendedPath(
                InventoryDBContract.InventoryEntry.CONTENT_URI,
                itemId + ""));
        startActivity(launchItemView);
    }
}
