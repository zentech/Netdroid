package org.pctechtips.netdroid.dbhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by jlvaz on 4/12/2017.
 * Database Helper file
 */


public class DatabaseHelper extends SQLiteOpenHelper {
    //The Android's default system path of your application database.
    public static String DB_PATH = "";
    public static String DB_NAME = "netdb.db";
    private static String TABLE_NAME = "ports";
    private static int DB_VERSION = 1;
    /*SQL Query to get service running on especific port*/
    private static String SQL_QUERY = "SELECT description FROM ports WHERE port==";
    private static String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (service TEXT, port INT(11), protocol TEXT, description TEXT);";
    private SQLiteDatabase mDataBase;
    private static DatabaseHelper dbSingleton;
    private final Context context;


    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.     *
     * @param context
     */
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        DB_PATH = context.getDatabasePath(DB_NAME).getPath();
        this.context = context;
    }

    public static DatabaseHelper getDatabaseInstance(Context ctxt) {
        if(dbSingleton == null) {
            dbSingleton = new DatabaseHelper(ctxt);
        }
        return dbSingleton;
    }

     public void createDataBase() throws IOException {
        if(!checkDataBase()) {
            this.getReadableDatabase();
            copyDataBase();
            this.close();
        }
    }

    public boolean checkDataBase() {
        File DbFile = new File(DB_PATH + DB_NAME);
        String databasePath = context.getDatabasePath(DB_NAME).getPath();
        return DbFile.exists();
    }

    public boolean openDataBase() {
        mDataBase = SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.CREATE_IF_NECESSARY);
        return mDataBase != null;
    }

    public synchronized void close(){
        if(mDataBase != null)
            mDataBase.close();
        SQLiteDatabase.releaseMemory();
        super.close();
    }

    /**
     * create port table in netdb
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    /**
     * add record to port table on netdb
     * @param service
     * @param port
     * @param proto
     * @param desc
     * @return
     */
    public boolean insertData(String service, int port, String proto, String desc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put("service", service);
        value.put("port", port);
        value.put("protocol", proto);
        value.put("description", desc);
        long result = db.insert(TABLE_NAME, null, value);
        return result != -1;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    /**
     * copy database to app location
     * @throws IOException
     */
    private void copyDataBase() throws IOException {
        InputStream mInput =  context.getAssets().open(DB_NAME);
        String outfileName = DB_PATH;
        OutputStream mOutput = new FileOutputStream(outfileName);
        byte[] buffer = new byte[1024];
        int mLength;
        while ((mLength = mInput.read(buffer))>0) {
            mOutput.write(buffer, 0, mLength);
        }
        mOutput.flush();
        mInput.close();
        mOutput.close();
    }

    /*
    * SQL query to get servive description
    * of especific port
    * */
    public String getPortService(int port) {
        String service = "";
        SQLiteDatabase db = this.getReadableDatabase();
        String sqlQuery = SQL_QUERY + port + ";";
        Cursor cursor = db.rawQuery(sqlQuery,null);
        if (cursor.getCount() > 0) {
            cursor.moveToLast();
            service = cursor.getString(0);
        }
        // make sure to close the cursor
        cursor.close();
        return (service != "")? service : "Unknown!";
    }

}