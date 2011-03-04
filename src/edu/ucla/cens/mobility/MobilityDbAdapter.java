package edu.ucla.cens.mobility;

import java.util.ArrayList;
import java.util.Random;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.andwellness.R;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

public class MobilityDbAdapter
{
	public static final String KEY_MODE = "mode";
	public static final String KEY_SPEED = "speed";
	public static final String KEY_STATUS = "status";
	public static final String KEY_LOC_TIMESTAMP = "location_timestamp";
	public static final String KEY_ACCURACY = "accuracy";
	public static final String KEY_PROVIDER = "provider";
	public static final String KEY_WIFIDATA = "wifi_data";
	public static final String KEY_ACCELDATA = "accel_data";
	public static final String KEY_TIMEZONE = "timezone";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_TIME = "time";
	public static final String KEY_LATITUDE = "latitude";
	public static final String KEY_LONGITUDE = "longitude";
	private static boolean databaseOpen = false;
	private static Object dbLock = new Object();
	public static final String TAG = "awDB";
	private DatabaseHelper dbHelper;
	private static SQLiteDatabase db = null;

	private Context mCtx = null;

	// This variable should ONLY be set in the constructor. If it is set
	// anywhere else
	// in this class, then this could cause DatabaseHelper to be out of sync,
	// because it
	// will not necessarily get that updated name.
	private String database_table = "";
	private static final int DATABASE_VERSION = 1;
	SharedPreferences settings;
	SharedPreferences.Editor editor;
	private static final String DATABASE_CREATE = "create table %s (" + KEY_ROWID + " integer primary key autoincrement," + KEY_MODE + " text not null," + KEY_SPEED + " text not null," + KEY_STATUS
			+ " text not null," + KEY_LOC_TIMESTAMP + " text not null," + KEY_ACCURACY + " text not null," + KEY_PROVIDER + " text not null," + KEY_WIFIDATA + " text not null," + KEY_ACCELDATA
			+ " text not null," + KEY_TIME + " integer not null," + KEY_TIMEZONE + " text not null," + KEY_LATITUDE + " text," + KEY_LONGITUDE + " text" + ");";

	public class DBRow extends Object
	{
		public long rowValue;
		public String statusValue;
		public String locTimeValue;
		public String accuracyValue;
		public String providerValue;
		public String wifiDataValue;
		public String accelDataValue;
		public String modeValue;
		public String speedValue;
//		public String varianceValue;
//		public String averageValue;
//		public String fftValue;
		public long timeValue;
		public String timezoneValue;
		public String latitudeValue;
		public String longitudeValue;
	}

	private class DatabaseHelper extends SQLiteOpenHelper
	{
		// Stores the name of the database table that is used in the parent
		// class
		private String table = "";

		DatabaseHelper(Context ctx, String table)
		{
			super(ctx, table, null, DATABASE_VERSION);
			this.table = table;
			Log.d(TAG, "Calling constructor: Creating database name: " + table);
		}

		@Override
		public void onCreate(SQLiteDatabase db)
		{
			Log.d(TAG, "onCreate: Creating database table: " + table);
			db.execSQL(String.format(DATABASE_CREATE, table));
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{

			db.execSQL("DROP TABLE EXISTS " + table);
			onCreate(db);
		}
	}

	private static Lock lock = new ReentrantLock();

