package com.urban.basicsample;

import java.util.ArrayList;

import com.urban.basicsample.adapter.DetailsAdapter;
import com.urban.basicsample.dao.DBHelper;
import com.urban.basicsample.model.Attendance;
import com.urban.basicsample.model.Lesson;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

public class ListDetails extends Activity {

	private long id;
	private ArrayList<Attendance> listAtt;
	private DetailsAdapter dAdapter;

	private static final String Tag = "MyLog";
MyFileClass file = new MyFileClass();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		file.writeFile( "ListDetails   onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_details);
		Intent startIntent = getIntent();
		
		id = startIntent.getLongExtra("id", 0l);
		String subject = startIntent.getStringExtra("Subject");
		String data = startIntent.getStringExtra("Date");
		getData();
		((TextView) findViewById(R.id.textView)).setText("Предмет " + subject + "  Дата: " + data);
		dAdapter = new DetailsAdapter(this, listAtt);
		ListView lvDet = (ListView) findViewById(R.id.lvDet);
		lvDet.setAdapter(dAdapter);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		file.writeFile( "ListDetails   onCreateOptionsMenu");
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list_details, menu);
		return true;
	}

	
	private void getData() {
		file.writeFile( "ListDetails   getData");
		DBHelper dbHeper = new DBHelper(this);
		SQLiteDatabase db = dbHeper.getReadableDatabase();

		String query = "SELECT * FROM Attendance AS At LEFT JOIN Students AS St ON At.studentId = St._id WHERE At.lessonId = ?";
		
		Cursor c = db.rawQuery(query, new String[] { String.valueOf(id) });

		if (c.moveToFirst()) {
			listAtt = new ArrayList<>();

			int firstNameColIndex = c.getColumnIndex("FirstName");
			int lastNameColIndex = c.getColumnIndex("LastName");
			int groupColIndex = c.getColumnIndex("GroupId");
			int att1ColIndex = c.getColumnIndex("Attendance1");
			int att2ColIndex = c.getColumnIndex("Attendance2");

			do {
				Attendance attendance = new Attendance();
				attendance.setFirstName(c.getString(firstNameColIndex));
				attendance.setLastName(c.getString(lastNameColIndex));
				attendance.setGroup(c.getString(groupColIndex));
				attendance.setAt1(c.getInt(att1ColIndex));
				attendance.setAt2(c.getInt(att2ColIndex));
				listAtt.add(attendance);
			} while (c.moveToNext());
		}
		c.close();
		db.close();
	}
}
