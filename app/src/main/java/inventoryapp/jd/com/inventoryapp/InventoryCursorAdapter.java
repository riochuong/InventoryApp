package inventoryapp.jd.com.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by chuondao on 11/13/16.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    // The new view method is used to inflat the new view and return it
    // you dont bind any data to the view at this point
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.inventorty_item_layout, viewGroup, false);
    }

    // bind all data to a given view
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView itemName = (TextView) view.findViewById(R.id.item_name_text);
        TextView itemPrice = (TextView) view.findViewById(R.id.price_text);
        TextView itemQuantity = (TextView) view.findViewById(R.id.quantity_text);
        TextView itemSupplier = (TextView) view.findViewById(R.id.supplier_text);
        Button saleButton = (Button) view.findViewById(R.id.sale_button);
        ImageView itemImage = (ImageView) view.findViewById(R.id.image_item_source);
        itemImage.setImageBitmap(null);
        int itemId = cursor.getInt(
                cursor.getColumnIndexOrThrow(InventoryDBContract.InventoryEntry._ID));
        itemName.setText(cursor.getString(cursor.getColumnIndexOrThrow
                (InventoryDBContract.InventoryEntry.COLUMN_ITEM_NAME)));
        itemPrice.setText(cursor.getString(cursor.getColumnIndexOrThrow
                (InventoryDBContract.InventoryEntry.COLUMN_PRICE)));
        itemQuantity.setText(cursor.getString(cursor.getColumnIndexOrThrow
                (InventoryDBContract.InventoryEntry.COLUMN_AVAILABLE_QUANTITY)));
        itemSupplier.setText(cursor.getString(cursor.getColumnIndexOrThrow
                (InventoryDBContract.InventoryEntry.COLUMN_SUPPLIER)));
        byte[] imageData = cursor.getBlob(cursor.getColumnIndexOrThrow
                (InventoryDBContract.InventoryEntry.COLUMN_IMAGE));

        if (imageData != null && imageData.length > 0) {
            Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            itemImage.setImageBitmap(imageBitmap);
            Log.d(AppConst.TAG, "Image item set !!! " + itemName.getText());
        }


        saleButton.setOnClickListener(new OnSelectItemOnClickListener(itemId, itemQuantity));

    }

    private class OnSelectItemOnClickListener implements Button.OnClickListener {

        private int itemId;
        private TextView itemQuanity;


        public OnSelectItemOnClickListener(int itemId, TextView itemQ) {
            this.itemId = itemId;
            this.itemQuanity = itemQ;
        }

        @Override
        public void onClick(View view) {
            try {
                int quantity = Integer.parseInt(itemQuanity.getText().toString());
                int new_quantity = (quantity > 0) ? quantity - 1 : 0;
                // need to update the db with this new data
                if (new_quantity != quantity) {
                    String selection = InventoryDBContract.InventoryEntry._ID + "=?";
                    String[] selectionArgs = new String[]{String.valueOf(itemId)};
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(InventoryDBContract.InventoryEntry.COLUMN_AVAILABLE_QUANTITY,
                            new_quantity);
                    Uri uri = Uri.withAppendedPath(
                            InventoryDBContract.InventoryEntry.CONTENT_URI, itemId + "");
                    view.getContext().getContentResolver().update(uri, contentValues,
                            selection, selectionArgs);
                    Log.d(AppConst.TAG, "Update sale item count on Database " + itemId);
                }

            } catch (NumberFormatException e) {
                e.printStackTrace();
                Log.e(AppConst.TAG, "Item should not have invalid quanity for sale");
            }
        }
    }


}
