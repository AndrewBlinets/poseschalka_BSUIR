package com.urban.basicsample.util;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.urban.basicsample.Log_file;
import com.urban.basicsample.dao.DBHelper;

public class Importer {

	private Context mContext;

	Log_file log_file = new Log_file();

	public Importer(Context context) {
		mContext = context;
	}

	public boolean importFile(String path) {
		log_file.writeFile(" 37 Importer    importFile   " + path);
		try {
			File file = new File(path);
			String group = file.getName().replaceAll(".xls", "");
			FileInputStream myInput = new FileInputStream(file);
			POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);
			HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);
			HSSFSheet mySheet = myWorkBook.getSheetAt(0);
			Iterator<Row> rowIter = mySheet.rowIterator();
			rowIter.next();
			while (rowIter.hasNext()) {
				HSSFRow myRow = (HSSFRow) rowIter.next();
				TreeMap<Integer, String> subjects = new TreeMap<>();
				String day = null;
				String time = null;
				// String week = "";
				HashMap<String, String> week = new HashMap<>();
				Iterator<Cell> cellIter = myRow.cellIterator();
				while (cellIter.hasNext()) {
					HSSFCell myCell = (HSSFCell) cellIter.next();
					switch (myCell.getColumnIndex()) {
					case 2:
					case 3:
					case 4:
					case 5:
						if (!myCell.toString().isEmpty()) {
							subjects.put(myCell.getColumnIndex() - 1, myCell.toString());
						}
						break;
					default:
						break;
					}
				}
				Iterator<Cell> cellIter1 = myRow.cellIterator();
				String old = "";
				while (cellIter1.hasNext()) {
					HSSFCell myCell = (HSSFCell) cellIter1.next();
					switch (myCell.getColumnIndex()) {
					case 0:
						day = myCell.toString();
						break;
					case 1:
						time = myCell.toString();
						break;
					default:
						if (old.indexOf(myCell.toString()) == -1) {
							String weekStr = "";
							for (Map.Entry<Integer, String> item : subjects.entrySet()) {
								if (item.getValue().equals(myCell.toString())) {
									weekStr += item.getKey();
									old += myCell.toString();
								}
							}
							if (weekStr.equals("1234")) {
								weekStr = "01234";
							}
							week.put(weekStr, myCell.toString());
						}
						break;
					}
				}

				switch (day) {
				case "Пн":
					day = "Понедельник";
					break;
				case "Вт":
					day = "Вторник";
					break;
				case "Ср":
					day = "Среда";
					break;
				case "Чт":
					day = "Четверг";
					break;
				case "Пт":
					day = "Пятница";
					break;
				case "Сб":
					day = "Суббота";
					break;
				default:
					break;
				}

				String[] arr = time.split("-");
				String timeStart = arr[0].substring(0, 2) + ":" + arr[0].substring(2);
				String timeEnd = arr[1].substring(0, 2) + ":" + arr[1].substring(2);

				if (!subjects.isEmpty()) {
					writeToDb(week, timeStart, timeEnd, day, group);
				}
			}
			Toast.makeText(mContext, "Импорт завершен", Toast.LENGTH_SHORT).show();
			log_file.writeFile(" 131 Importer    importFile   импорт завершен успешно.");
			return true;
		} catch (Exception e) {
			log_file.writeFile(" 134 Importer    importFile  исключение " + e.getMessage());
			Toast.makeText(mContext, "Упс! Что-то не так...", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}

		return false;
	}

	private void writeToDb(HashMap<String, String> weeks, String startTime, String endTime, String day, String group) {
		log_file.writeFile(" 143 Importer    writeToDb   " + day + "    " + group);
		DBHelper dbHeper = new DBHelper(mContext);
		SQLiteDatabase db = dbHeper.getReadableDatabase();

		String query = "SELECT * FROM Schedule WHERE LessonTimeStart = ? AND LessonTimeEnd = ? "
				+ "AND NumSubgroup = ? AND StudentGroup = ? AND Subject = ? " + "AND WeekNumber = ? AND Day = ?";

		for (Map.Entry<String, String> item : weeks.entrySet()) {
			Cursor c = db.rawQuery(query, new String[] { startTime, endTime, "0", group, item.getValue(),
					item.getKey(), day });
			if (!c.moveToFirst()) {
				ContentValues cv = new ContentValues();
				cv.put("LessonTimeStart", startTime);
				cv.put("LessonTimeEnd", endTime);
				cv.put("NumSubgroup", 0);
				cv.put("StudentGroup", group);
				cv.put("Subject", item.getValue());
				cv.put("WeekNumber", item.getKey());
				cv.put("Day", day);
				long rowID = db.insert("Schedule", null, cv);
			}
		}
		db.close();

	}
}
