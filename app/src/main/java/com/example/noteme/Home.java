package com.example.noteme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SearchView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class Home extends AppCompatActivity implements NoteAdapter.NoteItemClickListener{

    private SearchView searchBar;
    private FloatingActionButton newNoteButton;
    private NoteAdapter adapter;
    private RecyclerView noteList;
    private SQLiteDatabase database;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Database
        NoteDBHelper dbHelper = new NoteDBHelper(this);
        database = dbHelper.getWritableDatabase();

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Reference Views
        newNoteButton = (FloatingActionButton) findViewById(R.id.createNewNoteButton);
        noteList = (RecyclerView) findViewById(R.id.savedNotesList);
        searchBar = (SearchView) findViewById(R.id.searchNotesBar);

        // Setup Note List (Recycler View)
        noteList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter(this, getAllNotes("null"), this);
        noteList.setAdapter(adapter); // Set recycler view to display adapter


        // Create search notes functionality
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.swapCursor(getAllNotes(newText));
                return true;
            }
        });


        // Create delete note via swipe functionality
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                deleteNote((long) viewHolder.itemView.getTag());
            }
        }).attachToRecyclerView(noteList);

        // Create new note button functionality
        newNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openNewNoteActivity(-1);
            }
        });

    }

    // Function that starts the NewNote.java activity
    public void openNewNoteActivity(long noteID) {
        Intent intent = new Intent(this, NewNote.class);
        intent.putExtra("noteID", noteID);
        startActivity(intent);

    }

    // Function that returns all the notes stored in the database
    private Cursor getAllNotes(String queryString) {

        String selection;
        String[] selectionArgs;

        if (queryString.equalsIgnoreCase("null")) {
            selection = null;
            selectionArgs = null;
        } else {
            selection = NoteContract.NoteEntry.COLUMN_NAME + " LIKE ?";
            selectionArgs = new String[]{"%" + queryString + "%"};
        }


        return database.query(
                NoteContract.NoteEntry.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                NoteContract.NoteEntry.COLUMN_TIMESTAMP + " DESC"
        );
    }

    // Function that deletes a node
    private void deleteNote(long noteID) {
        database.delete(NoteContract.NoteEntry.TABLE_NAME, NoteContract.NoteEntry._ID + "=" + noteID, null);
        adapter.swapCursor(getAllNotes("null"));
    }

    @Override
    public void onItemClick(long noteID) {
        openNewNoteActivity(noteID);
    }
}