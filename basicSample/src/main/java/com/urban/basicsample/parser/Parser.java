package com.urban.basicsample.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.urban.basicsample.Log_file;
import com.urban.basicsample.dao.DBHelper;
import com.urban.basicsample.model.Schedule;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import android.util.Log;

public class Parser {

	public static final String TAG = "DEBUG";

	private static final String SCHEDULE_URL = "https://www.bsuir.by/schedule/rest/schedule/%s";
	private static final String GROUP_ID_URL = "https://www.bsuir.by/schedule/rest/studentGroup";

	private Handler handler;
	private Context mContext;
	private Thread thread = null;


	public Log_file fileClass =  new Log_file();




	public Parser(Context context) {

		mContext = context;

	}

	public void loadScheduleForGroup(final String group) {
		if (thread == null) {
			thread = new Thread(new Runnable() {
				@Override
				public void run() {
					loadScheduleForGroupInternal(group);
				}
			});
			thread.start();
		}
	}

	public boolean loadScheduleForGroupInternal(String group) {
		String groupId = getGroupId(group);
		if (groupId == null) {
			return false;
		}

		String url = String.format(SCHEDULE_URL, groupId);
		Document document = loadXmlDocumentFromUrl(url);

		if (document != null) {
			Schedule scheduleValues = parseScheduleDocument(document, group);
			if (scheduleValues != null) {
				return true;
			}

		} else {
			// TODO
		}

		thread = null;
		return false;
	}

	private String getGroupId(String group) {
		fileClass.writeFile(" 93 Parser      getGroupId   " + group);
		Document document = loadXmlDocumentFromUrl(GROUP_ID_URL);

		Element root = null;

		try {
			root = document.getDocumentElement();
		}
		catch (NullPointerException e)
		{

		}
		NodeList rows = root.getElementsByTagName("studentGroup");

		for (int i = 0; i < rows.getLength(); i++) {
			Element elem = (Element) rows.item(i);
			if (elem.getElementsByTagName("name").item(0).getTextContent().equals(group)) {
				return elem.getElementsByTagName("id").item(0).getTextContent();
			}
		}
		return null;
	}

	private Document loadXmlDocumentFromUrl(String stringUrl) {
		fileClass.writeFile(" 117 Parser loadXmlDocumentFromUrl");
		Document document = null;
		InputStream is = null;
		try {
			URL url = new URL(stringUrl);
			is = url.openStream();
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = db.parse(is);
		}
		catch (SAXException e) {
			fileClass.writeFile(" 127 Parser loadXmlDocumentFromUrl ошибка xml");
			Log.i(TAG, "retrieved xml is broken", e);
		}
		catch (MalformedURLException e) {
			Log.i(TAG, "bad url", e);
			fileClass.writeFile(" 132 Parser loadXmlDocumentFromUrl bad url");
		} catch (IOException e) {
			Log.i(TAG, "exception occurred during the reading data from bsuir's service", e);
			fileClass.writeFile(" 135 Parser loadXmlDocumentFromUrl exception occurred during the reading data from bsuir's service");
		} catch (Exception e) {
			Log.e(TAG, "xml parsing exception", e);
			fileClass.writeFile(" 138 Parser loadXmlDocumentFromUrl xml parsing exception");
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		return document;
	}

	private Schedule parseScheduleDocument(Document document, String group) {

		fileClass.writeFile(" 153 Parser parseScheduleDocument " + group );
		Schedule schedule = null;

		Element root = document.getDocumentElement();

		NodeList rows = root.getElementsByTagName("scheduleModel");
		for (int i = 0; i < rows.getLength(); i++) {
			Element row = (Element) rows.item(i);

			String day = row.getElementsByTagName("weekDay").item(0).getTextContent();

			NodeList list = row.getElementsByTagName("schedule");

			for (int j = 0; j < list.getLength(); j++) {
				schedule = new Schedule();

				Element elem = (Element) list.item(j);

				schedule.setDay(day);
				schedule.setLessonTime(elem.getElementsByTagName("lessonTime").item(0).getTextContent());
				schedule.setStudentGroup(elem.getElementsByTagName("studentGroup").item(0).getTextContent());
				schedule.setNumSubgroup(Integer.parseInt(elem.getElementsByTagName("numSubgroup").item(0)
						.getTextContent()));
				schedule.setSubject(elem.getElementsByTagName("subject").item(0).getTextContent()+"_"+elem.getElementsByTagName("lessonType").item(0).getTextContent());

				NodeList weeks = elem.getElementsByTagName("weekNumber");
				for (int k = 0; k < weeks.getLength(); k++) {
					Element week = (Element) weeks.item(k);
					schedule.addWeekNumber(week.getTextContent());
				}


				String[] str = schedule.getLessonTime().split("-");

				List<Integer> list_inspection = new ArrayList<>();
				for(int k =0; k < 2; k++) {
					String[] mas = str[k].split(":");
					list_inspection.add(Integer.parseInt(mas[0]));
				}

				if(list_inspection.get(1) - list_inspection.get(0) > 2 )
				{
					SimpleDateFormat format = new SimpleDateFormat();
					format.applyPattern("HH:mm");
					Date Time_start = new Date();
					Date Time_finish = new Date();
					try {
						Time_start = format.parse(str[0]);
						Time_finish = format.parse(str[1]);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					schedule.setLessonTime(str[0] + "-" + format.format(new Date(Time_start.getTime() + 5700000)));

					insertScheduleIntoDatabase(schedule);
					schedule.setLessonTime(format.format(new Date(Time_finish.getTime() - 5700000)) + "-" + str[1]);
					insertScheduleIntoDatabase(schedule);

				}
				else
				{
					insertScheduleIntoDatabase(schedule);
				}




				 /* Log.d(TAG, schedule.getDay()); Log.d(TAG, schedule.getLessonTime()); Log.d(TAG,
				  schedule.getStudentGroup()); Log.d(TAG, schedule.getSubject()); Log.d(TAG, schedule.getWeekNumber());
				  Log.d(TAG, schedule.getNumSubgroup()+"");*/

			}

		}

		return schedule;
	}

	private boolean insertScheduleIntoDatabase(Schedule schedule) {

		fileClass.writeFile(" 233 Parser insertScheduleIntoDatabase    " + schedule.getDay()
		+ "   " +  schedule.getSubject() + "   " + schedule.getStudentGroup());

	    StringTokenizer stk = new StringTokenizer(schedule.getLessonTime(),"-");
	    String []ar = new String[stk.countTokens()];
	    for(int i = 0; i<ar.length; i++)
	    {
	    	ar[i] = stk.nextToken();
	    }



	    DBHelper helper = new DBHelper(mContext);
		SQLiteDatabase database = helper.getWritableDatabase();

		try {
			database.beginTransaction();

			ContentValues cv = new ContentValues();
			cv.put("LessonTimeStart", ar[0]);
			cv.put("LessonTimeEnd", ar[1]);
			cv.put("NumSubgroup", schedule.getNumSubgroup());
			cv.put("StudentGroup", schedule.getStudentGroup());
			cv.put("Subject", schedule.getSubject());
			cv.put("WeekNumber", schedule.getWeekNumber());
			cv.put("Day", schedule.getDay());

			database.insert("Schedule", null, cv);

			database.setTransactionSuccessful();




			return true;
		} catch (Exception e) {
			return false;
		} finally {
			database.endTransaction();
			database.close();
		}

	}

}
