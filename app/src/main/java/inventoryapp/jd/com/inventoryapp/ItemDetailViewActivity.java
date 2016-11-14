package inventoryapp.jd.com.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chuondao on 11/13/16.
 */

public class ItemDetailViewActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, Button.OnClickListener {


    private static final String NA_TEXT = "NA";

    private static final int DETAIL_VIEW_LOADER = 2;

    private static final int REQUEST_IMAGE_SELECT = 0;

    private static final int BUFFER_SIZE = 1024;

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final String PHONE_PATTERN = "^[0-9\\-]*$"; // only match number and dash

    private static final String EMAIM_MIME_TYPE = "vnd.android.cursor.item/email";
    @BindView(R.id.detail_view_title_text)
    EditText nameTextView;
    @BindView(R.id.detail_view_price_text)
    EditText priceTextView;
    @BindView(R.id.detail_view_quantity_text)
    EditText quantityTextView;
    @BindView(R.id.detail_view_supplier_text)
    EditText supplierTextView;
    @BindView(R.id.detail_view_supplier_contacts)
    EditText supplierContactsTextView;
    @BindView(R.id.increase_quantity_btn)
    Button increaseQuantityBtn;
    @BindView(R.id.decrease_quantity_btn)
    Button decreaseQuantityBtn;
    @BindView(R.id.delete_btn)
    Button deleteItemBtn;
    @BindView(R.id.order_btn)
    Button orderItemBtn;
    @BindView(R.id.select_image_btn)
    Button selectImageBtn;
    @BindView(R.id.detail_image_view)
    ImageView itemImage;
    private Uri mDataUri;
    private byte[] imageByteArray = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataUri = getIntent().getData();
        setContentView(R.layout.item_detail_view_layout);
        ButterKnife.bind(this);
        increaseQuantityBtn.setOnClickListener(this);
        decreaseQuantityBtn.setOnClickListener(this);
        itemImage.setVisibility(View.GONE);
        selectImageBtn.setOnClickListener(this);
        deleteItemBtn.setOnClickListener(this);
        // only allow delete and order button when in EDIT MODE
        deleteItemBtn.setVisibility(View.INVISIBLE);
        orderItemBtn.setOnClickListener(this);
        orderItemBtn.setVisibility(View.INVISIBLE);
        String itemTitle = (mDataUri == null) ? getString(R.string.add_new_item_text)
                : getString(R.string.edit_item_text);
        setTitle(itemTitle);
        if (mDataUri != null) {
            getSupportLoaderManager().initLoader(DETAIL_VIEW_LOADER, null, this);
            deleteItemBtn.setVisibility(View.VISIBLE);
            orderItemBtn.setVisibility(View.VISIBLE);
        }


    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case DETAIL_VIEW_LOADER:
                // get all table data sorted by item name
                return new CursorLoader(
                        this,
                        mDataUri,
                        null,
                        null,
                        null,
                        null
                );
            default:
                Log.e(AppConst.TAG, "Failled to recognized loader id for detail view");
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // cursor should only have one item
        if (cursor != null && cursor.moveToFirst()) {
            nameTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow
                    (InventoryDBContract.InventoryEntry.COLUMN_ITEM_NAME)));
            priceTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow
                    (InventoryDBContract.InventoryEntry.COLUMN_PRICE)));
            quantityTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow
                    (InventoryDBContract.InventoryEntry.COLUMN_AVAILABLE_QUANTITY)));
            supplierTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow
                    (InventoryDBContract.InventoryEntry.COLUMN_SUPPLIER)));
            supplierContactsTextView.setText(cursor.getString(cursor.getColumnIndexOrThrow
                    (InventoryDBContract.InventoryEntry.COLUMN_SUPPLIER_CONTACTS)));
            imageByteArray = cursor.getBlob(cursor.getColumnIndexOrThrow
                    (InventoryDBContract.InventoryEntry.COLUMN_IMAGE));
            if (imageByteArray != null && imageByteArray.length > 0) {
                Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
                itemImage.setImageBitmap(imageBitmap);
                itemImage.setVisibility(View.VISIBLE);
            }
        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameTextView.setText(NA_TEXT);
        priceTextView.setText("0.0");
        quantityTextView.setText("0");
        supplierTextView.setText(NA_TEXT);
        supplierContactsTextView.setText(NA_TEXT);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save item ... only exit if success
                if (saveItem()) {
                    finish();
                }
                break;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*use content values to update data*/
    private boolean saveItem() {

        ContentValues contentValues = constructContentValuesFromTexts();

        if (contentValues == null) {
            return false;
        }

        if (mDataUri == null) {
            Log.d(AppConst.TAG, "Add new item");
            getContentResolver().
                    insert(InventoryDBContract.InventoryEntry.CONTENT_URI, contentValues);

        } else {
            Log.d(AppConst.TAG, "Update Item ");
            String selection = InventoryDBContract.InventoryEntry._ID + "=?";
            String[] selecionArgs = {String.valueOf(ContentUris.parseId(mDataUri))};
            getContentResolver().update(mDataUri, contentValues, selection, selecionArgs);
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_SELECT && resultCode == RESULT_OK) {
            try {
                Log.d(AppConst.TAG, "Image Result get Called");
                Uri selectedImage = data.getData();
                InputStream iStream = getContentResolver().openInputStream(selectedImage);
                imageByteArray = getBytes(iStream);
                Bitmap bitmapImage = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.length);
                itemImage.setImageBitmap(bitmapImage);
                itemImage.setVisibility(View.VISIBLE);
            } catch (IOException e) {
                Log.e(AppConst.TAG, "Failed to read image from uri");
                e.printStackTrace();
            }
        }
    }


    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = BUFFER_SIZE;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.delete_btn:
                showDeleteConfirmationDiaglog();
                break;
            case R.id.order_btn:
                launchOrderWithDetails();
                break;
            case R.id.increase_quantity_btn:
            case R.id.decrease_quantity_btn:
                determineQuantity(view);
                break;
            case R.id.select_image_btn:
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, REQUEST_IMAGE_SELECT);
                Log.d(AppConst.TAG, "Start Activity Image Select");
            default:
                Log.e(AppConst.TAG, "Unsupported button. Should not get here");

        }

    }

    private void launchOrderWithDetails() {
        String supplierContact = supplierContactsTextView.getText().toString();
        Intent intent = new Intent();
        if (supplierContact.matches(PHONE_PATTERN)) {
            intent.setAction(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + supplierContact));
            startActivity(intent);
        } else if (supplierContact.matches(EMAIL_PATTERN)) {
            intent.setAction(Intent.ACTION_SEND);
            intent.setType(EMAIM_MIME_TYPE);
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{supplierContact});
            startActivity(intent);
        }
    }


    /**
     * helper to check if contacts email or number is valid
     *
     * @return true only if the text is number of email
     */
    private boolean isSupplierContactValie(String contactInfo) {
        if (contactInfo.matches(EMAIL_PATTERN)) {
            Log.d(AppConst.TAG, "Contact info : " + contactInfo + " is a valid email address");
            return true;
        }

        if (contactInfo.matches(PHONE_PATTERN)) {
            Log.d(AppConst.TAG, "Contact Info : " + contactInfo + " is a valid phone number");
            return true;
        }
        Log.e(AppConst.TAG, "Contact " + contactInfo + " is not a valid format");
        return false;
    }

    /**
     * prompt user to confirm that they want to delete current item
     */
    private void showDeleteConfirmationDiaglog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button so delete Item
                deleteItem();
                ItemDetailViewActivity.this.finish();
                Log.d(AppConst.TAG, "Exit activity!");
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }


        });
        builder.show();
    }

    private ContentValues constructContentValuesFromTexts() {
        ContentValues contentValues = new ContentValues();
        String errorMsg = null;
        String nameText = nameTextView.getText().toString();
        String priceText = priceTextView.getText().toString();
        String quantityText = quantityTextView.getText().toString();
        String supplierText = supplierTextView.getText().toString();
        String supplierContactsText = supplierContactsTextView.getText().toString();

        if (nameText == null || nameText.trim().equalsIgnoreCase("")) {
            showErrorForUpdateDB(getString(R.string.name_error_msg));
            return null;
        }

        // try to parse decimal value
        try {
            errorMsg = getString(R.string.price_error_msg);
            Float.parseFloat(priceText);
            errorMsg = getString(R.string.quantity_error_msg);
            Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            showErrorForUpdateDB(errorMsg);
            Log.e(AppConst.TAG, errorMsg);
            return null;
        }

        if (supplierText == null || supplierText.trim().equalsIgnoreCase("")) {
            showErrorForUpdateDB(getString(R.string.supplier_error_msg));
            return null;
        }

        if (!isSupplierContactValie(supplierContactsText)) {
            showErrorForUpdateDB(getString(R.string.contact_info_error_msg));
            return null;
        }

        contentValues.put(InventoryDBContract.InventoryEntry.COLUMN_ITEM_NAME,
                nameText);
        contentValues.put(InventoryDBContract.InventoryEntry.COLUMN_PRICE,
                priceText);
        contentValues.put(InventoryDBContract.InventoryEntry.COLUMN_AVAILABLE_QUANTITY,
                quantityText);
        contentValues.put(InventoryDBContract.InventoryEntry.COLUMN_SUPPLIER,
                supplierText);
        contentValues.put(InventoryDBContract.InventoryEntry.COLUMN_SUPPLIER_CONTACTS,
                supplierContactsText);

        // image is not required right now
        if (imageByteArray != null) {
            Log.d(AppConst.TAG, "Image saved as blob also");
            contentValues.put(InventoryDBContract.InventoryEntry.COLUMN_IMAGE,
                    imageByteArray);
        }

        return contentValues;
    }

    /**
     * prompt user to confirm that they want to delete current item
     */
    private void showErrorForUpdateDB(String errorMsg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(errorMsg);
        builder.setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button so delete Item
                dialog.cancel();
                Log.d(AppConst.TAG, "Exit Error Dialog activity!");
            }
        });
        builder.show();
    }


    /**
     * perform delete item on db if user confirmed
     */
    private void deleteItem() {
        String selection = InventoryDBContract.InventoryEntry._ID + "=?";
        String[] selecionArgs = {String.valueOf(ContentUris.parseId(mDataUri))};
        getContentResolver().delete(mDataUri, selection, selecionArgs);
        Log.d(AppConst.TAG, "Delete Item is Confirmed. Perform Delete on Database");

    }

    private void determineQuantity(View view) {
        try {
            String currentQuantiyStr = quantityTextView.getText().toString();
            Integer currentQuantity = Integer.parseInt(currentQuantiyStr);
            switch (view.getId()) {
                case R.id.increase_quantity_btn:
                    currentQuantity += 1;
                    break;
                case R.id.decrease_quantity_btn:
                    currentQuantity = (currentQuantity > 0) ? (currentQuantity - 1) : 0;
                    break;
            }
            quantityTextView.setText(currentQuantity + "");

        } catch (NumberFormatException e) {
            // reset to 0 with invalid string
            quantityTextView.setText("0");
        }
    }
}
