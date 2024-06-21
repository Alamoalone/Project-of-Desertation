package com.example.dwelventory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
/**
 * This is the activity that allows the user to input the item's information
 * for the creation of a new item, allows the user to view the item's information,
 * allows the user to edit an item's information, and allows the user to edit the
 * the tags associated with the item.
 * @author Maggie Lacson and Ethan Keys
 * @see MainActivity
 * @see TagFragment
 * */
public class AddEditActivity extends AppCompatActivity implements TagFragment.OnFragmentInteractionListener, PhotoFragment.onPhotoFragmentInteractionListener{
    // All views
    EditText nameButton;
    EditText dateButton;
    EditText makeButton;
    EditText modelButton;
    MaterialButton serialNumButton;
    EditText estValButton;
    EditText commentButton;
    MaterialButton photoButton;
    MaterialButton confirmButton;
    ImageButton editTagButton;
    MaterialButton tagDisplay1Button;
    MaterialButton tagDisplay2Button;
    MaterialButton tagDisplay3Button;
    ImageButton backButton;
    // Required inputs
    private String name;
    private Date date;
    private String make;
    private String model;
    private int estValue;
    private ArrayList<String> photos;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference usersRef;
    private Item item;

    private ArrayList<Tag> tagsToApply;
//    private String comment;
    private String prevName;
    /**
     * This sets up the activity. Either blank if the user is adding an item
     * or with the item's information loaded into the text boxes if the user
     * is viewing or editing the item.
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item_fragment);
        // set up
        editTagButton = findViewById(R.id.edit_tag_button);
        tagDisplay1Button = findViewById(R.id.tag_display_1);
        tagDisplay2Button = findViewById(R.id.tag_display_2);
        tagDisplay3Button  =findViewById(R.id.tag_display_3);
        backButton = findViewById(R.id.edit_activity_back);

        tagDisplay3Button.setVisibility(View.GONE);
        tagDisplay2Button.setVisibility(View.GONE);
        tagDisplay1Button.setVisibility(View.GONE);

        nameButton = findViewById(R.id.item_name_button);
        dateButton = findViewById(R.id.date_button);
        makeButton = findViewById(R.id.make_button);
        modelButton = findViewById(R.id.model_button);
        serialNumButton = findViewById(R.id.serial_number_button);
        estValButton = findViewById(R.id.estimated_val_button);
        commentButton = findViewById(R.id.comment_button);
        photoButton = findViewById(R.id.photo_button);
        confirmButton = findViewById(R.id.confirm_button);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        usersRef = db.collection("users");

        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");
        int position = intent.getIntExtra("position", -1);
        int requestCode = intent.getIntExtra("requestCode", -1);
        String itemRefID = intent.getStringExtra("itemRefID");
        Log.d("itemTag", "RefID after opening activity: " + itemRefID);

        try{
            String serial = intent.getStringExtra("serialNo");
            serialNumButton.setText(serial);
        }catch(Exception e){
            Log.d("D","Serial Number unable to be set when returning from scan activity.");
        }

        if(mode.equals("add")){
            serialNumButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ImageButton serial_no_cam = findViewById(R.id.serial_no_cam);
                    serial_no_cam.setVisibility(View.VISIBLE);

                    serial_no_cam.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent scan_intent  = new Intent(AddEditActivity.this, SerialNumberScan.class);
                            scan_intent.putExtra("name",nameButton.getText());
                            scan_intent.putExtra("date",dateButton.getText());
                            Log.d("Date Test",date.toString());
                            scan_intent.putExtra("mode", mode);
                            scan_intent.putExtra("position", position );
                            scan_intent.putExtra("requestCode", requestCode);
                            //scan_intent.putExtra("previous name", prevName);
                            scan_intent.putExtra("itemRefID", itemRefID);
                            scan_intent.putExtra("tags",tagsToApply);
                            startActivity(scan_intent);
                        }
                    });
                }
            });

            nameButton.setText(intent.getStringExtra("name"));
            makeButton.setText(intent.getStringExtra("make"));


        }

        if (mode.equals("edit")){
            item = (Item) intent.getParcelableExtra("item");

            // fill edit texts with information
            assert item != null;
            prevName = item.getDescription();
            nameButton.setText(prevName);
            Date date = (Date) intent.getSerializableExtra("date");
            tagsToApply = intent.getParcelableArrayListExtra("tags");
            Log.d("# just opened on edit mode", String.valueOf(tagsToApply));
            if(tagsToApply == null){
                tagsToApply = new ArrayList<>();
                Log.d("", "onCreate: SEE HERE 1" + tagsToApply);
            }
            Log.d("", "onCreate: SEE HERE " + tagsToApply);
            item.setTags(tagsToApply);
            ArrayList<String> alreadyAddedPhotos = intent.getStringArrayListExtra("send_photos");
            item.setPhotos(alreadyAddedPhotos);

            // Now display any tags that are already applied. Up to 3
            // Add the Tags indentifiers to the Top right corner of the screen setting up to 3 Tag Names.
            // If a Item has less than 3 Tags then the remaining unused Tag buttons will be hidden.
            int numTags = tagsToApply.size();
            int i = 0;
            while (i <= 2 || i < numTags){
                if (i == 3 || i == numTags){
                    break;
                }
                if (i == 0){
                    tagDisplay1Button.setText(tagsToApply.get(i).getTagName());
                    tagDisplay1Button.setVisibility(View.VISIBLE);
                }
                else if (i == 1){
                    tagDisplay2Button.setText(tagsToApply.get(i).getTagName());
                    tagDisplay2Button.setVisibility(View.VISIBLE);
                }
                else if(i == 2){
                    tagDisplay3Button.setText(tagsToApply.get(i).getTagName());
                    tagDisplay3Button.setVisibility(View.VISIBLE);
                }
                i++;
            }

            assert date != null;
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
            // Format the Date object as a string
            String strDate = dateFormat.format(date);
            dateButton.setText(strDate);
            makeButton.setText(item.getMake());
            modelButton.setText(item.getModel());
            serialNumButton.setText(String.valueOf(item.getSerialNumber()));
            estValButton.setText(String.valueOf(item.getEstValue()));
            commentButton.setText(item.getComment());

            serialNumButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ImageButton serial_no_cam = findViewById(R.id.serial_no_cam);
                    serial_no_cam.setVisibility(View.VISIBLE);

                    serial_no_cam.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent scan_intent  = new Intent(AddEditActivity.this, SerialNumberScan.class);
                            scan_intent.putExtra("item",item);
                            scan_intent.putExtra("date",date);
                            Log.d("Date Test",date.toString());
                            scan_intent.putExtra("mode", mode);
                            scan_intent.putExtra("position", position );
                            scan_intent.putExtra("requestCode", requestCode);
                            scan_intent.putExtra("previous name", prevName);
                            scan_intent.putExtra("itemRefID", itemRefID);
                            scan_intent.putExtra("tags",tagsToApply);

                            startActivity(scan_intent);
                        }
                    });
                }
            });
        }

        // END IF EDIT //

        editTagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mode.equals("edit")) {
                    Log.d("", "onClick: The current tags.." + tagsToApply);
                    TagFragment newFragment = TagFragment.newInstance(mAuth.getUid(),tagsToApply);
                    newFragment.show(getSupportFragmentManager(), "TAG_FRAG");
                }else{
                    TagFragment newFragment = TagFragment.newInstance(mAuth.getUid());
                    newFragment.show(getSupportFragmentManager(), "TAG_FRAG");
                }
            }
        });


        // These on click listeners display a toast with the tag name
        tagDisplay1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                produceTagToast("Tag: " + tagDisplay1Button.getText().toString());
            }
        });

        tagDisplay2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                produceTagToast("Tag: " + tagDisplay2Button.getText().toString());
            }
        });

        tagDisplay3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                produceTagToast("Tag: " + tagDisplay3Button.getText().toString());
            }
        });


        confirmButton.setOnClickListener(v -> {
            // check for valid inputs
            if (reqInputsValid()){
                // take info and make item object
                Log.d("editTag", "before making the new item, date is " + date);
                ArrayList<String> unedittedPhotos = item.getPhotos();
                Item item = new Item(name, date, make, model, estValue);
                if ( tagsToApply == null){
                    tagsToApply = new ArrayList<>();
                };
                Intent intent1 = new Intent();

                item.setTags(tagsToApply);
                item.setPhotos(photos);

                // put it in intent
                Intent updatedIntent = new Intent();
                if(item.getTags() == null){
                  ArrayList<Tag>  emptyTagSet = new ArrayList<>();
                  item.setTags(emptyTagSet);
                }

                if (photos == null || item.getPhotos() == null){
                    photos = new ArrayList<>();
                    item.setPhotos(photos);
                    Log.d("statement", "ENTERED IF STATEMENT" + item.getPhotos());
                    item.setPhotos(unedittedPhotos);
                    Log.d("statement", "ENTERED IF STATEMENT2" + item.getPhotos());
                }
                if (photos.size() == 0 && unedittedPhotos != null){
                    item.setPhotos(unedittedPhotos);
                    Log.d("statement", "ENTERED IF STATEMENT" + item.getPhotos());
                }

                // go back to main activity
                updatedIntent.putStringArrayListExtra("applied_photos",item.getPhotos());
                updatedIntent.putParcelableArrayListExtra("tags",tagsToApply);
                Log.d("# tag TAg hitting confirm", String.valueOf(tagsToApply));
                updatedIntent.putExtra("item", item);
                updatedIntent.putExtra("date", date);
                updatedIntent.putExtra("mode", mode);
                updatedIntent.putExtra("position", position );
                updatedIntent.putExtra("requestCode", requestCode);
                updatedIntent.putExtra("previous name", prevName);
                updatedIntent.putExtra("itemRefID", itemRefID);
                Log.d("itemTag", "RefID coming out of edit activity: " + itemRefID);
                setResult(818, updatedIntent);
                Log.d("aeTag", "finishing aeActivity...");

                finish();
            }
        });

        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode.equals("add")) {
                    PhotoFragment photoFrag = PhotoFragment.newInstance(mAuth.getUid());
                    photoFrag.show(getSupportFragmentManager(),"PHOTO_FRAG");
                }else{
                    PhotoFragment photoFrag = PhotoFragment.newInstance(mAuth.getUid(),item.getPhotos());
                    photoFrag.show(getSupportFragmentManager(),"PHOTO_FRAG");
                }
            }
        });

        backButton.setOnClickListener(v -> {
            // go back to main activity
            finish();
        });


    }

    /**
     * This checks all the required inputs are filled out properly
     * @return true or false whether or not inputs are valid
     */
    private boolean reqInputsValid(){
        boolean valid = true;
        // check name
        name = nameButton.getText().toString().trim();
        if (name.isEmpty()) {
            // handle empty field
            nameButton.setError("Field cannot be empty");
            nameButton.requestFocus();
            valid = false;
        }
        // check date
        String strDate = dateButton.getText().toString();
        if (strDate.isEmpty()) {
            // handle empty field
            dateButton.setError("Field cannot be empty");
            dateButton.requestFocus();
            valid = false;
        }
        if (!isDateValid(strDate)){
            // handle date format
            dateButton.setError("Date format must be (mm-dd-yyyy)");
            dateButton.requestFocus();
            valid = false;
        }
        // check make
        make = makeButton.getText().toString().trim();
        if (make.isEmpty()) {
            // handle empty field
            makeButton.setError("Field cannot be empty");
            makeButton.requestFocus();
            valid = false;
        }
        // check model
        model = modelButton.getText().toString();
        if (model.isEmpty()) {
            // handle empty field
            modelButton.setError("Field cannot be empty");
            modelButton.requestFocus();
            valid = false;
        }
        // check estimated value
        String ev = estValButton.getText().toString();
        if (ev.isEmpty()){
            estValButton.setError("Field cannot be empty");
            estValButton.requestFocus();
            valid = false;
        }
        else{
            estValue = Integer.parseInt(ev);
        }
        // All inputs valid!!!
        return valid;
    }
    /**
     * This checks if the given date is valid
     * @param dateStr (String) the string version of the data given by the user
     * @return true or false depending if the given string was a valid date
     */
    private boolean isDateValid(String dateStr) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
        dateFormat.setLenient(false); // Disallow lenient date parsing
        try {
            date = dateFormat.parse(dateStr);
            return true; // Date is valid
        } catch (ParseException e) {
            return false; // Date is invalid
        }
    }

    /***
     * This overriden method will close the currently opened TagFragment without creating any side
     * effects.
     */
    @Override
    public void onCloseAction() {
        TagFragment tagFragment = (TagFragment) getSupportFragmentManager().findFragmentByTag("TAG_FRAG");
        tagFragment.dismiss();
    }

    /***
     * This method which is overriden applies an ArrayList of Tags to the currently specified Item.
     * If the Item has already been created and is being edited, then the specified Item's ArrayList of
     * current Tags will be updated. Else if the Item is being created for the firts time, we just
     * Set a current ArrayList of Tags which will thus later be applied to an Item.
     * @param applyTags
     */
    @Override
    public void onTagApplyAction(ArrayList<Tag> applyTags) {
        TagFragment tagFragment = (TagFragment) getSupportFragmentManager().findFragmentByTag("TAG_FRAG");
        tagFragment.dismiss();
        Intent intent = getIntent();
        if (tagsToApply == null){
            tagsToApply = new ArrayList<>();
        }
        String mode = intent.getStringExtra("mode");
        TagListEditor editor = new TagListEditor();
        if (mode.equals("add")){
            Log.d("addTag", "in add mode");
            editor.checkSingleItemTagAddition(tagsToApply,applyTags);
        }
        else if (tagsToApply == null || applyTags.size() == 0){
            tagsToApply = new ArrayList<>();
            item.setTags(tagsToApply);
        }else if(mode.equals("edit")){
            editor.checkSingleItemTagAddition(tagsToApply,applyTags);
            item.setTags(tagsToApply);
    }

        tagDisplay3Button.setVisibility(View.GONE);
        tagDisplay2Button.setVisibility(View.GONE);
        tagDisplay1Button.setVisibility(View.GONE);

        // Add the Tags indentifiers to the Top right corner of the screen setting up to 3 Tag Names.
        // If a Item has less than 3 Tags then the remaining unused Tag buttons will be hidden.
        int numTags = tagsToApply.size();
        int i = 0;
        while (i < numTags && i <= 2){
            if (i == 3 || i == numTags) {
                break;
            }
            if (i == 0){
                tagDisplay1Button.setText(tagsToApply.get(i).getTagName());
                tagDisplay1Button.setVisibility(View.VISIBLE);
            }
            else if (i == 1){
                tagDisplay2Button.setText(tagsToApply.get(i).getTagName());
                tagDisplay2Button.setVisibility(View.VISIBLE);
            }
            else if(i == 2){
                tagDisplay3Button.setText(tagsToApply.get(i).getTagName());
                tagDisplay3Button.setVisibility(View.VISIBLE);
            }
            i++;
        }
}

    /***
     * Hook which is not implemented for the AddEditActivity as Tags should not be allowed to be deleted
     * from this Activity
     * @param deletedTag
     *      Wouldve been the Tag instance of the Tag to be deleted.
     */
    @Override
    public void onTagDeletion(Tag deletedTag) {
        return;
    }

    /***
     * Produces a toast for the specified action by accepting a String to be displayed as the toast message
     * @param stringResource
     *      String object representing the String that will be displayed within the customized Toast.
     */
    private void produceTagToast(String stringResource){
        // create a toast with the specified string resource on the appropiate action.
        Toast toast = Toast.makeText(this,stringResource,Toast.LENGTH_SHORT);
        toast.show();
    }

    /***
     * Updates the list of photos of the associated item
     * @param photoPaths
     *      ArrayList of paths of photos of associated item
     */
    @Override
    public void onPhotoConfirmPressed(ArrayList<String> photoPaths) {
        photos = photoPaths;// update the photos!
    }
}