	/**
	 * Connects to the database, gets the information requested based on the
	 * parameters, closes the connection to the database, and returns the Cursor
	 * object that the query generated.
	 * 
	 * @param columns
	 *            A String array representing the columns that are being
	 *            requested.
	 * 
	 * @param selection
	 *            A SQLite WHERE clause without the "WHERE" keyword.
	 * 
	 * @param selectionArgs
	 *            A String array used for replacing "?"s in the 'selection'
	 *            String with values. The number of items in the array should be
	 *            equal to the number of "?"s in the 'selection' String. 'null'
	 *            is an acceptable value if there are no "?"s in the 'selection'
	 *            String.
	 * 
	 * @param orderBy
	 *            A SQLite "ORDER BY" clause without the "ORDER BY" keyword.
	 * 
	 * @return A Cursor object pointing to all the elements that satisfy the the
	 *         parameters. If a SQLite exception occurs, null is returned.
	 */
	public synchronized Cursor getMobilityCursor(String[] columns, String selection, String[] selectionArgs, String orderBy)
	{
		lock.lock();

		Cursor c;
		try
		{
			Log.i(TAG, (dbHelper == null) + " is stupid");
			SQLiteDatabase sdb = new DatabaseHelper(mCtx, database_table).getReadableDatabase();

			c = sdb.query("mobility", columns, selection, selectionArgs, null, null, orderBy);
		} catch (SQLiteException e)
		{
			Log.i(TAG, e.toString());
			c = null;
		}

		lock.unlock();

		return c;
	}

	public MobilityDbAdapter(Context ctx, String table)
	{
		settings = ctx.getSharedPreferences(ctx.getString(R.string.prefs), 0);
		mCtx = ctx;
		database_table = table;
	}

