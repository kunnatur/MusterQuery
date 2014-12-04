package org.ccmp.musterquery;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class TrialDbHandler extends SQLiteOpenHelper {

    //The Android's default system path of your application database.

    private static final String DB_NAME = "surguja.sqlite";

    private SQLiteDatabase myDataBase;

    private String dbPath;

    private final Context myContext;

    private static final String LOG_TAG = "org.ccmp.musterquery";

    private static final String[] expectedTables = {"blocks", "panchayats", "jobcardRegister", "musterTransactionDetails"};

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public TrialDbHandler(Context context) {

        super(context, DB_NAME, null, 1);
        this.myContext = context;
        dbPath = context.getDatabasePath("surguja.sqlite").getParentFile().getPath() + "/";
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException {

            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();
            try {

                copyDataBase();

            } catch (IOException e) {

                throw new Error("Error copying database");

            }


    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    public boolean checkDataBase(){
        Log.i(LOG_TAG, "checkDatabase called");
        Toast.makeText(myContext, "checkDatabase called", Toast.LENGTH_SHORT).show();
        SQLiteDatabase checkDB = null;
        boolean expectedTablesExist = true;

        try{
            String myPath = dbPath + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS|SQLiteDatabase.OPEN_READONLY);
        }catch(SQLiteException e){

            //database doesn't exist yet.

        }

        if(checkDB == null){
            return false;
        } else {
            ArrayList<String> tableNames = getTableNames(checkDB);
            for (String table : expectedTables) {
                if (!tableNames.contains(table)) {
                    expectedTablesExist = false;
                    break;
                }
            }
        }
        checkDB.close();

        return expectedTablesExist;


    }

    private ArrayList<String> getTableNames(SQLiteDatabase db) {
        ArrayList<String> tableNames = new ArrayList<String>();
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        if (c.moveToFirst()) {
            while ( !c.isAfterLast() ) {
                tableNames.add( c.getString( c.getColumnIndex("name")) );
                c.moveToNext();
            }
        }
        return tableNames;
    }
    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{

        Log.i(LOG_TAG, "copyDatabase() called");
        Log.i(LOG_TAG, "external directory is : " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
        Toast.makeText(myContext, "checkDatabase called", Toast.LENGTH_SHORT).show();
        //Open your local db as the input stream
        //InputStream myInput = new FileInputStream(new File(Environment.DIRECTORY_DOWNLOADS, DB_NAME));
        InputStream myInput = new FileInputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), DB_NAME));

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(new File(dbPath, DB_NAME));

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
        Log.i(LOG_TAG, "copyDatabase done");

    }

    public void openDataBase() throws SQLException {

        //Open the database
        String myPath = dbPath + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY|SQLiteDatabase.NO_LOCALIZED_COLLATORS);
    }

    @Override
    public synchronized void close() {

        if(myDataBase != null)
            myDataBase.close();

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public List<BlockRecord> getBlocks() {
        List<BlockRecord> blocks = new ArrayList<BlockRecord>();

        // Select All Query
        String selectQuery = "SELECT  * FROM blocks";

        openDataBase();
        Cursor cursor = myDataBase.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                blocks.add(new BlockRecord(cursor.getInt(0), cursor.getString(1), cursor.getString(4)));
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();
        myDataBase.close();

        // returning lables
        return blocks;
    }

    public List<PanchayatRecord> getPanchayats(BlockRecord block) {
        List<PanchayatRecord> panchayats = new ArrayList<PanchayatRecord>();

        // Select All Query
        String selectQuery = "SELECT  * FROM panchayats WHERE blockCode=?";
        //String selectQuery = "SELECT * FROM panchayats";
        Log.i(LOG_TAG, "selectQuery is " + selectQuery);

        openDataBase();
        Cursor cursor = myDataBase.rawQuery(selectQuery, new String[]{block.getBlockCode()});

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                panchayats.add(new PanchayatRecord(cursor.getInt(0), cursor.getString(1), cursor.getString(5), block.getBlockCode()));
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();
        myDataBase.close();

        Log.i(LOG_TAG, "Number of panchayats returned :" + panchayats.size());
        return panchayats;
    }

    public List<JobcardRecord> getJobcards(PanchayatRecord panchayat) {
        List<JobcardRecord> jobcards = new ArrayList<JobcardRecord>();

        // Select All Query
        String selectQuery = "SELECT  * FROM jobcardRegister WHERE panchayatCode=? and blockCode=?";
        //String selectQuery = "SELECT * FROM panchayats";
        Log.i(LOG_TAG, "selectQuery is " + selectQuery);

        openDataBase();
        Cursor cursor = myDataBase.rawQuery(selectQuery, new String[]{panchayat.getPanchayatCode(), panchayat.getBlockCode()});

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String jobcardId = cursor.getString(1);
                String headOfHousehold = cursor.getString(8);
                String issueDate = cursor.getString(10);
                String caste = cursor.getString(11);

                jobcards.add(new JobcardRecord(id,jobcardId,headOfHousehold,issueDate,caste));
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();
        myDataBase.close();

        return jobcards;
    }

    public List<MusterEntry> getMusterEntries(JobcardRecord jobcard) {
        List<MusterEntry> musterEntries = new ArrayList<MusterEntry>();

        // Select All Query
        String selectQuery = "SELECT  * FROM musterTransactionDetails WHERE jobcard=?";
        Log.i(LOG_TAG, "select query : " + selectQuery + " jobcard : " + jobcard.getJobcard());
        openDataBase();
        Cursor cursor = myDataBase.rawQuery(selectQuery, new String[]{jobcard.getJobcard()});


        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(5);
                int daysWorked = cursor.getInt(7);
                int totalWage=cursor.getInt(9);
                String creditStatus = cursor.getString(15);
                String creditDate = cursor.getString(16);

                musterEntries.add(new MusterEntry(id,name,daysWorked,totalWage,creditStatus,creditDate));
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();
        myDataBase.close();

        return musterEntries;
    }

}