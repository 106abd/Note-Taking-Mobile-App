package com.example.noteme;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Map;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private Context mContext;
    private Cursor mCursor;
    private NoteItemClickListener listener;

    public interface NoteItemClickListener {
        void onItemClick(long noteID);
    }

    public NoteAdapter(Context context, Cursor cursor, NoteItemClickListener listener) {
        mContext = context;
        mCursor = cursor;
        this.listener = listener;
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {

        public ConstraintLayout listedNote;
        public TextView nameText, descriptionText, contentText;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            nameText = itemView.findViewById(R.id.listedNoteTitle);
            descriptionText = itemView.findViewById(R.id.listedNoteSubtitle);
            listedNote = itemView.findViewById(R.id.listedNote);

        }
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.listed_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }

        String name = mCursor.getString(mCursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_NAME));
        String description = mCursor.getString(mCursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_DESCRIPTION));
        int color = mCursor.getInt(mCursor.getColumnIndex(NoteContract.NoteEntry.COLUMN_COLOR));

        long noteID = mCursor.getLong(mCursor.getColumnIndex(NoteContract.NoteEntry._ID)); // Note unique ID

        holder.itemView.setTag(noteID);
        holder.nameText.setText(name);
        holder.descriptionText.setText(description);
        holder.listedNote.setBackgroundColor(color);


        // Add click listener to open NewNote activity for editing
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(noteID);
            }
        });


    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (mCursor != null) {
            mCursor.close();
        }

        mCursor = newCursor;

        if (newCursor != null) {
            notifyDataSetChanged();
        }

    }

}
