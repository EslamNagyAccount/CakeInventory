package com.example.android.cakeinventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.cakeinventory.data.CakeContract.CakeEntry;

/**
 * Allows user to create a new cake or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    // Maximum quantity value
    private static final int MAX_ITEM = 1000;

    /**
     * Identifier for the cake data loader
     */
    private static final int EXISTING_CAKE_LOADER = 0;

    /**
     * Content URI for the existing cake (null if it's a new cake)
     */
    private Uri mCurrentCakeUri;

    /**
     * EditText field to enter the cake's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the cake's price
     */
    private EditText mPriceEditText;

    /**
     * TextView field to enter the cake's quantity
     */
    private TextView mQuantityEditText;

    /**
     * EditText to enter cake's supplier's name
     */
    private EditText mSupplierEditText;

    /**
     * EditText to enter cake's supplier's phone number
     */
    private EditText mPhoneEditText;

    /**
     * EditText to enter cake's description
     */
    private EditText mDescriptionEditText;

    private Button mContactSupplierButton;

    private Button mItemRemoveButton;

    private Button mItemAddButton;

    // variable for the cake quantity
    int cakeQuantity ;

    /**
     * EditText field to enter the cake's shape
     */
    private Spinner mShapeSpinner;

    /**
     * Shape of the cake. The possible valid values are in the CakeContract.java file:
     * {@link CakeEntry#SHAPE_UNKNOWN}, {@link CakeEntry#SHAPE_RING},
     * {@link CakeEntry#SHAPE_ROUND}, (@link CakeEntry#SHAPE_SQUARE).
     */
    private int mShape = CakeEntry.SHAPE_UNKNOWN;

    /**
     * Boolean flag that keeps track of whether the cake has been edited (true) or not (false)
     */
    private boolean mCakeHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mCakeHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mCakeHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new cake or editing an existing one.
        Intent intent = getIntent();
        mCurrentCakeUri = intent.getData();

        mContactSupplierButton = findViewById(R.id.call_supplier_button);

        // If the intent DOES NOT contain a cake content URI, then we know that we are
        // creating a new cake.
        if (mCurrentCakeUri == null) {
            // This is a new cake, so change the app bar to say "Add a Cake"
            setTitle(getString(R.string.editor_activity_title_new_cake));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a cake that hasn't been created yet.)
            invalidateOptionsMenu();
            mContactSupplierButton.setVisibility(View.GONE);
        } else {
            // Otherwise this is an existing cake, so change app bar to say "Edit Cake"
            setTitle(getString(R.string.editor_activity_title_edit_cake));

            // Initialize a loader to read the cake data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_CAKE_LOADER, null, this);

            // handles contact supplier button, visible only in editing mode
            mContactSupplierButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL,
                            Uri.parse("tel:" + mPhoneEditText.getText()));
                    startActivity(intent);

                }
            });
        }
        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_cake_name);
        mPriceEditText = findViewById(R.id.edit_cake_price);
        mQuantityEditText = findViewById(R.id.edit_cake_quantity);
        mSupplierEditText = findViewById(R.id.edit_cake_supplier);
        mPhoneEditText = findViewById(R.id.edit_supplier_phone);
        mDescriptionEditText = findViewById(R.id.edit_cake_description);
        mItemAddButton = findViewById(R.id.edit_add_item_button);
        mItemRemoveButton = findViewById(R.id.edit_remove_item_button);
        mShapeSpinner = findViewById(R.id.spinner_shape);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mPhoneEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        mShapeSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the shape of the cake.
     */

    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter shapeSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_shape_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        shapeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mShapeSpinner.setAdapter(shapeSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mShapeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.shape_ring))) {
                        mShape = CakeEntry.SHAPE_RING;
                    } else if (selection.equals(getString(R.string.shape_round))) {
                        mShape = CakeEntry.SHAPE_ROUND;
                    } else if (selection.equals(getString(R.string.shape_square))) {
                        mShape = CakeEntry.SHAPE_SQUARE;
                    } else {
                        mShape = CakeEntry.SHAPE_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mShape = CakeEntry.SHAPE_UNKNOWN;

            }
        });

        mItemAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IncQuantity();
            }
        });
        mItemRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DecQuantity();
            }
        });
    }

    /**
     * Get user input from editor and save cake into database.
     * @return boolean that indicates if the Activity may finish.
     */
    private void saveCake() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String phoneString = mPhoneEditText.getText().toString().trim();
        String descriptionString = mDescriptionEditText.getText().toString().trim();

        // Check if this is supposed to be a new cake
        // and check if all the fields in the editor are blank
        if (mCurrentCakeUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString) && TextUtils.isEmpty(supplierString) &&
                TextUtils.isEmpty(phoneString) && TextUtils.isEmpty(descriptionString) &&
                mShape == CakeEntry.SHAPE_UNKNOWN){
            Toast.makeText(this, getString(R.string.toast_all_fields_empty),
                    Toast.LENGTH_SHORT).show();
            return;
        }

            // check all edit text and quantity field
        if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, R.string.toast_missing_cake_name, Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, R.string.toast_missing_cake_price, Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(supplierString)) {
            Toast.makeText(this, R.string.toast_missing_supplier_name, Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(phoneString)) {
            Toast.makeText(this, R.string.toast_missing_supplier_phone_number, Toast.LENGTH_LONG).show();
            return;
        }
        if (TextUtils.isEmpty(descriptionString)) {
            Toast.makeText(this, R.string.toast_missing_cake_description, Toast.LENGTH_LONG).show();
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and cake attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(CakeEntry.COLUMN_CAKE_NAME, nameString);
        values.put(CakeEntry.COLUMN_CAKE_PRICE, priceString);
        values.put(CakeEntry.COLUMN_CAKE_SHAPE, mShape);
        // If the quantity is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(CakeEntry.COLUMN_CAKE_QUANTITY, quantity);
        values.put(CakeEntry.COLUMN_CAKE_SUPPLIER, supplierString);
        values.put(CakeEntry.COLUMN_CAKE_PHONE, phoneString);
        values.put(CakeEntry.COLUMN_CAKE_DESCRIPTION, descriptionString);

        // Determine if this is a new or existing cake by checking if mCurrentPetUri is null or not
        if (mCurrentCakeUri == null) {
            // This is a NEW cake, so insert a new cake into the provider,
            // returning the content URI for the new cake.
            Uri newUri = getContentResolver().insert(CakeEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_cake_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_cake_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Otherwise this is an EXISTING cake, so update the cake with content URI: mCurrentCakeUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentCakeUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentCakeUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_cake_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_cake_successful),
                        Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    public void IncQuantity() {
        cakeQuantity = Integer.valueOf(mQuantityEditText.getText().toString().trim());
        cakeQuantity = cakeQuantity + 1;
        mQuantityEditText.setText(String.valueOf(cakeQuantity));
    }

    public void DecQuantity() {
        cakeQuantity = Integer.valueOf(mQuantityEditText.getText().toString().trim());
        if (cakeQuantity > 0) {
            cakeQuantity = cakeQuantity - 1;
            mQuantityEditText.setText(String.valueOf(cakeQuantity));
        }else if (cakeQuantity == 0) {
                mItemRemoveButton.setEnabled(false);
            }}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentCakeUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save cake to database
                saveCake();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the cake hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mCakeHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the cake hasn't changed, continue with handling back button press
        if (!mCakeHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all cake attributes, define a projection that contains
        // all columns from the pet table
        String[] projection = {
                CakeEntry._ID,
                CakeEntry.COLUMN_CAKE_NAME,
                CakeEntry.COLUMN_CAKE_SHAPE,
                CakeEntry.COLUMN_CAKE_PRICE,
                CakeEntry.COLUMN_CAKE_QUANTITY,
                CakeEntry.COLUMN_CAKE_SUPPLIER,
                CakeEntry.COLUMN_CAKE_PHONE,
                CakeEntry.COLUMN_CAKE_DESCRIPTION};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentCakeUri,         // Query the content URI for the current pet
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of cake attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(CakeEntry.COLUMN_CAKE_NAME);
            int shapeColumnIndex = cursor.getColumnIndex(CakeEntry.COLUMN_CAKE_SHAPE);
            int priceColumnIndex = cursor.getColumnIndex(CakeEntry.COLUMN_CAKE_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(CakeEntry.COLUMN_CAKE_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(CakeEntry.COLUMN_CAKE_SUPPLIER);
            int phoneColumnIndex = cursor.getColumnIndex(CakeEntry.COLUMN_CAKE_PHONE);
            int descriptionColumnIndex = cursor.getColumnIndex(CakeEntry.COLUMN_CAKE_DESCRIPTION);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int shape = cursor.getInt(shapeColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            int phone = cursor.getInt(phoneColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityEditText.setText(Integer.toString(quantity));
            mSupplierEditText.setText(supplier);
            mPhoneEditText.setText(Integer.toString(phone));
            mDescriptionEditText.setText(description);

            // Shape is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Ring, 2 is Round , 3 is Square).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (shape) {
                case CakeEntry.SHAPE_RING:
                    mShapeSpinner.setSelection(1);
                    break;
                case CakeEntry.SHAPE_ROUND:
                    mShapeSpinner.setSelection(2);
                    break;
                case CakeEntry.SHAPE_SQUARE:
                    mShapeSpinner.setSelection(3);
                    break;
                default:
                    mShapeSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierEditText.setText("");
        mPhoneEditText.setText("");
        mDescriptionEditText.setText("");
        mShapeSpinner.setSelection(0); // Select "Unknown" shape
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the cake.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this cake.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the cake.
                deleteCake();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the cake.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the cake in the database.
     */
    private void deleteCake() {
        // Only perform the delete if this is an existing cake.
        if (mCurrentCakeUri != null) {
            // Call the ContentResolver to delete the cake at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentCakeUri
            // content URI already identifies the cake that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentCakeUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_cake_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_cake_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}