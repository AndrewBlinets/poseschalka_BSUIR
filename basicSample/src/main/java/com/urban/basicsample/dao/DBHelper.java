package com.urban.basicsample.dao;

import com.urban.basicsample.MyFileClass;
import com.urban.basicsample.util.PassEncrypter;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 4;

    private static final String CREATE_TABLE_ST = "create table Students (" + "_id integer primary key autoincrement,"
            + "FirstName text," + "LastName text," + "GroupId text," + "SubGroup integer," + "Lecturer integer," + "Scan blob" + ");";

    private static final String CREATE_TABLE_L = "create table Lessons (" + "_id integer primary key autoincrement,"
            + "Date text," + "GroupId text," + "Subject text" + ");";

    private static final String CREATE_TABLE_A = "create table Attendance (" + "_id integer primary key autoincrement,"
            + "LessonId integer," + "StudentId integer," + "Attendance1 integer," + "Attendance2 integer" + ");";

    private static final String CREATE_TABLE_SCH = "create table Schedule (" + "_id integer primary key autoincrement,"
            + "LessonTimeStart text," + "LessonTimeEnd text," + "NumSubgroup integer," + "StudentGroup text,"
            + "Subject text," + "WeekNumber text," + "Day text" + ");";

    private static final String CREATE_TABLE_USERS = "create table Users (" + "_id integer primary key autoincrement,"
            + "user text," + "pass text" + ");";

    private static final String CREATE_TABLE_ST_TEMP = "create temporary table Students_backup (" + "_id integer primary key autoincrement,"
            + "FirstName text," + "LastName text," + "GroupId text," + "SubGroup integer," + "Scan blob" + ");";

    private static final String INSERT_INTO_BACKUP = "insert into Students_backup select _id,"
            + "FirstName, LastName, GroupId, SubGroup, Scan from Students;";

    private static final String INSERT_INTO = "insert into Students select _id,"
            + "FirstName, LastName, GroupId, SubGroup, Scan from Students_backup;";

   // MyFileClass file = new MyFileClass();

    public DBHelper(Context context) {
        super(context, "myDB", null, DB_VERSION);
      //  file.writeFile("DBHelper   construktor");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
       // file.writeFile("DBHelper   onCreate");
       // Log.i(Tag, Class.class + M)
        db.execSQL(CREATE_TABLE_ST);
        db.execSQL(CREATE_TABLE_L);
        db.execSQL(CREATE_TABLE_A);
        db.execSQL(CREATE_TABLE_SCH);
        db.execSQL(CREATE_TABLE_USERS);
        db.beginTransaction();
        ContentValues cv = new ContentValues();
        cv.put("user", "admin");
        cv.put("pass", PassEncrypter.encrypt("admin"));
        db.insert("Users", null, cv);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
       // file.writeFile("DBHelper   onUpgrade");
        ContentValues cv;
        switch (oldVersion) {
            case 1:
                db.execSQL(CREATE_TABLE_USERS);
                db.beginTransaction();
                cv = new ContentValues();
                cv.put("user", "admin");
                cv.put("pass", PassEncrypter.encrypt("admin"));
                db.insert("Users", null, cv);
                db.setTransactionSuccessful();
                db.endTransaction();
                break;
            case 2:
                cv = new ContentValues();
                cv.put("pass", PassEncrypter.encrypt("admin"));
                db.update("Users", cv, "user = ?", new String[]{"admin"});
                cv.clear();
                break;
            case 3:
                db.beginTransaction();
                db.execSQL(CREATE_TABLE_ST_TEMP);
                db.execSQL(INSERT_INTO_BACKUP);
                db.execSQL("drop table Students");
                db.execSQL(CREATE_TABLE_ST);
                db.execSQL(INSERT_INTO);
                db.execSQL("drop table Students_backup");
                db.setTransactionSuccessful();
                db.endTransaction();
                break;
        }

//        if (oldVersion == 1) {
//            db.execSQL(CREATE_TABLE_USERS);
//            db.beginTransaction();
//            ContentValues cv = new ContentValues();
//            cv.put("user", "admin");
//            cv.put("pass", PassEncrypter.encrypt("admin"));
//            db.insert("Users", null, cv);
//            db.setTransactionSuccessful();
//            db.endTransaction();
//        } else if (oldVersion == 2) {
//            ContentValues cv = new ContentValues();
//            cv.put("pass", PassEncrypter.encrypt("admin"));
//            db.update("Users", cv, "user = ?", new String[]{"admin"});
//            cv.clear();
//        }
    }

}
