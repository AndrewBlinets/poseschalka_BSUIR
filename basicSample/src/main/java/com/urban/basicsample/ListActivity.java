package com.urban.basicsample;

import java.util.ArrayList;
import java.util.zip.Inflater;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.urban.basicsample.MainActivity.PlaceholderFragment;
import com.urban.basicsample.adapter.LessonAdapter;
import com.urban.basicsample.dao.DBHelper;
import com.urban.basicsample.model.CustomDialog1;
import com.urban.basicsample.model.Lesson;
import com.urban.basicsample.model.Schedule;
import com.urban.basicsample.util.Exporter;
import com.urban.basicsample.util.Importer;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.LayoutParams;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListActivity extends Activity implements NavigationDrawerFragmentMain.NavigationDrawerCallbacks {

	private static final String Tag = "MyLog";

	private ArrayList<Lesson> lessons = null;
	private ArrayList<Schedule> schedules = null;
	private LessonAdapter lAdapter;
	private static final int DIALOG = 1;
	private static final int LIST_DIALOG = 2;
	private static final int STUDENT_DIALOG = 3;
	private static final int FILE_SELECT_CODE = 0;

	private NavigationDrawerFragmentMain mNavigationDrawerFragment;
	private CharSequence mTitle;
	private boolean access;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(Tag, "ListActivity   onCreate");
		super.onCreate(savedInstanceState);
		
		Intent startIntent = getIntent();
		String group = startIntent.getStringExtra("group");
		access = startIntent.getBooleanExtra("access", false);
		// mTitle = getTitle();

		// Set up the drawer.
		if (access) {
			setContentView(R.layout.activity_list);
			mNavigationDrawerFragment = (NavigationDrawerFragmentMain) getFragmentManager().findFragmentById(
					R.id.navigation_drawer2);
			mNavigationDrawerFragment.setUp(R.id.navigation_drawer2, (DrawerLayout) findViewById(R.id.drawer_layout2));
		} else {
			setContentView(R.layout.activity_list1);
		}

		getData(group);
		if (lessons == null) {
			Toast.makeText(this, "Группа не найдена", Toast.LENGTH_SHORT).show();
			finish();
		} else {
			lAdapter = new LessonAdapter(this, lessons);
			((TextView) findViewById(R.id.textView)).setText("Группа № " + group);
			// LayoutInflater inflater = (LayoutInflater) this.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
			//
			// View view = inflater.inflate( R.layout.fragment_main2, null );
			ListView listView = (ListView) findViewById(R.id.lvMain);
			listView.setAdapter(lAdapter);
			listView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					// Toast.makeText(getApplicationContext(), "pos=" + position + " id=" + id,
					// Toast.LENGTH_LONG).show();
					Intent intent = new Intent(ListActivity.this, ListDetails.class);
					intent.putExtra("id", id);
					intent.putExtra("Date", lessons.get(position).getDate());
					intent.putExtra("Subject", lessons.get(position).getSubject());
					startActivity(intent);
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(Tag, "ListActivity   onCreateOptionsMenu");
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list, menu);
		return true;
	}

	public void onSectionAttached(int number) {
		Log.i(Tag, "ListActivity   onSectionAttached");
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
			/*
			 * Intent intent2 = new Intent(this, ListActivity.class); startActivity(intent2);
			 */
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
		Log.i(Tag, "ListActivity   onActivityResult");
		if (requestCode == FILE_SELECT_CODE && resultCode == -1) {
			Uri uri = data.getData();
			String path = FileUtils.getPath(this, uri);
			Importer importer = new Importer(getApplicationContext());
			importer.importFile(path);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Log.i(Tag, "ListActivity   onCreateDialog");
		Dialog dialog = null;
		Builder builder = new Builder(this);

		final EditText editText = new EditText(this);
		editText.setInputType(InputType.TYPE_CLASS_TEXT);
		switch (id) {
		case DIALOG:
			DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String group = editText.getText().toString().trim();
					Exporter exporter = new Exporter(getApplicationContext(), group);
					if (exporter.export()) {
						Toast.makeText(getApplicationContext(), "Импортировано в файл" + group + ".xls",
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

	private void getData(String group) {
		DBHelper dbHeper = new DBHelper(this);
		SQLiteDatabase db = dbHeper.getReadableDatabase();

		String query = "SELECT * FROM Lessons WHERE GroupId = ?";
		Cursor c = db.rawQuery(query, new String[] { String.valueOf(group) });
		// Cursor c = db.query("Lessons", null, null, null, null, null, null);

		if (c.moveToFirst()) {
			lessons = new ArrayList<>();

			int idColIndex = c.getColumnIndex("_id");
			int dateColIndex = c.getColumnIndex("Date");
			int groupColIndex = c.getColumnIndex("GroupId");
			int subjectColIndex = c.getColumnIndex("Subject");

			do {
				Lesson lesson = new Lesson();
				lesson.setId(c.getInt(idColIndex));
				lesson.setDate(c.getString(dateColIndex));
				lesson.setGroup(c.getString(groupColIndex));
				lesson.setSubject(c.getString(subjectColIndex));
				lessons.add(lesson);
			} while (c.moveToNext());
		}
		c.close();
		db.close();
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {

		Log.i(Tag, "ListActivity   onNavigationDrawerItemSelected");
		onSectionAttached(position + 1);
	}
}
