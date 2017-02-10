package com.urban.basicsample;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;

import com.urban.basicsample.dao.DBHelper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;


/**
 * Created by Andrew on 18.12.2016.
 */

public class Log_file {

    final private String FILENAME = "Log_file_bsuir.txt";

    private String str_file = "";

    private SimpleDateFormat format;

    public Log_file() {
        File sdPath = Environment.getExternalStorageDirectory();
        sdPath = new File(sdPath.getAbsolutePath() + "/" + "Attendance");
        sdPath.mkdirs();
        this.format = new SimpleDateFormat();
        format.applyPattern("dd.MM.yyyy HH:mm");
        this.str_file  = sdPath.getAbsolutePath() + "/" + FILENAME ;
    }

    public void writeFile(String line) {
        String dataTime = format.format(new Date());
        FileWriter bw = null;
        try {
            bw = new FileWriter(str_file,true);
            bw.append(" " + dataTime + "  " + line + "\n");
            bw.flush();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


  /*  public Log_file() {
        FILENAME = Environment.getExternalStorageDirectory().toString() + "/Log_file.txt";
    }

    public void qwe ( Context mContext) {
        String str = null;



        DBHelper dbHeper123 = new DBHelper(mContext);
        SQLiteDatabase db132 = dbHeper123.getReadableDatabase();
        String query123 = "SELECT * FROM Schedule";
        Cursor c123 = db132.rawQuery(query123, null );
        str +="SELECT * FROM Schedule\n";
        if (c123.moveToFirst()) {

            int idColIndex1 = c123.getColumnIndex("_id");
            int LessonTimeStart = c123.getColumnIndex("LessonTimeStart");
            int LessonTimeEnd = c123.getColumnIndex("LessonTimeEnd");
            int NumSubgroup = c123.getColumnIndex("NumSubgroup");
            int StudentGroup = c123.getColumnIndex("StudentGroup");
            int Subject = c123.getColumnIndex("Subject");
            int WeekNumber = c123.getColumnIndex("WeekNumber");
            int Day = c123.getColumnIndex("Day");



            do {
                str +=
                        c123.getString(idColIndex1)+" "+
                        c123.getString(LessonTimeStart)+" "+
                        c123.getString(LessonTimeEnd)+" "+
                        c123.getString(NumSubgroup)+" "+
                        c123.getString(StudentGroup)+" "+
                        c123.getString(Subject)+" "+
                        c123.getString(WeekNumber)+" "+
                        c123.getString(Day)+" "+
                                "\n";
            } while (c123.moveToNext());
        }
        c123.close();

        db132.close();

        DBHelper dbHeper = new DBHelper(mContext);
        SQLiteDatabase db = dbHeper.getReadableDatabase();
        String query = "SELECT * FROM Lessons";
        Cursor c = db.rawQuery(query, null );
        str +="SELECT * FROM Lessons\n";
        if (c.moveToFirst()) {

            int idColIndex1 = c.getColumnIndex("_id");
            int dateColIndex1 = c.getColumnIndex("Date");
            int GroupId = c.getColumnIndex("GroupId");
            int Subject = c.getColumnIndex("Subject");
            int numSubject = c.getColumnIndex("SubGroup");


            do {
                str +=
                        c.getString(idColIndex1)+" "
                        +c.getString(dateColIndex1)+" "
                        +c.getString(GroupId)+" "
                        +c.getString(Subject)+ " "
                        +c.getString(numSubject)+ "\n";
            } while (c.moveToNext());
        }


        DBHelper dbHeper1 = new DBHelper(mContext);
        SQLiteDatabase db1 = dbHeper1.getReadableDatabase();
         query = "SELECT * FROM Students";
        Cursor c1 = db1.rawQuery(query, null );
        str +="SELECT * FROM Students\n";
        if (c1.moveToFirst()) {

            int idColIndex1 = c1.getColumnIndex("_id");
            int dateColIndex1 = c1.getColumnIndex("FirstName");
            int LastNameColIndex1 = c1.getColumnIndex("LastName");
            int GroupIdColIndex1 = c1.getColumnIndex("GroupId");
            int SubjectColIndex1 = c1.getColumnIndex("SubGroup");
            int LecturerColIndex1 = c1.getColumnIndex("Lecturer");
            int LecturerColIndex2 = c1.getColumnIndex("Scan");

            do {
                str += c1.getString(idColIndex1)+" "
                        + c1.getString(dateColIndex1)+" "
                        + c1.getString(LastNameColIndex1)+" "
                        + c1.getString(GroupIdColIndex1)+" "
                        + c1.getString(SubjectColIndex1)+" "
                        + c1.getString(LecturerColIndex1) +" "
                        + c1.getBlob(LecturerColIndex2).toString()
                        + "\n";
            } while (c1.moveToNext());
        }

        str+="/////////////////////Attendance///////////////////////\n";
        str+=" _id  LessonId    StudentId   Attendance1 Attendance2\n" ;
        DBHelper dbHeper21 = new DBHelper(mContext);
        SQLiteDatabase db21 = dbHeper21.getReadableDatabase();
        query = "SELECT * FROM Attendance";
        Cursor c121 = db21.rawQuery(query, null );
        str +="SELECT * FROM Attendance\n";
        if (c121.moveToFirst()) {

            int idColIndex21 = c121.getColumnIndex("_id");
            int LessonId = c121.getColumnIndex("LessonId");
            int StudentId = c121.getColumnIndex("StudentId");
            int Attendance1 = c121.getColumnIndex("Attendance1");
            int Attendance2 = c121.getColumnIndex("Attendance2");

            do {
                str +=
                        c121.getString(idColIndex21)+"      " +
                                c121.getString(LessonId)+"      " +
                                c121.getString(StudentId)+"     " +
                                c121.getString(Attendance1)+"       " +
                                c121.getString(Attendance2) +
                                "\n";
            } while (c121.moveToNext());
        }


        c121.close();
        c1.close();
        c.close();

        db1.close();
        db.close();

        db21.close();
        }*/
}
