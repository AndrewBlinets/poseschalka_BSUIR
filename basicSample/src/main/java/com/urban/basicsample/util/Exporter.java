package com.urban.basicsample.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.urban.basicsample.MyFileClass;
import com.urban.basicsample.dao.DBHelper;

public class Exporter {

    private Context mContext;
    private String mGroup;
    private int expAtt;
    private HashMap<String, Integer> names;
   //  private ArrayList<String> names;
    private Workbook wb;
    private Sheet sheet;

    MyFileClass file = new MyFileClass();

    public Exporter(Context context, String group) {
        file.writeFile("Exporter    Exporter  "+ group);
        mContext = context;
        mGroup = group;
    }

    public boolean export() {
        file.writeFile("Exporter    export  ");
        // check if available and not read only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            return false;
        }
        boolean success = false;
        getNames();
        wb = new HSSFWorkbook();
        Cell c = null;
        sheet = wb.createSheet(mGroup);
        Row row = sheet.createRow(0);
        c = row.createCell(0);
        c.setCellValue("Группа:");
        c = row.createCell(1);
        c.setCellValue(mGroup);

        expAtt = getExcAtt();

        LinkedList<String> subjects = getData();
        Integer rowNum = 3;
       try {
           for (String subject : subjects) {
               rowNum = writeSubject(rowNum, subject);
           }
       }
       catch (NullPointerException e)
       {
           success = false;
           return success;
       }
        File sdPath = Environment.getExternalStorageDirectory();
        sdPath = new File(sdPath.getAbsolutePath() + "/" + "Attendance");
        sdPath.mkdirs();
        File file = new File(sdPath, mGroup + ".xls");
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);
            wb.write(os);
            success = true;
        } catch (Exception ignored) {

        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ignored) {
            }
        }

        return success;
    }

    private Integer writeSubject(Integer rowNum, String subject) {

        file.writeFile("Exporter    writeSubject  ");
        Row row0 = null;
        Row nameRow = null;
        Cell c = null;
        row0 = sheet.createRow(rowNum);
        c = row0.createCell(0);
        c.setCellValue("Предмет:");
        c = row0.createCell(1);
        c.setCellValue(subject);
        Row row1 = sheet.createRow(rowNum += 1);

        HashMap<String, Integer> dates = getDate(subject);
        for (Map.Entry<String, Integer> entry : dates.entrySet()) {
            c = row1.createCell(entry.getValue());
            c.setCellValue(entry.getKey());
        }

        HashMap<String, Row> rows = new HashMap<>();
        for (Map.Entry<String, Integer> entry : names.entrySet()) {
            rowNum++;
            nameRow = sheet.createRow(/* entry.getValue() + */rowNum);
            c = nameRow.createCell(0);
            c.setCellValue(entry.getKey().toString().split(" ")[1] + " " + entry.getKey().toString().split(" ")[0]);
            rows.put(entry.getKey(), nameRow);
        }
        setNulls(rows, dates);
        DBHelper dbHeper = new DBHelper(mContext);
        SQLiteDatabase db = dbHeper.getReadableDatabase();

        String query = "SELECT * FROM Students LEFT JOIN Attendance ON Students._id = Attendance.StudentId"
                + " LEFT JOIN Lessons ON Attendance.LessonId = Lessons._id WHERE Students.GroupId = ? AND Subject = ?";
        Cursor cursor = db.rawQuery(query, new String[]{mGroup, subject});
        String lastDate = null;
        if (cursor.moveToFirst()) {
            int att1ColIndex = cursor.getColumnIndex("Attendance1");
            int att2ColIndex = cursor.getColumnIndex("Attendance2");
            int firstColIndex = cursor.getColumnIndex("FirstName");
            int lastColIndex = cursor.getColumnIndex("LastName");
            int dateColIndex = cursor.getColumnIndex("Date");
            do {

                String att = cursor.getShort(att1ColIndex) + "/" + cursor.getShort(att2ColIndex);
                String date = cursor.getString(dateColIndex);
                lastDate = date;
                String name = cursor.getString(firstColIndex) + " " + cursor.getString(lastColIndex);
                // Row newRow = sheet.createRow(names.get(name) + (rowNum+=2));
                // Cell newCell = newRow.createCell(dates.get(date));
                Cell newCell = rows.get(name).createCell(dates.get(date));
                newCell.setCellValue(att);
               /* CellStyle style = wb.createCellStyle();
                Font font = wb.createFont();
                font.setBold(true);
                style.setFont(font);
                newCell.setCellStyle(style);
                */
            } while (cursor.moveToNext());


        }
        cursor.close();
        c = row1.createCell(dates.get(lastDate) + 2);
        c.setCellValue("Посещаемость, %");
        for (Map.Entry<String, Integer> entry : names.entrySet()) {
            Cell newCell = rows.get(entry.getKey()).createCell(dates.get(lastDate) + 2);
            float res = getAttByStudent(entry.getKey(), subject) * 100 / (dates.size()*2);//протестить
            newCell.setCellValue(res);
        }

        db.close();
        return rowNum += 3;
    }

    private int getAttByStudent(String name, String subject) {
        file.writeFile("Exporter    getAttByStudent ");
        int result = 0;
        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String names[] = name.split(" ");
        /*String query = "SELECT coalesce(Attendance.Attendance1,0) + coalesce(Attendance.Attendance2,0) AS res FROM Attendance" +
                " LEFT JOIN Students ON Students._id = Attendance.StudentId WHERE Students.FirstName = ? " +
                "AND Students.LastName = ?";*/
            String query = "SELECT Attendance.Attendance1,Attendance.Attendance2 FROM Attendance" +
                    " LEFT JOIN Students ON Students._id = Attendance.StudentId" +
                    " LEFT JOIN Lessons ON Lessons._id = Attendance.LessonId"
                    + " WHERE Students.FirstName = ? " +
                    "AND Students.LastName = ? AND Lessons.Subject = ?";
            Cursor c = db.rawQuery(query, new String[]{names[0], names[1],subject});
            if (c.moveToFirst()) {
                int Attendance1ColIndex = c.getColumnIndex("Attendance1");
                int Attendance2ColIndex = c.getColumnIndex("Attendance2");
                do {
                    result += c.getInt(Attendance1ColIndex);
                    result += c.getInt(Attendance2ColIndex);
                }
                while (c.moveToNext());

            }
        c.close();
        db.close();
        return result;
    }

    private int getExcAtt() {
        file.writeFile("Exporter    getExcAtt ");
        int result = 0;
        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT coalesce(Attendance.Attendance1,0) + coalesce(Attendance.Attendance2,0) AS res FROM Attendance" +
                " LEFT JOIN Students ON Students._id = Attendance.StudentId WHERE Students.Lecturer = ?";
        Cursor c = db.rawQuery(query, new String[]{"1"});
        if (c.moveToFirst()) {
            int resColIndex = c.getColumnIndex("res");
            result = c.getInt(resColIndex);
        }

        c.close();
        db.close();
        return result;
    }

    private void setNulls(HashMap<String, Row> rows, HashMap<String, Integer> dates) {
        file.writeFile("Exporter    setNulls ");
        for (Integer cell : dates.values()) {
            for (Map.Entry<String, Integer> name : names.entrySet()) {
                Cell nullCell = rows.get(name.getKey()).createCell(cell);
                nullCell.setCellValue("н");
                CellStyle style = wb.createCellStyle();
                Font font = wb.createFont();
                font.setBold(true);
                style.setFont(font);
                nullCell.setCellStyle(style);
            }
        }

    }

    private HashMap<String, Integer> getDate(String subject) {
        file.writeFile("Exporter    getDate  " + subject);
        HashMap<String, Integer> dates = null;

        DBHelper dbHeper = new DBHelper(mContext);
        SQLiteDatabase db = dbHeper.getReadableDatabase();

        String query = "SELECT * FROM Lessons WHERE GroupId = ? AND Subject = ?";
        Cursor c = db.rawQuery(query, new String[]{mGroup, subject});
        Integer cell = 3;

        if (c.moveToFirst()) {
            dates = new HashMap<>();
            int dateColIndex = c.getColumnIndex("Date");
            do {
                dates.put(c.getString(dateColIndex), cell);
                cell += 1;
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return dates;
    }

    private void getNames() {
        file.writeFile("Exporter    getNames  ");
        DBHelper dbHeper = new DBHelper(mContext);
        SQLiteDatabase db = dbHeper.getReadableDatabase();

        String query = "SELECT * FROM Students WHERE GroupId = ? ORDER BY LastName ASC, FirstName ASC";
        Cursor c = db.rawQuery(query, new String[]{mGroup});

        if (c.moveToFirst()) {
            Integer row = 0;
            names = new HashMap<>();
            int firstColIndex = c.getColumnIndex("FirstName");
            int lastColIndex = c.getColumnIndex("LastName");
            do {
                names.put(c.getString(firstColIndex) + " " + c.getString(lastColIndex), row);
                row += 2;
            } while (c.moveToNext());
        }
        c.close();
        db.close();
    }

    private LinkedList<String> getData() {
        file.writeFile("Exporter    getData  ");
        LinkedList<String> subjects = null;
        // HashMap<String, Integer> date = null;

        DBHelper dbHeper = new DBHelper(mContext);
        SQLiteDatabase db = dbHeper.getReadableDatabase();

        String query = "SELECT Subject FROM Lessons WHERE GroupId = ? GROUP BY Subject";

        Cursor c = db.rawQuery(query, new String[]{mGroup});

        if (c.moveToFirst()) {
            subjects = new LinkedList<>();
            // date = new HashMap<>();
            Integer dateCell = 3;

            // int idColIndex = c.getColumnIndex("_id");
            // int dateColIndex = c.getColumnIndex("Date");
            // int groupColIndex = c.getColumnIndex("GroupId");
            int subjectColIndex = c.getColumnIndex("Subject");

            do {
                subjects.add(c.getString(subjectColIndex));
                // date.put(c.getString(dateColIndex), dateCell);
                dateCell += 2;
            } while (c.moveToNext());
        }
        c.close();
        db.close();

        return subjects;
    }

    private boolean isExternalStorageReadOnly() {
        file.writeFile("Exporter    isExternalStorageReadOnly  ");
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private boolean isExternalStorageAvailable() {
        file.writeFile("Exporter    isExternalStorageAvailable  ");
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

}
