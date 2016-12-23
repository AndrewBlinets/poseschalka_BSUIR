package com.urban.basicsample;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.urban.basicsample.adapter.StudentAdapter;
import com.urban.basicsample.dao.DBHelper;
import com.urban.basicsample.model.CustomDialog1;
import com.urban.basicsample.model.Student;
import com.urban.basicsample.util.Exporter;
import com.urban.basicsample.util.Importer;

public class StudentListActivity extends Activity implements NavigationDrawerFragmentMain.NavigationDrawerCallbacks {

	private NavigationDrawerFragmentMain mNavigationDrawerFragment;
	private StudentAdapter sAdapter;
	private ArrayList<Student> students = null;

	private static final int DIALOG = 1;
	private static final int LIST_DIALOG = 2;
	private static final int STUDENT_DIALOG = 3;
	private static final int STUDENT_NOT_FOUND = 4;
	private static final int FILE_SELECT_CODE = 0;

	private static final String Tag = "MyLog";

	MyFileClass file = new MyFileClass();

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		file.writeFile("StudentListActivity   onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_student_list);
		Intent startIntent = getIntent();
		String lastName = startIntent.getStringExtra("lastName");
		mNavigationDrawerFragment = (NavigationDrawerFragmentMain) getFragmentManager().findFragmentById(
				R.id.navigation_drawer3);
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer3, (DrawerLayout) findViewById(R.id.drawer_layout2));

		getStudents(lastName);
		if (students != null) {
			sAdapter = new StudentAdapter(this, students);
			ListView listView = (ListView) findViewById(R.id.lvStudent);
			listView.setAdapter(sAdapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Intent intent = new Intent(StudentListActivity.this, StudentAttListActivity.class);
					intent.putExtra("id", id);
					startActivity(intent);
				}
			});
			
			listView.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					Intent intent = new Intent(StudentListActivity.this, ChangeStudentActivity.class);
					intent.putExtra("id", id);
					startActivity(intent);
					return true;
				}
			});
		} else {
			showDialog(STUDENT_NOT_FOUND);
		}

	}

	public void onSectionAttached(int number) {
		file.writeFile("StudentListActivity   onSectionAttached   " + number);
		switch (number) {
		case 1:
			CustomDialog1 cd = new CustomDialog1(this);
			cd.show();
			break;
		case 2:
			Intent intent = new Intent(this, AddStudentActivity.class);
			startActivity(intent);
			break;
		case 3:
			showDialog(LIST_DIALOG);
			break;
		case 4:
			showDialog(DIALOG);
			break;
		case 5:
			showDialog(STUDENT_DIALOG);
			break;
		case 6:
			Intent getContentIntent = FileUtils.createGetContentIntent();
			Intent i = Intent.createChooser(getContentIntent, "Выберите файл для загрузки");
			startActivityForResult(i, FILE_SELECT_CODE);
			break;
		case 7:
			// TODO
			Intent intent1 = new Intent(this, MainActivity.class);
			intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent1.putExtra("finish", true);
			startActivity(intent1);
			finish();
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		file.writeFile( "StudentListActivity   onActivityResult");
		if (requestCode == FILE_SELECT_CODE && resultCode == -1) {
			Uri uri = data.getData();
			String path = FileUtils.getPath(this, uri);
			Importer importer = new Importer(getApplicationContext());
			importer.importFile(path);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		file.writeFile( "StudentListActivity   onCreateDialog");
		Dialog dialog = null;
		Builder builder = new Builder(this);

		final EditText editText = new EditText(this);
		editText.setInputType(InputType.TYPE_CLASS_TEXT);
		switch (id) {
		case STUDENT_NOT_FOUND:
			dialog = builder.setTitle("Ошибка").setMessage("Студент не найден")
					.setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton("Ок", myClickListener).create();
			break;
		case DIALOG:
			DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String group = editText.getText().toString().trim();
					Exporter exporter = new Exporter(getApplicationContext(), group);
					if (exporter.export()) {
						Toast.makeText(getApplicationContext(), "Импортировано в файл " + group + ".xls",
								Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(getApplicationContext(), "Ошибка экспорта", Toast.LENGTH_SHORT).show();
					}
					dialog.dismiss();
				}
			};
			dialog = builder.setTitle("Введите группу").setPositiveButton("Ок", onClickListener).setView(editText)
					.create();
			break;
		case LIST_DIALOG:
			DialogInterface.OnClickListener onClickListener1 = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String group = editText.getText().toString().trim();
					Intent intent = new Intent(getApplicationContext(), ListActivity.class);
					intent.putExtra("group", group);
					startActivity(intent);
					dialog.dismiss();
				}
			};
			dialog = builder.setTitle("Введите группу").setPositiveButton("Ок", onClickListener1).setView(editText)
					.create();
			break;
		case STUDENT_DIALOG:
			DialogInterface.OnClickListener onClickListener2 = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String lastName = editText.getText().toString().trim();
					Intent intent = new Intent(getApplicationContext(), StudentListActivity.class);
					intent.putExtra("lastName", lastName);
					startActivity(intent);
					dialog.dismiss();
				}
			};
			dialog = builder.setTitle("Введите фамилию").setPositiveButton("Ок", onClickListener2).setView(editText)
					.create();
			break;
		default:
			break;
		}
		return dialog;
	}

	private void getStudents(String lastName) {
		file.writeFile( "StudentListActivity   getStudents   " + lastName);
		DBHelper dbHeper = new DBHelper(this);
		SQLiteDatabase db = dbHeper.getReadableDatabase();

		String query = "SELECT * FROM Students WHERE lastName = ?";
		Cursor c = db.rawQuery(query, new String[] { String.valueOf(lastName) });
		// Cursor c = db.query("Lessons", null, null, null, null, null, null);

		if (c.moveToFirst()) {
			students = new ArrayList<>();

			int idColIndex = c.getColumnIndex("_id");
			int firstColIndex = c.getColumnIndex("FirstName");
			int lastColIndex = c.getColumnIndex("LastName");
			int groupColIndex = c.getColumnIndex("GroupId");

			do {
				Student student = new Student();
				student.setId(c.getInt(idColIndex));
				student.setFirstName(c.getString(firstColIndex));
				student.setLastName(c.getString(lastColIndex));
				student.setGroup(c.getString(groupColIndex));
				students.add(student);
			} while (c.moveToNext());
		}
		c.close();
		db.close();
	}

	OnClickListener myClickListener = new OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case Dialog.BUTTON_POSITIVE:
				finish();
				break;
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		file.writeFile( "StudentListActivity   onCreateOptionsMenu");
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.student_list, menu);
		return true;
	}


	@Override
	public void onNavigationDrawerItemSelected(int position) {
		file.writeFile( "StudentListActivity   onNavigationDrawerItemSelected");
		onSectionAttached(position + 1);
	}
}
