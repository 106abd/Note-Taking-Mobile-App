package com.example.noteme;

import android.Manifest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLData;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewNote extends AppCompatActivity {

    private Button backButton, doneButton;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 123; // You can choose any unique code.
    private static final int GALLERY_REQ_CODE = 1000;
    private static final int CAMERA_REQ_CODE = 1001;
    private EditText noteTitle, noteSubtitle, noteTextBody;
    private ImageButton imageButton;
    private long noteID;
    private RadioGroup colorSelectGroup;
    private RadioButton colorChoiceButton1, colorChoiceButton2, colorChoiceButton3;
    private int selectedColor;
    private SQLiteDatabase database;
    private Uri imageButtonDisplayPicture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        // Initialize Database
        NoteDBHelper dbHelper = new NoteDBHelper(this);
        database = dbHelper.getWritableDatabase();

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Reference views
        backButton = (Button) findViewById(R.id.newNoteBackButton);
        colorSelectGroup = (RadioGroup) findViewById(R.id.noteColourChoices);
        colorChoiceButton1 = (RadioButton) findViewById(R.id.colourChoice1);
        colorChoiceButton2 = (RadioButton) findViewById(R.id.colourChoice2);
        colorChoiceButton3 = (RadioButton) findViewById(R.id.colourChoice3);
        doneButton = (Button) findViewById(R.id.newNoteDoneButton);
        imageButton = findViewById(R.id.imageButtonDisplay);
        noteTitle = (EditText) findViewById(R.id.noteTitle);
        noteSubtitle = (EditText) findViewById(R.id.noteSubtitle);
        noteTextBody = (EditText) findViewById(R.id.noteTextBody);

        // Retrieve the noteID if it was passed from the Home activity
        noteID = getIntent().getLongExtra("noteID", -1);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }

        // Load existing note if noteID is valid
        if (noteID != -1) {
            loadNote();
        }

        // Back button functionality
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToHomeScreen();
            }
        });

        // Done button functionality
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNote();
                returnToHomeScreen();
            }
        });

        // Image Button functionality
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {imageOptionsMenu();}
        });

        // Select Color Functionality
        selectedColor = ContextCompat.getColor(getApplicationContext(), R.color.colorChoice1); // Default color
        colorSelectGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (colorChoiceButton1.isChecked()) {
                    selectedColor = ContextCompat.getColor(getApplicationContext(), R.color.colorChoice1);
                } else if (colorChoiceButton2.isChecked()) {
                    selectedColor = ContextCompat.getColor(getApplicationContext(), R.color.colorChoice2);
                } else if (colorChoiceButton3.isChecked()) {
                    selectedColor = ContextCompat.getColor(getApplicationContext(), R.color.colorChoice3);
                }
            }
        });

    }

    // Function that starts the Home.java Activity
    public void returnToHomeScreen(){
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }

    private void saveNote() {
        int noteTitleLength = noteTitle.getText().toString().trim().length();

        if (noteTitleLength == 0) {
            Toast.makeText(getBaseContext(), "Error: Could Not Save Note - Title Missing", Toast.LENGTH_LONG).show();
            return;
        } else {

            String noteName = noteTitle.getText().toString();
            String noteDescription = noteSubtitle.getText().toString();
            String noteContent = noteTextBody.getText().toString();

            ContentValues contentValues = new ContentValues();
            contentValues.put(NoteContract.NoteEntry.COLUMN_NAME, noteName);
            contentValues.put(NoteContract.NoteEntry.COLUMN_DESCRIPTION, noteDescription);
            contentValues.put(NoteContract.NoteEntry.COLUMN_CONTENT, noteContent);
            contentValues.put(NoteContract.NoteEntry.COLUMN_COLOR, selectedColor);

            // Check if an image URI is registered
            if (imageButtonDisplayPicture != null) {
                contentValues.put(NoteContract.NoteEntry.COLUMN_IMAGE_URI, imageButtonDisplayPicture.toString());
            }

            if (noteID == -1) { // Insert a new note if noteID is -1 (new note)

                database.insert(NoteContract.NoteEntry.TABLE_NAME, null, contentValues);

            } else { // Update the existing note if noteID is valid

                database.update(
                        NoteContract.NoteEntry.TABLE_NAME,
                        contentValues,
                        NoteContract.NoteEntry._ID + "=?",
                        new String[]{String.valueOf(noteID)}
                );
            }


            noteTitle.getText().clear();
            noteSubtitle.getText().clear();
            noteTextBody.getText().clear();
            imageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.baseline_add_photo_alternate_24));

        }
    }

    private void loadNote() {
        if (noteID != -1) {
            String[] projection = {
                    NoteContract.NoteEntry.COLUMN_NAME,
                    NoteContract.NoteEntry.COLUMN_DESCRIPTION,
                    NoteContract.NoteEntry.COLUMN_CONTENT,
                    NoteContract.NoteEntry.COLUMN_COLOR,
                    NoteContract.NoteEntry.COLUMN_IMAGE_URI
            };

            String selection = NoteContract.NoteEntry._ID + "=?";
            String[] selectionArgs = {String.valueOf(noteID)};

            Cursor cursor = database.query(
                    NoteContract.NoteEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                String noteName = cursor.getString(cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_NAME));
                String noteDescription = cursor.getString(cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_DESCRIPTION));
                String noteContent = cursor.getString(cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_CONTENT));
                int noteColor = cursor.getInt(cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_COLOR));
                String noteImage = cursor.getString(cursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_IMAGE_URI));

                if (noteImage != null) {
                    imageButtonDisplayPicture = Uri.parse(noteImage);
                    imageButton.setImageURI(imageButtonDisplayPicture);
                } else {
                    imageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.baseline_add_photo_alternate_24));
                }


                noteTitle.setText(noteName);
                noteSubtitle.setText(noteDescription);
                noteTextBody.setText(noteContent);

                // Set the selected color based on the loaded note
                selectedColor = noteColor;
                if (noteColor == ContextCompat.getColor(getApplicationContext(), R.color.colorChoice1)) {
                    colorChoiceButton1.setChecked(true);
                } else if (noteColor == ContextCompat.getColor(getApplicationContext(), R.color.colorChoice2)) {
                    colorChoiceButton2.setChecked(true);
                } else if (noteColor == ContextCompat.getColor(getApplicationContext(), R.color.colorChoice3)) {
                    colorChoiceButton3.setChecked(true);
                }
            }

            cursor.close();
        }
    }

    // Function to open the image menu (camera or gallery)
    private void imageOptionsMenu() {
        // Create an array to store the available options (camera and gallery)
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        // Create a dialog to display the options to the user
        AlertDialog.Builder optionsMenu = new AlertDialog.Builder(NewNote.this);
        optionsMenu.setTitle("Add Photo");

        optionsMenu.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    takePicture();
                } else if (options[item].equals("Choose from Gallery")) {
                    selectImage();
                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });

        optionsMenu.show();
    }


    private void takePicture() {
        Intent iCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(iCamera, CAMERA_REQ_CODE);
    }


    // Function tutorial
    private void selectImage() {
        Intent iGallery = new Intent(Intent.ACTION_PICK);
        iGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(iGallery, GALLERY_REQ_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){

            if (requestCode == GALLERY_REQ_CODE){

                imageButtonDisplayPicture = data.getData();
                imageButton.setImageURI(imageButtonDisplayPicture);

            } else if (requestCode == CAMERA_REQ_CODE) {

                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");

                Uri imageUri = bitmapToUri(imageBitmap);
                imageButtonDisplayPicture = imageUri;

                imageButton.setImageURI(imageButtonDisplayPicture);

            }

        }

    }

    private Uri bitmapToUri(Bitmap bitmap) {
        try {
            // Create a temporary file to store the bitmap
            File tempFile = File.createTempFile("image", ".png", getCacheDir());

            // Write the bitmap to the file
            FileOutputStream out = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();

            // Convert the file to a Uri
            return FileProvider.getUriForFile(this, "com.example.noteme.fileprovider", tempFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}