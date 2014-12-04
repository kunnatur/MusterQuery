package org.ccmp.musterquery;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class MusterDatabaseHandler extends SQLiteOpenHelper {
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "sarguja";

    // Labels table name
    private static final String TABLE_BLOCKS = "blocks";

    // Labels Table Columns names
    private static final String KEY_BLOCK_ID = "id";
    private static final String KEY_BLOCK_NAME = "name";

    public MusterDatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Category table create query
        String CREATE_BLOCKS_TABLE = "CREATE TABLE " + TABLE_BLOCKS + "("
                + KEY_BLOCK_ID + " INTEGER PRIMARY KEY," + KEY_BLOCK_NAME + " TEXT)";
        db.execSQL(CREATE_BLOCKS_TABLE);

        String[] dummyValues = {"blockna", "blockbaaa", "foobar"};
        for (String blockName : dummyValues) {
            ContentValues values = new ContentValues();
            values.put(KEY_BLOCK_NAME, blockName);
            // Inserting Row
            db.insert(TABLE_BLOCKS, null, values);
        }
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLOCKS);

        // Create tables again
        onCreate(db);
    }

    /**
     * Inserting new lable into lables table
     * */
    public void insertBlock(String blockName){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_BLOCK_NAME, blockName);

        // Inserting Row
        db.insert(TABLE_BLOCKS, null, values);
        db.close(); // Closing database connection
    }

    /**
     * Getting all labels
     * returns list of labels
     * */
    public List<BlockRecord> getBlocks(){
        List<BlockRecord> blocks = new ArrayList<BlockRecord>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_BLOCKS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                blocks.add(new BlockRecord(cursor.getInt(0), cursor.getString(1), cursor.getString(4)));
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();
        db.close();

        // returning lables
        return blocks;
    }
}
