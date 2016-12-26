package com.urban.basicsample;

import java.util.ArrayList;

import com.urban.basicsample.adapter.StudentAdapter;
import com.urban.basicsample.adapter.StudentAttAdapter;
import com.urban.basicsample.dao.DBHelper;
import com.urban.basicsample.model.Attendance;
import com.urban.basicsample.model.Student;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class StudentAttListActivity extends Activity {

	private ArrayList<Attendance> attendances;
	private StudentAttAdapter sAdapter;

	private static final String Tag = "MyLog";
	MyFileClass file = new MyFileClass();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		file.writeFile( "StudentAttListActivity   onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_student_att_list);
		Intent startIntent = getIntent();
		int id = Integer.parseInt(startIntent.getStringExtra("id"));//.getExtra("id");
		getStudentAtt(id);
		
		sAdapter = new StudentAttAdapter(this, attendances);
		ListView listView = (ListView) findViewById(R.id.lvAttStudent);
		listView.setAdapter(sAdapter);		
	}

	private void getStudentAtt(int id) {
		file.writeFile( "StudentAttListActivity   getStudentAtt   " + id);
		DBHelper dbHeper = new DBHelper(this);
		SQLiteDatabase db = dbHeper.getReadableDatabase();

		String query = "SELECT * FROM Attendance LEFT JOIN Lessons ON Attendance.LessonId = Lessons._id " +
				"WHERE Attendance.StudentId = ?";
		Cursor c = db.rawQuery(query, new String[] { String.valueOf(id) });

		if (c.moveToFirst()) {
			attendances = new ArrayList<>();

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
		file.writeFile( "StudentAttListActivity   onCreateOptionsMenu");
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.student_att_list, menu);
		return true;
	}


}
