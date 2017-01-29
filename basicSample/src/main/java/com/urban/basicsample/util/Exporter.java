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
   // private HashMap<String, Integer> names;
    private ArrayList<String> names_all_groups;
    private ArrayList<String> names_first_half;
    private ArrayList<String> names_second_half;
    private Workbook wb;
    private Sheet sheet;

    private ArrayList<String> dates = null;
    private ArrayList<Integer> Number_col = null;

    /*private static final String query_main = "SELECT *  " +
            "FROM Attendance LEFT JOIN Students ON Attendance.StudentId = Students._id"
            + " LEFT JOIN Lessons ON Attendance.LessonId = Lessons._id" +
            " WHERE Students.FirstName = ? AND Lessons._id = ?" +
            " AND Students.LastName = ?";*/

    private static final String q_y = "SELECT * FROM Lessons WHERE Subject = ? AND Date = ? AND GroupId = ?";


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
        getNames(0);
        getNames(1);
        getNames(2);
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

       // HashMap<String, Integer> dates =
                getDate(subject);
       // for (Map.Entry<String, Integer> entry : dates.entrySet()) {
        for (int i = 0; i < dates.size(); i++) {
            c = row1.createCell(Number_col.get(i));
            c.setCellValue(dates.get(i));
        }
        String [] mas = subject.split(" ");
        if(mas.length == 1)
        {
            HashMap<String, Row> rows = new HashMap<>();
            //for (Map.Entry<String, Integer> entry : names.entrySet()) {
            for (int i = 0; i < names_all_groups.size(); i++) {
                rowNum++;
                nameRow = sheet.createRow(/* entry.getValue() + */rowNum);
                c = nameRow.createCell(0);
                //c.setCellValue(entry.getKey().toString().split(" ")[1] + " " + entry.getKey().toString().split(" ")[0]);
                c.setCellValue(names_all_groups.get(i).split(" ")[1] + " " + names_all_groups.get(i).split(" ")[0]);
                //rows.put(entry.getKey(), nameRow);
                rows.put(names_all_groups.get(i), nameRow);
            }
            setNulls(rows, names_all_groups);
            DBHelper dbHeper = new DBHelper(mContext);
            SQLiteDatabase db = dbHeper.getReadableDatabase();
            int count_col = Number_col.get(0);
            String date_test = "";
            for (int i = 0; i < dates.size(); i++) {
                if(date_test.equals(dates.get(i)))
                {
                    i++;
                }
                else {
                    date_test = dates.get(i);
                    Cursor cursor = db.rawQuery(q_y, new String[]{subject, date_test, mGroup});
                    if (cursor.moveToFirst()) {
                        int idIndex = cursor.getColumnIndex("_id");
                        do {
                            String str_id = cursor.getString(idIndex);
                            for (int j = 0; j < names_all_groups.size(); j++) {
                                String query_main = "SELECT *  " +
                                        "FROM Attendance LEFT JOIN Students ON Attendance.StudentId = Students._id"
                                        + " LEFT JOIN Lessons ON Attendance.LessonId = Lessons._id" +
                                        " WHERE Students.FirstName = ? AND Lessons._id = ?" +
                                        " AND Students.LastName = ?";
                                String f = names_all_groups.get(j).split(" ")[0];
                                String l = names_all_groups.get(j).split(" ")[1];
                                Cursor cursor_main = db.rawQuery(query_main, new String[]{names_all_groups.get(j).split(" ")[0], str_id,
                                        names_all_groups.get(j).split(" ")[1]});
                                if (cursor_main.moveToFirst()) {
                                    int att1ColIndex = cursor_main.getColumnIndex("Attendance1");
                                    int att2ColIndex = cursor_main.getColumnIndex("Attendance2");
                                    String att = cursor_main.getShort(att1ColIndex) + "/" + cursor_main.getShort(att2ColIndex);
                                    Cell newCell = rows.get(names_all_groups.get(j)).createCell(count_col);
                                    newCell.setCellValue(att);
                                }
                                cursor_main.close();
                            }
                            count_col++;
                        } while (cursor.moveToNext());
                        cursor.close();
                    }
                }
            }
            c = row1.createCell(Number_col.get(dates.size() - 1) + 2);
            c.setCellValue("Посещаемость, %");
            // for (Map.Entry<String, Integer> entry : names.entrySet()) {
            for (int i = 0; i < names_all_groups.size(); i++) {
                // Cell newCell = rows.get(entry.getKey()).createCell(dates.get(lastDate) + 2);
                Cell newCell = rows.get(names_all_groups.get(i)).createCell(Number_col.get(dates.size() - 1) + 2);
                //  float res = getAttByStudent(entry.getKey(), subject) * 100 / (dates.size()*2);//протестить
                float res = getAttByStudent(names_all_groups.get(i), subject) * 100 / (dates.size()*2);//протестить
                newCell.setCellValue(res);
            }
            db.close();
        }
        else
        {
            if(dates.size() != 0)
        switch (mas[1])
        {
            case "1": {

                HashMap<String, Row> rows = new HashMap<>();
                //for (Map.Entry<String, Integer> entry : names.entrySet()) {
                for (int i = 0; i < names_first_half.size(); i++) {
                    rowNum++;
                    nameRow = sheet.createRow(/* entry.getValue() + */rowNum);
                    c = nameRow.createCell(0);
                    //c.setCellValue(entry.getKey().toString().split(" ")[1] + " " + entry.getKey().toString().split(" ")[0]);
                    c.setCellValue(names_first_half.get(i).split(" ")[1] + " " + names_first_half.get(i).split(" ")[0]);
                    //rows.put(entry.getKey(), nameRow);
                    rows.put(names_first_half.get(i), nameRow);
                }
                setNulls(rows, names_first_half);
                DBHelper dbHeper = new DBHelper(mContext);
                SQLiteDatabase db = dbHeper.getReadableDatabase();
                int count_col = Number_col.get(0);
                String date_test = "";
                for (int i = 0; i < dates.size(); i++) {
                    if(date_test.equals(dates.get(i)))
                    {
                        i++;
                    }
                    else {
                        date_test = dates.get(i);
                        Cursor cursor = db.rawQuery(q_y, new String[]{mas[0], date_test, mGroup});
                        if (cursor.moveToFirst()) {
                            int idIndex = cursor.getColumnIndex("_id");
                            do {
                                String str_id = cursor.getString(idIndex);
                                for (int j = 0; j < names_first_half.size(); j++) {
                                    String query_main = "SELECT *  " +
                                            "FROM Attendance LEFT JOIN Students ON Attendance.StudentId = Students._id"
                                            + " LEFT JOIN Lessons ON Attendance.LessonId = Lessons._id" +
                                            " WHERE Students.FirstName = ? AND Lessons._id = ?" +
                                            " AND Students.LastName = ?";

                                    Cursor cursor_main = db.rawQuery(query_main,
                                            new String[]{names_first_half.get(j).split(" ")[0], str_id,
                                                    names_first_half.get(j).split(" ")[1]});
                                    if (cursor_main.moveToFirst()) {
                                        int att1ColIndex = cursor_main.getColumnIndex("Attendance1");
                                        int att2ColIndex = cursor_main.getColumnIndex("Attendance2");
                                        String att = cursor_main.getShort(att1ColIndex) + "/" + cursor_main.getShort(att2ColIndex);
                                        Cell newCell = rows.get(names_first_half.get(j)).createCell(count_col);
                                        newCell.setCellValue(att);
                                    }
                                    cursor_main.close();
                                }
                                count_col++;
                            } while (cursor.moveToNext());
                            cursor.close();
                        }
                    }
                }
                c = row1.createCell(Number_col.get(dates.size() - 1) + 2);
                c.setCellValue("Посещаемость, %");
                // for (Map.Entry<String, Integer> entry : names.entrySet()) {
                for (int i = 0; i < names_first_half.size(); i++) {
                    // Cell newCell = rows.get(entry.getKey()).createCell(dates.get(lastDate) + 2);
                    Cell newCell = rows.get(names_first_half.get(i)).createCell(Number_col.get(dates.size() - 1) + 2);
                    //  float res = getAttByStudent(entry.getKey(), subject) * 100 / (dates.size()*2);//протестить
                    float res = getAttByStudent(names_first_half.get(i), subject) * 100 / (dates.size()*2);//протестить
                    newCell.setCellValue(res);
                }
                db.close();
                break;
            }
            case "2":
            {
                HashMap<String, Row> rows = new HashMap<>();
                //for (Map.Entry<String, Integer> entry : names.entrySet()) {
                for (int i = 0; i < names_second_half.size(); i++) {
                    rowNum++;
                    nameRow = sheet.createRow(/* entry.getValue() + */rowNum);
                    c = nameRow.createCell(0);
                    //c.setCellValue(entry.getKey().toString().split(" ")[1] + " " + entry.getKey().toString().split(" ")[0]);
                    c.setCellValue(names_second_half.get(i).split(" ")[1] + " " + names_second_half.get(i).split(" ")[0]);
                    //rows.put(entry.getKey(), nameRow);
                    rows.put(names_second_half.get(i), nameRow);
                }
                setNulls(rows, names_second_half);
                DBHelper dbHeper = new DBHelper(mContext);
                SQLiteDatabase db = dbHeper.getReadableDatabase();
                int count_col = Number_col.get(0);
                String date_test = "";
                for (int i = 0; i < dates.size(); i++) {
                    if(date_test.equals(dates.get(i)))
                    {
                        i++;
                    }
                    else {
                        date_test = dates.get(i);
                        Cursor cursor = db.rawQuery(q_y, new String[]{mas[0], date_test, mGroup});
                        if (cursor.moveToFirst()) {
                            int idIndex = cursor.getColumnIndex("_id");
                            do {
                                String str_id = cursor.getString(idIndex);
                                for (int j = 0; j < names_second_half.size(); j++) {
                                    String query_main = "SELECT *  " +
                                            "FROM Attendance LEFT JOIN Students ON Attendance.StudentId = Students._id"
                                            + " LEFT JOIN Lessons ON Attendance.LessonId = Lessons._id" +
                                            " WHERE Students.FirstName = ? AND Lessons._id = ?" +
                                            " AND Students.LastName = ?";

                                    Cursor cursor_main = db.rawQuery(query_main,
                                            new String[]{names_second_half.get(j).split(" ")[0], str_id,
                                                    names_second_half.get(j).split(" ")[1]});
                                    if (cursor_main.moveToFirst()) {
                                        int att1ColIndex = cursor_main.getColumnIndex("Attendance1");
                                        int att2ColIndex = cursor_main.getColumnIndex("Attendance2");
                                        String att = cursor_main.getShort(att1ColIndex) + "/" + cursor_main.getShort(att2ColIndex);
                                        Cell newCell = rows.get(names_second_half.get(j)).createCell(count_col);
                                        newCell.setCellValue(att);
                                    }
                                    cursor_main.close();
                                }
                                count_col++;
                            } while (cursor.moveToNext());
                            cursor.close();
                        }
                    }
                }
                c = row1.createCell(Number_col.get(dates.size() - 1) + 2);
                c.setCellValue("Посещаемость, %");
                // for (Map.Entry<String, Integer> entry : names.entrySet()) {
                for (int i = 0; i < names_second_half.size(); i++) {
                    // Cell newCell = rows.get(entry.getKey()).createCell(dates.get(lastDate) + 2);
                    Cell newCell = rows.get(names_second_half.get(i)).createCell(Number_col.get(dates.size() - 1) + 2);
                    //  float res = getAttByStudent(entry.getKey(), subject) * 100 / (dates.size()*2);//протестить
                    float res = getAttByStudent(names_second_half.get(i), subject) * 100 / (dates.size()*2);//протестить
                    newCell.setCellValue(res);
                }
                db.close();
                break;
            }

        }
        }










        return rowNum += 3;
    }

    private int getAttByStudent(String name, String subject) {
        file.writeFile("Exporter    getAttByStudent ");
        int result = 0;
        DBHelper dbHelper = new DBHelper(mContext);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String names[] = name.split(" ");
        String sub[] = subject.split(" ");
        /*String query = "SELECT coalesce(Attendance.Attendance1,0) + coalesce(Attendance.Attendance2,0) AS res FROM Attendance" +
                " LEFT JOIN Students ON Students._id = Attendance.StudentId WHERE Students.FirstName = ? " +
                "AND Students.LastName = ?";*/
            String query = "SELECT Attendance.Attendance1,Attendance.Attendance2 FROM Attendance" +
                    " LEFT JOIN Students ON Students._id = Attendance.StudentId" +
                    " LEFT JOIN Lessons ON Lessons._id = Attendance.LessonId"
                    + " WHERE Students.FirstName = ? " +
                    "AND Students.LastName = ? AND Lessons.Subject = ?";
            Cursor c = db.rawQuery(query, new String[]{names[0], names[1],sub[0]});
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

    private void setNulls(HashMap<String, Row> rows, ArrayList<String> mas) {
        file.writeFile("Exporter    setNulls ");
        //for (Integer cell : dates.values()) {
        for (int j = 0; j < Number_col.size(); j++) {
            //for (Map.Entry<String, Integer> name : names.entrySet()) {
            for (int i = 0; i < mas.size(); i++) {
                Cell nullCell = rows.get(mas.get(i)).createCell(Number_col.get(j));
                nullCell.setCellValue("н");
                CellStyle style = wb.createCellStyle();
                Font font = wb.createFont();
                font.setBold(true);
                style.setFont(font);
                nullCell.setCellStyle(style);
            }
        }

    }

    /* private HashMap<String, Integer> getDate(String subject) {
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
                String string = c.getString(dateColIndex);
                dates.put(c.getString(dateColIndex), cell);
                cell += 1;
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return dates;
    }*/


    private void getDate(String subject) {
        file.writeFile("Exporter    getDate  " + subject);

        String[] mas = subject.split(" ");

        DBHelper dbHeper = new DBHelper(mContext);
        SQLiteDatabase db = dbHeper.getReadableDatabase();
        if(mas.length == 1) {
            String query = "SELECT * FROM Lessons WHERE GroupId = ? AND Subject = ? ORDER BY Date ASC ";
            Cursor c = db.rawQuery(query, new String[]{mGroup, subject});
            Integer cell = 3;

            if (c.moveToFirst()) {
                // dates = new HashMap<>();
                dates = new ArrayList<>();
                Number_col = new ArrayList<>();
                int dateColIndex = c.getColumnIndex("Date");
                do {
                    //dates.put(c.getString(dateColIndex), cell);
                    dates.add(c.getString(dateColIndex));
                    Number_col.add(cell);
                    cell += 1;
                } while (c.moveToNext());
            }
            c.close();
        }
        else
        {

            String query = "SELECT * FROM Lessons WHERE GroupId = ? AND Subject = ? AND SubGroup = ? ORDER BY Date ASC ";
            Cursor c = db.rawQuery(query, new String[]{mGroup, mas[0], mas[1]});
            Integer cell = 3;
            dates = new ArrayList<>();
            if (c.moveToFirst()) {
                // dates = new HashMap<>();

                Number_col = new ArrayList<>();
                int dateColIndex = c.getColumnIndex("Date");
                do {
                    //dates.put(c.getString(dateColIndex), cell);
                    dates.add(c.getString(dateColIndex));
                    Number_col.add(cell);
                    cell += 1;
                } while (c.moveToNext());
            }
            c.close();
        }
        db.close();
       // return dates;
    }






    private void getNames(int i) {
        file.writeFile("Exporter    getNames  ");
        DBHelper dbHeper = new DBHelper(mContext);
        SQLiteDatabase db = dbHeper.getReadableDatabase();

        switch (i) {
            case 0:
            {
                String query = "SELECT * FROM Students WHERE GroupId = ? ORDER BY LastName ASC, FirstName ASC";
                Cursor c = db.rawQuery(query, new String[]{mGroup});

                if (c.moveToFirst()) {
                    Integer row = 0;
                    //names = new HashMap<>();
                    names_all_groups = new ArrayList<>();
                    int firstColIndex = c.getColumnIndex("FirstName");
                    int lastColIndex = c.getColumnIndex("LastName");
                    do {
                        //names.put(c.getString(firstColIndex) + " " + c.getString(lastColIndex), row);
                        names_all_groups.add(c.getString(firstColIndex) + " " + c.getString(lastColIndex));
                        //row += 2;
                    } while (c.moveToNext());
                }
                c.close();
                break;
            }
            case 1:
            {
                String query = "SELECT * FROM Students WHERE GroupId = ? AND SubGroup = ? ORDER BY LastName ASC, FirstName ASC";
                Cursor c = db.rawQuery(query, new String[]{mGroup, i + ""});

                if (c.moveToFirst()) {
                    Integer row = 0;
                    //names = new HashMap<>();
                    names_first_half = new ArrayList<>();
                    int firstColIndex = c.getColumnIndex("FirstName");
                    int lastColIndex = c.getColumnIndex("LastName");
                    do {
                        //names.put(c.getString(firstColIndex) + " " + c.getString(lastColIndex), row);
                        names_first_half.add(c.getString(firstColIndex) + " " + c.getString(lastColIndex));
                        //row += 2;
                    } while (c.moveToNext());
                }
                c.close();
                break;
            }
            case 2:
            {
                String query = "SELECT * FROM Students WHERE GroupId = ? AND SubGroup = ? ORDER BY LastName ASC, FirstName ASC";
                Cursor c = db.rawQuery(query, new String[]{mGroup, i + ""});

                if (c.moveToFirst()) {
                    Integer row = 0;
                    //names = new HashMap<>();
                    names_second_half = new ArrayList<>();
                    int firstColIndex = c.getColumnIndex("FirstName");
                    int lastColIndex = c.getColumnIndex("LastName");
                    do {
                        //names.put(c.getString(firstColIndex) + " " + c.getString(lastColIndex), row);
                        names_second_half.add(c.getString(firstColIndex) + " " + c.getString(lastColIndex));
                        //row += 2;
                    } while (c.moveToNext());
                }
                c.close();
                break;
            }
        }

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
                String subject = c.getString(subjectColIndex);
                if(subject.split("_")[1].equals("ЛР"))
                {
                    subjects.add(subject + " 1 подгруппа");
                    dateCell += 2;
                    subjects.add(subject + " 2 подгруппа");
                    dateCell += 2;
                }
                else {
                    subjects.add(subject);
                }
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
