package com.urban.basicsample;

import com.urban.basicsample.dao.DBHelper;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChangeStudentActivity extends Activity {

	private EditText firstName;
	private EditText lastName;
	private EditText group;
	private EditText subGroup;
	private Button ok;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_student);
		Intent startIntent = getIntent();
		final long id = startIntent.getLongExtra("id", 1);
		
		firstName = (EditText) findViewById(R.id.et_c_firstname);
		lastName = (EditText) findViewById(R.id.et_c_lastname);
		group = (EditText) findViewById(R.id.et_c_group);
		subGroup = (EditText) findViewById(R.id.et_c_sgroup);
		ok = (Button) findViewById(R.id.c_ok);
		ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (updateStudent(id)) {
					Toast.makeText(getApplicationContext(), "Студент обновлен", Toast.LENGTH_SHORT).show();
					finish();
				} else {
					Toast.makeText(getApplicationContext(), "Ошибка записи", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		getStudentById(id);
	}
	
	private void getStudentById(long id) {
		DBHelper dbHelper = new DBHelper(this);
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		String query = "SELECT * FROM Students WHERE _id = ?";
		Cursor c = db.rawQuery(query, new String[] { String.valueOf(id) });
		
		if (c.moveToFirst()) {
			int firstNameColIndex = c.getColumnIndex("FirstName");
			int lastNameColIndex = c.getColumnIndex("LastName");
			int groupIdColIndex = c.getColumnIndex("GroupId");
			int subGroupColIndex = c.getColumnIndex("SubGroup");
			
			String firstname = c.getString(firstNameColIndex);
			String lastname = c.getString(lastNameColIndex);
			String groupId = c.getString(groupIdColIndex);
			int subGroup = c.getInt(subGroupColIndex);	
			
			firstName.setText(firstname);
			lastName.setText(lastname);
			group.setText(groupId);
			this.subGroup.setText(String.valueOf(subGroup));
		}
		c.close();
		db.close();
	}
	
	private boolean updateStudent(long id) {
		int updCount = 0;
		String firstName = this.firstName.getText().toString();
		String lastName = this.lastName.getText().toString();
		String group = this.group.getText().toString();
		int subGroup = Integer.parseInt(this.subGroup.getText().toString());
		
		ContentValues cv = new ContentValues();
		cv.put("FirstName", firstName);
		cv.put("LastName", lastName);
		cv.put("GroupId", group);
		cv.put("SubGroup", subGroup);
		
		DBHelper dbHelper = new DBHelper(this);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		updCount = db.update("Students", cv, "_id = ?", new String[] { String.valueOf(id) });
		
		return updCount == 1 ? true : false;
	}
	
}
