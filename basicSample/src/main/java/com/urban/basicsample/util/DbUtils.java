package com.urban.basicsample.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.urban.basicsample.Log_file;
import com.urban.basicsample.dao.DBHelper;

public class DbUtils {
	
	private Context context;
	
	public DbUtils(Context context) {
		this.context = context;
	}



	public boolean checkSchedule(String group) {

		SQLiteDatabase database = null;
		try {
			DBHelper helper = new DBHelper(context);
			database = helper.getWritableDatabase();
			String[] selectionArgs = new String[] { group };
			Cursor c = database.query("Schedule", null, "StudentGroup = ?", selectionArgs, null, null, null);

			if (c.getCount() == 0) {
				return false;
			} else {
				return true;
			}
		} finally {
			if (database != null)
				database.close();
		}

	}

	public void delete_schetual_group(String group)
	{

		SQLiteDatabase database = null;
		try {
			DBHelper helper = new DBHelper(context);
			database = helper.getWritableDatabase();
			database.delete("Schedule","StudentGroup = " + group, null);
		} finally {
			if (database != null)
				database.close();
		}
	}

}
