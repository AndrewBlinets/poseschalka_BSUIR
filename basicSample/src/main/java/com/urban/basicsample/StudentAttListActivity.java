package com.urban.basicsample;

import java.util.ArrayList;

import com.urban.basicsample.adapter.StudentAttAdapter;
import com.urban.basicsample.dao.DBHelper;
import com.urban.basicsample.model.Attendance;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class StudentAttListActivity extends Activity {

	private ArrayList<Attendance> attendances;
	private StudentAttAdapter sAdapter;
	private boolean flag = false;

	private static final String Tag = "MyLog";
	Log_file file = new Log_file();
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		file.writeFile("31 StudentAttListActivity onCreate");
		setContentView(R.layout.activity_student_att_list);
		Intent startIntent = getIntent();
		int id = Integer.parseInt(startIntent.getStringExtra("id"));//.getExtra("id");
		String lastName = startIntent.getStringExtra("lastName");
		String firstName = startIntent.getStringExtra("firstName");
		String group = startIntent.getStringExtra("group");
		getStudentAtt(id);

		if(flag)
		{
			((TextView) findViewById(R.id.textView)).setText("Студента группы " + group + " " + lastName + " " + firstName.substring(0,1) + ".");
			sAdapter = new StudentAttAdapter(this, attendances);
			ListView listView = (ListView) findViewById(R.id.lvAttStudent);
			listView.setAdapter(sAdapter);
		}
		else
		{
			Toast.makeText(getApplicationContext(), "Данный студент не был отмечан на занятиях!!!", Toast.LENGTH_SHORT).show();
		}
	}

	private void getStudentAtt(int id) {

		DBHelper dbHeper = new DBHelper(this);
		SQLiteDatabase db = dbHeper.getReadableDatabase();

		String query = "SELECT * FROM Attendance LEFT JOIN Lessons ON Attendance.LessonId = Lessons._id " +
				"WHERE Attendance.StudentId = ?";
		Cursor c = db.rawQuery(query, new String[] { String.valueOf(id) });

		if (c.moveToFirst()) {
			attendances = new ArrayList<>();
			flag = true;
			int sunjectColIndex = c.getColumnIndex("Subject");
			int dateColIndex = c.getColumnIndex("Date");
			int att1ColIndex = c.getColumnIndex("Attendance1");
			int att2ColIndex = c.getColumnIndex("Attendance2");

			do {
				Attendance attendance = new Attendance();
				attendance.setSubjectName(c.getString(sunjectColIndex));
				attendance.setDate(c.getString(dateColIndex));
				attendance.setAt1(c.getInt(att1ColIndex));
				attendance.setAt2(c.getInt(att2ColIndex));
				attendances.add(attendance);
			} while (c.moveToNext());
		}
		c.close();
		db.close();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.student_att_list, menu);
		return true;
	}


}
