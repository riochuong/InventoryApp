package inventoryapp.jd.com.inventoryapp;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by chuondao on 11/12/16.
 */

public class InventoryDBContract {

    /**
     * content authority to call the Inventory provider
     */
    public static final String CONTENT_AUTHORITY = "com.jd.inventoryapp";

    ;
    /**
     * Based URi can be used to concatenate path to find the data
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /* patg to be append to the inventory table*/
    public static final String PATH_INVENTORY = "inventory";

    // avoid wrong initialization
    private InventoryDBContract() {
    }

    public static class InventoryEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;


        public static final String _ID = BaseColumns._ID;

        public static final String TABLE_NAME = "inventory_table";

        public static final String COLUMN_ITEM_NAME = "column_item_name";

        public static final String COLUMN_PRICE = "column_price";

        public static final String COLUMN_AVAILABLE_QUANTITY = "available_quantity";

        public static final String COLUMN_SUPPLIER = "supplier";

        public static final String COLUMN_SUPPLIER_CONTACTS = "supplier_contacts";

        public static final String COLUMN_IMAGE = "image";


    }
}
