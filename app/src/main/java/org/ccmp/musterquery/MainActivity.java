package org.ccmp.musterquery;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.SQLException;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {

    private Spinner blockSpinner;
    private Spinner panchayatSpinner;
    private Spinner jobcardSpinner;
    private TextView jobcardTextView;
    private TableLayout musterTableLayout;
    private MusterDatabaseHandler musterDbHandler;
    private TrialDbHandler trialDbHandler;
    private Typeface hindiFont;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hindiFont = Typeface.createFromAsset(getAssets(), "fonts/Ananda Lipi Bold Cn Bt.ttf");

        blockSpinner = (Spinner) findViewById(R.id.block_spinner);
        panchayatSpinner = (Spinner) findViewById(R.id.panchayat_spinner);
        jobcardSpinner = (Spinner) findViewById(R.id.jobcard_spinner);
        jobcardTextView = (TextView) findViewById(R.id.jobcard_details_textview);
        musterTableLayout = (TableLayout) findViewById(R.id.muster_table);
        musterDbHandler = new MusterDatabaseHandler(getApplicationContext());
        trialDbHandler = new TrialDbHandler(getApplicationContext());
        checkDbAndInitSpinners(getApplicationContext());
        setupSpinnerListeners();

    }

    public void checkDbAndInitSpinners(Context ctx) {
        if (!trialDbHandler.checkDataBase()) {
            Toast.makeText(ctx,
                    "check database failed. Please download database", Toast.LENGTH_SHORT).show();
            blockSpinner.setEnabled(false);
            panchayatSpinner.setEnabled(false);
            jobcardSpinner.setEnabled(false);
        } else {
            Toast.makeText(ctx, "check database succeeded", Toast.LENGTH_SHORT).show();
            initializeBlockSpinner();
            initializePanchayatSpinner();
            initializeJobcardSpinner();
            blockSpinner.setEnabled(true);
            panchayatSpinner.setEnabled(true);
            jobcardSpinner.setEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void initializeBlockSpinner() {

        // Spinner Drop down elements
        List<BlockRecord> blocks = trialDbHandler.getBlocks();

        // Creating adapter for spinner
        ArrayAdapter<BlockRecord> dataAdapter = new ArrayAdapter<BlockRecord>(this,
                android.R.layout.simple_spinner_item, blocks);

        // Drop down layout style - list view with radio button
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        blockSpinner.setAdapter(dataAdapter);
    }

    public void initializePanchayatSpinner() {
        List<PanchayatRecord> items = new ArrayList<PanchayatRecord>();
        ArrayAdapter<PanchayatRecord> adapter = new ArrayAdapter<PanchayatRecord>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        panchayatSpinner.setAdapter(adapter);
        panchayatSpinner.setEnabled(false);
    }

    public void initializeJobcardSpinner() {
        List<JobcardRecord> items = new ArrayList<JobcardRecord>();
        ArrayAdapter<JobcardRecord> adapter = new ArrayAdapter<JobcardRecord>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        jobcardSpinner.setAdapter(adapter);
        jobcardSpinner.setEnabled(false);
    }

    public void setupSpinnerListeners() {
        blockSpinner.setOnItemSelectedListener(new BlockOnItemSelectedListener());
        panchayatSpinner.setOnItemSelectedListener(new PanchayatOnItemSelectedListener());
        jobcardSpinner.setOnItemSelectedListener(new JobcardOnItemSelectedListener());
    }

    public void downloadDatabase(View view) {
        String filename = "surguja.sqlite";
        Log.i("org.ccmp.musterquery", "download database called");
        File tmpFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
        if (tmpFile.exists()) {
            Log.i("org.ccmp.musterquery", "Deleting old file");
            boolean result = tmpFile.delete();
            Log.i("org.ccmp.musterquery", "File deletion result = " + result);
        }
        String url = "http://74.207.247.189/nreg-static/" + filename;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription("Downloading " + filename);
        request.setTitle("MusterQuery: NREGA database download");
        registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        registerReceiver(onNotificationClick,
                new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
        // in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

        // get download service and enqueue file
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        long id = manager.enqueue(request);
        Toast.makeText(view.getContext(), "Started download ", Toast.LENGTH_SHORT).show();

    }

    public void showMusterDetails(View view) {
        musterTableLayout.removeAllViews();
        //Get muster entries
        JobcardRecord jobcard = (JobcardRecord) jobcardSpinner.getSelectedItem();
        if (jobcard == null) {
            return;
        }
        List<MusterEntry> musterEntries = trialDbHandler.getMusterEntries(jobcard);

        //Show table header
        addMusterHeader();
        //Display muster entries
        for (MusterEntry musterEntry : musterEntries) {
            addMusterRow(musterEntry);
        }
    }

    private void addMusterHeader() {
        TableRow tr = new TableRow(this);
        tr.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        addMusterColumn(tr, "Name");
        addMusterColumn(tr, "Days Worked");
        addMusterColumn(tr, "Total Wages");
        addMusterColumn(tr, "Status");
        addMusterColumn(tr, "Credited Date");
        musterTableLayout.addView(tr, new TableLayout.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));
    }
    private void addMusterRow(MusterEntry musterEntry) {
        /** Create a TableRow dynamically **/
        TableRow tr = new TableRow(this);
        TableRow.LayoutParams layoutparams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        //layoutparams.setMargins(0,0,2,0); --not showing borders
        tr.setLayoutParams(layoutparams);
        addHindiMusterColumn(tr, musterEntry.getName());
        Log.i("org.ccmp.musterquery", "Name is :" + escapeNonAscii(musterEntry.getName()));
        addMusterColumn(tr, "" + musterEntry.getDaysWorked());
        addMusterColumn(tr, ""+musterEntry.getTotalWage());
        addMusterColumn(tr, ""+musterEntry.getCreditStatus());
        addMusterColumn(tr, ""+musterEntry.getCreditedDate());
        musterTableLayout.addView(tr, new TableLayout.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT));

    }

    private static String escapeNonAscii(String str) {

        StringBuilder retStr = new StringBuilder();
        for(int i=0; i<str.length(); i++) {
            int cp = Character.codePointAt(str, i);
            int charCount = Character.charCount(cp);
            if (charCount > 1) {
                i += charCount - 1; // 2.
                if (i >= str.length()) {
                    throw new IllegalArgumentException("truncated unexpectedly");
                }
            }

            if (cp < 128) {
                retStr.appendCodePoint(cp);
            } else {
                retStr.append(String.format("\\u%x", cp));
            }
        }
        return retStr.toString();
    }

    private void addMusterColumn(TableRow tr, String text) {
        TextView col = new TextView(this);
        col.setText(text);
        col.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        col.setPadding(5, 5, 5, 5);
        tr.addView(col);
    }

    private void addHindiMusterColumn(TableRow tr, String text) {
        TextView col = new TextView(this);
        col.setTypeface(hindiFont);
        col.setText(text);
        col.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        col.setPadding(5, 5, 5, 5);
        tr.addView(col);
    }

    private void updateJobcardData(PanchayatRecord panchayat) {
        ArrayAdapter<JobcardRecord> jobcardList = (ArrayAdapter<JobcardRecord>)jobcardSpinner.getAdapter();
        jobcardList.clear();
        jobcardList.addAll(trialDbHandler.getJobcards(panchayat));
        jobcardSpinner.setEnabled(true);
        jobcardSpinner.setSelection(0);
        jobcardList.notifyDataSetChanged();
    }

    private void updateJobcardDetails(JobcardRecord jobcard) {
        String text = "Jobcard Details: \n";
        text += "Head of Household: " + jobcard.getHeadOfHousehold() + "   ";
        text += "Issue Date: " + jobcard.getIssueDate() + "   ";
        text += "Caste: " + jobcard.getCaste();
        jobcardTextView.setText(text);
        musterTableLayout.removeAllViews();
    }

    public void copyDatabase(Context ctx)
    {

        Toast.makeText(ctx, "creating trialDbhandler", Toast.LENGTH_SHORT).show();

        TrialDbHandler trialHandler = new TrialDbHandler(ctx);
        try {

            trialHandler.createDataBase();

        } catch (IOException ioe) {

            throw new Error("Unable to create database: " + ioe.getMessage());

        }

        try {

            List<BlockRecord> blocks = trialHandler.getBlocks();
            for(BlockRecord b: blocks) {
                Log.i("org.ccmp.musterQuery","Block entry: " + b);
            }
        }catch(SQLException sqle){

            throw sqle;

        }

        Toast.makeText(ctx, "trialDbhandler initialized", Toast.LENGTH_SHORT).show();

    }

    BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context ctx, Intent intent) {
            Log.i("org.ccmp.musterquery", "onComplete Called");
            Toast.makeText(ctx, "download done", Toast.LENGTH_SHORT).show();
            copyDatabase(ctx);
            checkDbAndInitSpinners(ctx);
        }
    };

    BroadcastReceiver onNotificationClick=new BroadcastReceiver() {
        public void onReceive(Context ctx, Intent intent) {
            Toast.makeText(ctx, "Ummmm...hi!", Toast.LENGTH_LONG).show();
        }
    };

    private class BlockOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            ArrayAdapter<PanchayatRecord> panchayatList = (ArrayAdapter<PanchayatRecord>)panchayatSpinner.getAdapter();
            panchayatList.clear();

            BlockRecord block = (BlockRecord) parent.getItemAtPosition(pos);
            panchayatList.addAll(trialDbHandler.getPanchayats(block));
            panchayatList.notifyDataSetChanged();
            panchayatSpinner.setSelection(0);
            updateJobcardData(panchayatList.getItem(0));
        }


        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }

    private class PanchayatOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            PanchayatRecord panchayat = (PanchayatRecord) parent.getItemAtPosition(pos);
            updateJobcardData(panchayat);
            updateJobcardDetails((JobcardRecord)jobcardSpinner.getAdapter().getItem(0));
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }

    private class JobcardOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            JobcardRecord jobcard = (JobcardRecord)parent.getItemAtPosition(pos);
            updateJobcardDetails(jobcard);

        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }

    }

}
