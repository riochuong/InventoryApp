package inventoryapp.jd.com.inventoryapp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by chuondao on 11/12/16.
 */

public class InventoryConterntProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    // should we use enum
    private static final int INVENTORY = 1;
    private static final int INVENTORY_ITEM_ID = 2;
    private static final int INVENTORY_ITEM_NAME = 3;

    static {
        // initialize uri matcher
        sUriMatcher.addURI(InventoryDBContract.CONTENT_AUTHORITY,
                InventoryDBContract.PATH_INVENTORY, INVENTORY);

        sUriMatcher.addURI(InventoryDBContract.CONTENT_AUTHORITY,
                InventoryDBContract.PATH_INVENTORY + "/#", INVENTORY_ITEM_ID);

        sUriMatcher.addURI(InventoryDBContract.CONTENT_AUTHORITY,
                InventoryDBContract.PATH_INVENTORY + "/*", INVENTORY_ITEM_NAME);
    }

    InventoryDBHelper mDbHelper;

    @Override
    public boolean onCreate() {
        // initialize db helper
        mDbHelper = new InventoryDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {

        int match = sUriMatcher.match(uri);
        // just need to query db right now
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor returnCursor;
        switch (match) {
            case INVENTORY:
                Log.d(AppConst.TAG, "URI match INVENTORY case. Get the whole table");
                returnCursor = db.query(InventoryDBContract.InventoryEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INVENTORY_ITEM_ID:
                Log.d(AppConst.TAG, "URI match INVENTORY_ITEM_ID case");
                selection = InventoryDBContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{
                        String.valueOf(ContentUris.parseId(uri))
                };
                returnCursor = db.query(InventoryDBContract.InventoryEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                Log.d(AppConst.TAG, "query for item ID: " + selectionArgs[0]);
                break;
            case INVENTORY_ITEM_NAME:
                Log.d(AppConst.TAG, "URI match INVENTORY_NAME case");
                selection = InventoryDBContract.InventoryEntry.COLUMN_ITEM_NAME + "=?";
                String itemName = getStringAfterLastSlash(uri.toString());
                if (itemName == null) {
                    throw new IllegalArgumentException("URI is invalid for query item name ");
                }
                selectionArgs = new String[]{
                        itemName
                };
                returnCursor = db.query(InventoryDBContract.InventoryEntry.TABLE_NAME,
                        projection, selection, selectionArgs, null, null, sortOrder);
                Log.d(AppConst.TAG, "Query for item name " + itemName);
                break;
            default:
                Log.d(AppConst.TAG, "URI does not match content provider provided ones");
                throw new IllegalArgumentException("Cannot query unknown URI");

        }

        // set the URI notification change on the cursor
        // so we know what content URI the cursor is created for
        // if the data URI changes we know we need to update the cursor
        returnCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return returnCursor;
    }


    @Nullable
    @Override
    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return InventoryDBContract.InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ITEM_ID:
            case INVENTORY_ITEM_NAME:
                return InventoryDBContract.InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Cannot update Database with provided uri " + uri);
        }
    }

    @Nullable
    @Override
    /**
     * need to return the uri with the id of the new row
     */
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case INVENTORY:
                return insertInvetory(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not " +
                        "supported for this Uri type " + uri);
        }
    }

    private Uri insertInvetory(Uri uri, ContentValues contentValues) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        validateInsertContentValues(contentValues);

        long rowId = db.insert(InventoryDBContract.InventoryEntry.TABLE_NAME, null, contentValues);

        if (rowId < 0) {
            Log.e(AppConst.TAG, "Error insert into Table");
            return null;
        }

        // Notifiy that the data has changed
        // by default null the cursor adapter object will get notify
        getContext().getContentResolver().notifyChange(uri, null);
        Log.d(AppConst.TAG, "Data insert is good");
        return ContentUris.withAppendedId(uri, rowId);
    }

    /**
     * verify content values are valid data
     *
     * @param contentValues
     */
    private void validateInsertContentValues(ContentValues contentValues) {
        // ERROR CHECKS HERE
        String itemName = contentValues.getAsString(InventoryDBContract.
                InventoryEntry.COLUMN_ITEM_NAME);
        if (itemName == null) {
            throw new IllegalArgumentException("Item name must be a valid String");
        }
        Float price = contentValues.getAsFloat(InventoryDBContract.
                InventoryEntry.COLUMN_PRICE);

        if (price == null) {
            throw new IllegalArgumentException("Price must be a valid float value");
        }
        Long quantity = contentValues.getAsLong(InventoryDBContract.
                InventoryEntry.COLUMN_AVAILABLE_QUANTITY);

        if (quantity == null) {
            throw new IllegalArgumentException("Quantity must be a valid long value");
        }
        String supplier = contentValues.getAsString(InventoryDBContract.
                InventoryEntry.COLUMN_SUPPLIER);

        if (supplier == null) {
            throw new IllegalArgumentException("Supplier must be a valid string value");
        }
    }

    /**
     * verify update data contains correct data type
     *
     * @param contentValues
     */
    private void validateUpdateValue(ContentValues contentValues) {
        // ERROR CHECKS HERE
        String itemName = contentValues.getAsString(InventoryDBContract.
                InventoryEntry.COLUMN_ITEM_NAME);
        if (contentValues.containsKey(InventoryDBContract.InventoryEntry.COLUMN_ITEM_NAME)
                && itemName == null) {
            throw new IllegalArgumentException("Item name must be a valid String");
        }
        Float price = contentValues.getAsFloat(InventoryDBContract.
                InventoryEntry.COLUMN_PRICE);

        if (contentValues.containsKey(InventoryDBContract.InventoryEntry.COLUMN_PRICE)
                && price == null) {
            throw new IllegalArgumentException("Price must be a valid float value");
        }
        Long quantity = contentValues.getAsLong(InventoryDBContract.
                InventoryEntry.COLUMN_AVAILABLE_QUANTITY);

        if (contentValues.containsKey(InventoryDBContract.InventoryEntry.COLUMN_AVAILABLE_QUANTITY)
                && quantity == null) {
            throw new IllegalArgumentException("Quantity must be a valid long value");
        }
        String supplier = contentValues.getAsString(InventoryDBContract.
                InventoryEntry.COLUMN_SUPPLIER);

        if (contentValues.containsKey(InventoryDBContract.InventoryEntry.COLUMN_SUPPLIER)
                && supplier == null) {
            throw new IllegalArgumentException("Supplier must be a valid string value");
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int affectedRows;
        switch (match) {
            case INVENTORY:
                affectedRows = db.delete(InventoryDBContract.InventoryEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case INVENTORY_ITEM_ID:
                selection = InventoryDBContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                Log.d(AppConst.TAG, "Delete item with id " + ContentUris.parseId(uri));
                affectedRows = db.delete(InventoryDBContract.InventoryEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;
            case INVENTORY_ITEM_NAME:
                selection = InventoryDBContract.InventoryEntry.COLUMN_ITEM_NAME + "=?";
                String itemName = getStringAfterLastSlash(uri.toString());
                if (itemName == null) {
                    throw new IllegalArgumentException("Uri is invalid for delete operation");
                }
                Log.d(AppConst.TAG, "Delete item with name " + itemName);
                selectionArgs = new String[]{itemName};
                affectedRows = db.delete(InventoryDBContract.InventoryEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot update Database with provided uri " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return affectedRows;
    }

    @Override
    public int update(Uri uri,
                      ContentValues contentValues,
                      String selection,
                      String[] selectionArgs) {
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                return updateInventoryData(uri, contentValues, selection, selectionArgs);
            case INVENTORY_ITEM_ID:
                selection = InventoryDBContract.InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateInventoryData(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Cannot update Database with provided uri " + uri);
        }
    }

    private int updateInventoryData(Uri uri, ContentValues contentValues,
                                    String selection,
                                    String[] selectionArgs) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        validateUpdateValue(contentValues);
        Log.d(AppConst.TAG, "Update DB data ");
        int affectedRows = db.update(InventoryDBContract.InventoryEntry.TABLE_NAME,
                contentValues,
                selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return affectedRows;
    }


    /**
     * Helper method to extract string name of the item
     *
     * @param str
     * @return
     */
    private String getStringAfterLastSlash(String str) {
        int lastSlashIndex = str.lastIndexOf('/');
        // just make sure that the last slash is not the last char of the string
        if (lastSlashIndex < (str.length() - 1)) {
            return str.substring(lastSlashIndex + 1);
        }
        return null;
    }
}
