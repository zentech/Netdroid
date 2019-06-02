package org.pctechtips.netdroid.classes;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import org.pctechtips.netdroid.dbhelper.DatabaseHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Arrays;

public class DownloadTask extends android.os.AsyncTask<Void, Integer, String> {

    public static final String PORT_URL = "http://pctechtips.org/apps/service-names-port-numbers.csv";
    public static final int FILE_LENGTH = 1024;
    public Context context;
    public DatabaseHelper dbHelper;
    public SQLiteDatabase db;
    public ProgressDialog mProgressDialog;

    public DownloadTask(Context context) {
        this.context = context;
        dbHelper = new DatabaseHelper(context);
    }



    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //dialogbox stuff
        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle("Setting Ports Database");
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);
        mProgressDialog.show();
        Toast.makeText(context,"Creating Database!..", Toast.LENGTH_LONG).show();
    }

    @Override
    protected String doInBackground(Void... aVoid) {
        BufferedReader in;
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            java.net.URL url = new java.net.URL(PORT_URL);
            connection = (java.net.HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != java.net.HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            int lineNum = 0;
            while ((line = in.readLine()) != null && lineNum < FILE_LENGTH) {
                String[] data = line.split(",", -1);
                if(data.length != 12) { continue; }
                if(data == null) { continue; }
                if(data[2].equalsIgnoreCase("tcp")) {
                    lineNum++;
                    String service = (data[0].equals(" ")) ? "null" : data[0];
                    int portNum = Integer.parseInt(data[1]);
                    String protocol = data[2];
                    String desc = data[3];
                    if(!dbHelper.insertData(service, portNum, protocol, desc)) {
                        return "database error";
                    }
                    //update dialog box progress in percentage
                    publishProgress((int) (lineNum * 100 / FILE_LENGTH));
                }

            }

        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (java.io.IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress[0]);
        // if we get here, length is known, now set indeterminate to false
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        mProgressDialog.dismiss();
        if (result == null) {
            android.widget.Toast.makeText(context,"Download error: ", android.widget.Toast.LENGTH_LONG).show();
        }
    }
}
