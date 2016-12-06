package com.urban.basicsample.util;

import java.security.MessageDigest;
import java.util.ArrayList;

import com.urban.basicsample.dao.DBHelper;
import com.urban.basicsample.model.Attendance;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class PassEncrypter {

	public static String encrypt(String pass) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] bytes = md.digest(pass.getBytes("UTF-8"));
			return getString(bytes);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean verifyAdminPass(Context context, String pass) {
		String dbPass = null;
		DBHelper helper = new DBHelper(context);
		SQLiteDatabase db = helper.getReadableDatabase();
		String query = "SELECT pass FROM Users WHERE user = ?";
		Cursor c = db.rawQuery(query, new String[] { "admin" });
		if (c.moveToFirst()) {
			int adminPassIndex = c.getColumnIndex("pass");
			do {
				dbPass = c.getString(adminPassIndex);
			} while (c.moveToNext());
		}
		c.close();
		db.close();
		String encrPass = encrypt(pass);
		if (dbPass != null) {
			if (dbPass.equals(encrPass)) {
				return true;
			}
		} else {
			Toast.makeText(context, "Ошибка чтения базы", Toast.LENGTH_SHORT).show();
		}
		return false;
	}

	private static String getString(byte[] bytes) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			String hex = Integer.toHexString((int) 0x00FF & b);
			if (hex.length() == 1) {
				sb.append("0");
			}
			sb.append(hex);
		}
		return sb.toString();
	}
}
