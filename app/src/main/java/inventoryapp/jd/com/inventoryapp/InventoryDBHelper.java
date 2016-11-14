package inventoryapp.jd.com.inventoryapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by chuondao on 11/12/16.
 */

public class InventoryDBHelper extends SQLiteOpenHelper {

    private static final String TEXT_TYPE = " TEXT";

    private static final String INTEGER_TYPE = " INTEGER";
    private static final String REAL_TYPE = " REAL";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CMD_CREATE_TABLE =
            "CREATE TABLE " + InventoryDBContract.InventoryEntry.TABLE_NAME + " ("
                    + InventoryDBContract.InventoryEntry._ID + " INTEGER PRIMARY KEY,"
                    + InventoryDBContract.InventoryEntry.COLUMN_ITEM_NAME + TEXT_TYPE + COMMA_SEP
                    + InventoryDBContract.InventoryEntry.COLUMN_AVAILABLE_QUANTITY + INTEGER_TYPE + COMMA_SEP
                    + InventoryDBContract.InventoryEntry.COLUMN_PRICE + REAL_TYPE + COMMA_SEP
                    + InventoryDBContract.InventoryEntry.COLUMN_SUPPLIER + TEXT_TYPE + COMMA_SEP
                    + InventoryDBContract.InventoryEntry.COLUMN_SUPPLIER_CONTACTS + TEXT_TYPE + COMMA_SEP
                    + InventoryDBContract.InventoryEntry.COLUMN_IMAGE + " BLOB" + " )";


    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    public InventoryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CMD_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
