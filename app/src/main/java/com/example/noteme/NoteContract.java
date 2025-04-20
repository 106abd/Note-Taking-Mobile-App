package com.example.noteme;

import android.provider.BaseColumns;

public class NoteContract {

    private NoteContract() {}

    public static final class NoteEntry implements BaseColumns {
        public static final String TABLE_NAME = "noteList";
        public static final String COLUMN_NAME = "noteName";
        public static final String COLUMN_DESCRIPTION = "noteDescription";
        public static final String COLUMN_CONTENT = "noteContent";
        public static final String COLUMN_COLOR = "noteColor";
        public static final String COLUMN_IMAGE_URI = "imageURI";
        public static final String COLUMN_TIMESTAMP = "noteTimestamp";
    }

}