	public MobilityDbAdapter(Context ctx, String table, String serverDB, String type)
	{
		settings = ctx.getSharedPreferences(ctx.getString(R.string.prefs), 0);
		mCtx = ctx;
		database_table = table;
		try
		{
			registerTable(table, serverDB, type);
		} catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private long random()
	{
		Random gen = new Random(System.currentTimeMillis());
		return gen.nextInt(Integer.MAX_VALUE / 2);

	}

	private synchronized void registerTable(String tableName, String serverDB, String type) throws Exception
	{
		editor = settings.edit();
		if (!settings.contains(tableName))
		{
			int numTables = settings.getInt("numTables", 0);
			editor.putLong(tableName, random());
			editor.putString(tableName + "_serverDB", serverDB);
			editor.putString(tableName + "_type", type);
			numTables++;
			editor.putString("Table" + numTables, tableName);
			editor.putInt("numTables", numTables);
			Log.d(TAG, "Now numTables is " + numTables);
			Log.d(TAG, "tableName is " + tableName);
			Log.d(TAG, "registerTable: registered " + tableName + "_serverDB = " + serverDB);
			Log.d(TAG, "registerTable: registered " + tableName + "_type = " + type);
			Log.d(TAG, "registerTable: registered Table" + numTables + " = " + tableName);
			editor.commit();
			dbHelper = new DatabaseHelper(mCtx, database_table);
		} else if (!settings.getString(tableName + "_serverDB", "").equals(serverDB)) // update
																						// database
		{
			editor.putString(tableName + "_serverDB", serverDB);
			editor.commit();
		}
	}

	public MobilityDbAdapter open() throws SQLException
	{
		synchronized (dbLock)
		{
			while (databaseOpen)
			{
				try
				{
					dbLock.wait();
				} catch (InterruptedException e)
				{
				}

			}
			databaseOpen = true;
			dbHelper = new DatabaseHelper(mCtx, database_table);
			try
			{
				db = dbHelper.getWritableDatabase();
				Log.d(TAG, "Trying to getWriteableDatabase: " + db.toString());
			} catch (SQLiteException e)
			{
				Log.e(TAG, "Could not open database: " + database_table);
				return null;
			}
			return this;
		}
	}

	public void close()
	{
		synchronized (dbLock)
		{
			// if (dbHelper != null)
			dbHelper.close();
			databaseOpen = false;
			dbLock.notify();
		}
	}

	public long createRow(String mode, long time, String status, String speed, long timestamp, String accuracy, String provider, String wifiData, Vector<ArrayList<Double>> samples, String latitude,
			String longitude)
	{
		ContentValues vals = new ContentValues();
		if (db == null)
		{
			Log.e(TAG, "ERROR, database table: " + database_table + " was not initialized!");
			return -1;
		}

		else if (db.inTransaction())
		{
			Log.e(TAG, "ERROR, database in transaction, why is this happening?");
			return -1;
		}
		String timezone = TimeZone.getDefault().getID();
		editor.putLong(database_table, 1);
		editor.commit();
		vals.put(KEY_MODE, mode);
		vals.put(KEY_SPEED, speed);
		vals.put(KEY_STATUS, status);
		vals.put(KEY_LOC_TIMESTAMP, timestamp);
		vals.put(KEY_ACCURACY, accuracy);
		vals.put(KEY_PROVIDER, provider);
		vals.put(KEY_WIFIDATA, wifiData);
		vals.put(KEY_ACCELDATA, formatAccelData(samples));
		vals.put(KEY_TIME, time);
		vals.put(KEY_TIMEZONE, timezone);
		vals.put(KEY_LATITUDE, latitude);
		vals.put(KEY_LONGITUDE, longitude);
		Log.d(TAG, "createRow: adding to table: " + database_table + ": " + mode);
		long rowid = db.insert(database_table, null, vals);
		return rowid;
	}
	
	private String formatAccelData(Vector<ArrayList<Double>> samples)
	{
		JSONArray ja = new JSONArray();
		for (int i = 0; i < samples.get(0).size(); i++)
		{
			JSONObject jo = new JSONObject();
			try
			{
				jo.put("x", samples.get(0).get(i));
				jo.put("y", samples.get(1).get(i));
				jo.put("z", samples.get(2).get(i));
			} catch (JSONException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch(IndexOutOfBoundsException ie)
			{
				Log.e(TAG, "X has " + samples.get(0).size() + ", Y has " + samples.get(1).size() + ", Z has " + samples.get(2).size());
				throw ie; // want crash to alert me
			}
			ja.put(jo);
		}
		
		return ja.toString();
	}

	// public long createRow(String subtype, String mode, long time, String
	// latitude, String longitude)
	// {
	// ContentValues vals = new ContentValues();
	// if (db == null) {
	// Log.e(TAG, "ERROR, database table: " + database_table +
	// " was not initialized!");
	// return -1;
	// }
	//
	// else if (db.inTransaction()) {
	// Log.e(TAG, "ERROR, database in transaction, why is this happening?");
	// return -1;
	// }
	//
	//
	// editor.putLong(database_table, 1);
	// editor.commit();
	// vals.put(KEY_MODE, mode);
	// vals.put(KEY_TIME, time);
	// vals.put(KEY_LATITUDE, latitude);
	// vals.put(KEY_LONGITUDE, longitude);
	// Log.d(TAG, "createRow: adding to table: " + database_table + ": " +
	// mode);
	// long rowid = db.insert(database_table, null, vals);
	// return rowid;
	// }

	/**
	 * Delete rows that are older than the timestamp. Used for garbage
	 * collection.
	 */
	public int deleteSomeRows(long timestamp)
	{
		int dels = 0;

		// try
		{
			Log.d(TAG, "fetchSomeRows from table: " + database_table);
			dels = db.delete(database_table, KEY_TIME + "<= ?", new String[] { "" + timestamp });
		}
		// catch (Exception e)
		// {
		// Log.e(TAG, e.getMessage());
		// }
		return dels;
	}

	public boolean deleteRow(long rowId)
	{
		int count = 0;
		count = db.delete(database_table, KEY_ROWID + "=" + rowId, null);
		Log.d(TAG, "deleteRow: deleting row: " + rowId + "from table: " + database_table);
		if (count > 0)
			return true;
		else
			return false;
	}
	
	public ArrayList<DBRow> fetchSomeRows(Integer numLines)
	{
		ArrayList<DBRow> ret = new ArrayList<DBRow>();

		try
		{
			Log.d(TAG, "fetchSomeRows from table: " + database_table);
			Cursor c = db.query(database_table, new String[] { KEY_ROWID, KEY_MODE, KEY_SPEED, KEY_STATUS, KEY_LOC_TIMESTAMP, KEY_ACCURACY, KEY_PROVIDER, KEY_WIFIDATA, KEY_ACCELDATA, KEY_TIME, KEY_TIMEZONE, KEY_LATITUDE, KEY_LONGITUDE }, null, null,
					null, null, null, numLines.toString());
			int numRows = c.getCount();
			Log.d(TAG, numRows + " rows in the table\n");
			c.moveToFirst();
			for (int i = 0; i < numRows; ++i)
			{
				DBRow row = new DBRow();
				row.rowValue = c.getLong(0);
				row.modeValue = c.getString(1);
				row.speedValue = c.getString(2);
				row.statusValue = c.getString(3);
				row.locTimeValue = c.getString(4);
				row.accuracyValue = c.getString(5);
				row.providerValue = c.getString(6);
				row.wifiDataValue = c.getString(7);
				row.accelDataValue = c.getString(8);
//				row.varianceValue = c.getString(9);
//				row.averageValue = c.getString(10);
//				row.fftValue = c.getString(11);
				row.timeValue = c.getLong(9);
				row.timezoneValue = c.getString(10);
				if (c.isNull(11))
					row.latitudeValue = null;
				else
					row.latitudeValue = c.getString(11);
				if (c.isNull(12))
					row.longitudeValue = null;
				else
					row.longitudeValue = c.getString(12);
				ret.add(row);
				c.moveToNext();
			}
			c.close();
		} catch (Exception e)
		{
			Log.e(TAG, e.getMessage());
		}
		return ret;
	}

	public ArrayList<DBRow> fetchSomeRows(long timeLimit)
	{
		ArrayList<DBRow> ret = new ArrayList<DBRow>();

		try
		{
			Log.d(TAG, "fetchSomeRows from table: " + database_table);
			Cursor c = db.query(database_table, new String[] { KEY_ROWID, KEY_MODE, KEY_SPEED, KEY_STATUS, KEY_LOC_TIMESTAMP, KEY_ACCURACY, KEY_PROVIDER, KEY_WIFIDATA, KEY_ACCELDATA, KEY_TIME, KEY_TIMEZONE, KEY_LATITUDE, KEY_LONGITUDE }, KEY_TIME
					+ " < " + timeLimit, null, null, null, null, null);
			int numRows = c.getCount();
			Log.d(TAG, numRows + " rows in the table\n");
			c.moveToFirst();
			for (int i = 0; i < numRows; ++i)
			{
				DBRow row = new DBRow();
				row.rowValue = c.getLong(0);
				row.modeValue = c.getString(1);
				row.speedValue = c.getString(2);
				row.statusValue = c.getString(3);
				row.locTimeValue = c.getString(4);
				row.accuracyValue = c.getString(5);
				row.providerValue = c.getString(6);
				row.wifiDataValue = c.getString(7);
				row.accelDataValue = c.getString(8);
//				row.varianceValue = c.getString(9);
//				row.averageValue = c.getString(10);
//				row.fftValue = c.getString(11);
				row.timeValue = c.getLong(9);
				row.timezoneValue = c.getString(10);
				if (c.isNull(11))
					row.latitudeValue = null;
				else
					row.latitudeValue = c.getString(11);
				if (c.isNull(12))
					row.longitudeValue = null;
				else
					row.longitudeValue = c.getString(12);
				ret.add(row);
				c.moveToNext();
			}
			c.close();
		} catch (Exception e)
		{
			Log.e(TAG, e.getMessage());
		}
		return ret;
	}

	public ArrayList<DBRow> fetchAllRows()
	{
		ArrayList<DBRow> ret = new ArrayList<DBRow>();

		try
		{
			Log.d(TAG, "fetchSomeRows from table: " + database_table);
			Cursor c = db.query(database_table, new String[] { KEY_ROWID, KEY_MODE, KEY_SPEED, KEY_STATUS, KEY_LOC_TIMESTAMP, KEY_ACCURACY, KEY_PROVIDER, KEY_WIFIDATA, KEY_ACCELDATA, KEY_TIME, KEY_TIMEZONE, KEY_LATITUDE, KEY_LONGITUDE }, null, null, null, null,
					null, null);
			int numRows = c.getCount();
			Log.d(TAG, numRows + " rows in the table\n");
			c.moveToFirst();
			for (int i = 0; i < numRows; ++i)
			{
				DBRow row = new DBRow();

				row.rowValue = c.getLong(0);
				row.modeValue = c.getString(1);
				row.speedValue = c.getString(2);
				row.statusValue = c.getString(3);
				row.locTimeValue = c.getString(4);
				row.accuracyValue = c.getString(5);
				row.providerValue = c.getString(6);
				row.wifiDataValue = c.getString(7);
				row.accelDataValue = c.getString(8);
//				row.varianceValue = c.getString(9);
//				row.averageValue = c.getString(10);
//				row.fftValue = c.getString(11);
				row.timeValue = c.getLong(9);
				row.timezoneValue = c.getString(10);
				if (c.isNull(11))
					row.latitudeValue = null;
				else
					row.latitudeValue = c.getString(11);
				if (c.isNull(12))
					row.longitudeValue = null;
				else
					row.longitudeValue = c.getString(12);
				ret.add(row);
				c.moveToNext();
			}
			c.close();
		} catch (Exception e)
		{
			Log.e(TAG, e.getMessage());
		}
		return ret;
	}

	public DBRow fetchRow(long rowId) throws SQLException
	{
		Cursor c = db.query(database_table, new String[] { KEY_ROWID, KEY_MODE, KEY_SPEED, KEY_STATUS, KEY_LOC_TIMESTAMP, KEY_ACCURACY, KEY_PROVIDER, KEY_WIFIDATA, KEY_ACCELDATA, KEY_TIME, KEY_TIMEZONE, KEY_LATITUDE, KEY_LONGITUDE }, KEY_ROWID + "=" + rowId,
				null, null, null, null);
		DBRow ret = new DBRow();

		if (c != null)
		{
			c.moveToFirst();

			ret.rowValue = c.getLong(0);
			ret.modeValue = c.getString(1);
			ret.speedValue = c.getString(2);
			ret.statusValue = c.getString(3);
			ret.locTimeValue = c.getString(4);
			ret.accuracyValue = c.getString(5);
			ret.providerValue = c.getString(6);
			ret.wifiDataValue = c.getString(7);
			ret.accelDataValue = c.getString(8);
//			ret.varianceValue = c.getString(9);
//			ret.averageValue = c.getString(10);
//			ret.fftValue = c.getString(11);
			ret.timeValue = c.getLong(9);
			ret.timezoneValue = c.getString(10);
			if (c.isNull(11))
				ret.latitudeValue = null;
			else
				ret.latitudeValue = c.getString(11);
			if (c.isNull(12))
				ret.longitudeValue = null;
			else
				ret.longitudeValue = c.getString(12);
		} else
		{
			ret.rowValue = -1;
			ret.modeValue = null;
			ret.statusValue = null;
			ret.locTimeValue = null;
			ret.accuracyValue = null;
			ret.providerValue = null;
			ret.wifiDataValue = null;
			ret.accelDataValue = null;
			ret.timeValue = -1;
			ret.timezoneValue = null;
			ret.latitudeValue = null;
			ret.longitudeValue = null;

		}
		c.close();
		return ret;
	}

	public boolean updateRowData(long rowId, String modeValue)
	{
		ContentValues vals = new ContentValues();
		vals.put(KEY_MODE, modeValue);
		Log.d(TAG, "updateRow: Updating mode: " + modeValue + "at rowId = " + rowId);
		return db.update(database_table, vals, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public boolean updateRowTime(long rowId, long timeValue)
	{
		ContentValues vals = new ContentValues();
		vals.put(KEY_TIME, timeValue);
		Log.d(TAG, "updateRow: Updating time " + timeValue + "at rowId = " + rowId);
		return db.update(database_table, vals, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public boolean updateRowLocation(long rowId, String latitudeValue, String longitudeValue)
	{
		ContentValues vals = new ContentValues();
		vals.put(KEY_LATITUDE, latitudeValue);
		vals.put(KEY_LONGITUDE, longitudeValue);
		Log.d(TAG, "updateRow: Updating location " + latitudeValue + ", " + longitudeValue + "at rowId = " + rowId);
		return db.update(database_table, vals, KEY_ROWID + "=" + rowId, null) > 0;
	}
}