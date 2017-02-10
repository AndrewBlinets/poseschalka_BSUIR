package com.urban.basicsample;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.DrawerLayout;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.urban.basicsample.dao.DBHelper;
import com.urban.basicsample.model.CustomDialog1;
import com.urban.basicsample.parser.Loader;
import com.urban.basicsample.util.DbUtils;
import com.urban.basicsample.util.Exporter;
import com.urban.basicsample.util.Importer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity implements NavigationDrawerFragmentMain.NavigationDrawerCallbacks {

	private Button startScan;
	private static final int DIALOG = 1;
	private static final int LIST_DIALOG = 2;
	private static final int STUDENT_DIALOG = 3;
	private static final int FILE_SELECT_CODE = 0;
	private static final int LOAD_DIALOG = 4;
	private NavigationDrawerFragmentMain mNavigationDrawerFragment;
	private CharSequence mTitle;

	private static final String Tag = "MyLog";

	private Log_file obj_log = new Log_file();

	@Override
	protected void onCreate(Bundle savedInstanceState) {//активити админа
		super.onCreate(savedInstanceState);
		obj_log.writeFile( " 65  MainActivity   onCreate");
		if (getIntent().getBooleanExtra("finish", false))
			finish();
		setContentView(R.layout.activity_main);

		mNavigationDrawerFragment = (NavigationDrawerFragmentMain) getFragmentManager().findFragmentById(
				R.id.navigation_drawer1);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer1, (DrawerLayout) findViewById(R.id.drawer_layout));

		// startScan = (Button) findViewById(R.id.startScan);
		// startScan.setOnClickListener(this);
	}

	public void onSectionAttached(int number) {
		obj_log.writeFile( "82 MainActivity   onSectionAttached number = " + number);
		switch (number) {
		// case 1:
		// CustomDialog1 cd = new CustomDialog1(this);
		// cd.show();
		// break;
		case 1:
			Intent intent = new Intent(this, AddStudentActivity.class);
			startActivity(intent);
			break;
		case 2:
			showDialog(LIST_DIALOG);
			break;
		case 3:
			showDialog(DIALOG);
			break;
		case 4:
			showDialog(STUDENT_DIALOG);
			break;
		case 5:
			Intent getContentIntent = FileUtils.createGetContentIntent();
			Intent i = Intent.createChooser(getContentIntent, "Выберите файл для загрузки");
			startActivityForResult(i, FILE_SELECT_CODE);
			break;
		case 6: {
			if(isOnline())
			showDialog(LOAD_DIALOG);
			else
			{
				obj_log.writeFile( "111 MainActivity   onSectionAttached нет доступа к интернету.");
				Toast.makeText(getApplicationContext(), "Нет доступа к интернету!!!",
						Toast.LENGTH_SHORT).show();
			}
			break;}
		case 7:
			Intent intent1 = new Intent(this, LoginActivity.class);
			intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent1.putExtra("finish", true);
			startActivity(intent1);
			finish();
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == FILE_SELECT_CODE && resultCode == -1) {
			Uri uri = data.getData();
			String path = FileUtils.getPath(this, uri);
			Importer importer = new Importer(getApplicationContext());
			importer.importFile(path);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		obj_log.writeFile( "138 MainActivity   Dialog id = " + id);
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
					if (group != null && !group.isEmpty()) {
						Exporter exporter = new Exporter(getApplicationContext(), group);
						if (exporter.export()) {
							Toast.makeText(getApplicationContext(), "Экспотировано в файл  " + group + ".xls",
									Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(getApplicationContext(), "Ошибка экспорта," +
									" группа не существует либо не производилась проверка посещаемости.", Toast.LENGTH_SHORT).show();
						}
						dialog.dismiss();
					} else {
						Toast.makeText(getApplicationContext(), "Пустое поле", Toast.LENGTH_SHORT).show();
					}
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
					Intent intent = new Intent(MainActivity.this, ListActivity.class);
					intent.putExtra("group", group);
					intent.putExtra("access", true);
					startActivity(intent);
					dialog.dismiss();
				}
			};
			dialog = builder.setTitle("Введите группу").setPositiveButton("Ок", onClickListener1).setView(editText)
					.create();
			break;
		case LOAD_DIALOG:
			DialogInterface.OnClickListener onClickListener3 = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					DbUtils dbUtils = new DbUtils(MainActivity.this);
					String shedule = editText.getText().toString().trim();
					if (shedule != null && !shedule.isEmpty()) {
						boolean flag = false;
						if (dbUtils.checkSchedule(shedule)) {
							flag = true;
							dbUtils.delete_schetual_group(shedule);
						}
							Loader loader = new Loader(MainActivity.this);
							loader.execute(editText.getText().toString().trim());
							dialog.dismiss();
							try {
								if (loader.get()) {
									if(flag)
										Toast.makeText(MainActivity.this, "Расписание обновлено для группы " + shedule , Toast.LENGTH_SHORT)
												.show();
									else
									Toast.makeText(MainActivity.this, "Расписание загружено для группы " + shedule, Toast.LENGTH_SHORT)
											.show();
								}
								else
								{
									Toast.makeText(MainActivity.this, "Ошибка ввода номера группы", Toast.LENGTH_SHORT)
											.show();
								}
							} catch (Exception e) {
								// TODO
								obj_log.writeFile( "214 MainActivity   Dialog исключение " + e.getMessage());
								e.printStackTrace();
							}
						//} else {
						//	Toast.makeText(MainActivity.this, "Группа была ранее загружена", Toast.LENGTH_SHORT).show();
					//	}
					} else {
						Toast.makeText(MainActivity.this, "Пустое поле", Toast.LENGTH_SHORT).show();
					}
				}
			};
			dialog = builder.setTitle("Введите группу").setPositiveButton("Ок", onClickListener3).setView(editText)
					.create();
			break;
		case STUDENT_DIALOG:
			DialogInterface.OnClickListener onClickListener2 = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String lastName = editText.getText().toString().trim();
					//lastName.substring(0, 1).toUpperCase();
					if (lastName != null && !lastName.isEmpty()) {
						String dop_str = lastName.substring(0, 1).toUpperCase();
						dop_str += lastName.substring(1);
						lastName = dop_str;
						Intent intent = new Intent(getApplicationContext(), StudentListActivity.class);
						intent.putExtra("lastName", lastName);
						startActivity(intent);
						dialog.dismiss();
					}
					else {
						Toast.makeText(MainActivity.this, "Пустое поле", Toast.LENGTH_SHORT).show();
					}
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getTitle().toString()) {
		case "changePass":
			obj_log.writeFile( "267 MainActivity   onOptionsItemSelected изменение пароля");
			Intent intent = new Intent(this, ChangePassActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.container1, PlaceholderFragment.newInstance(position + 1))
				.commit();

	}

	public  boolean isOnline()
	{
		String cs = Context.CONNECTIVITY_SERVICE;
		ConnectivityManager cm = (ConnectivityManager)
				getSystemService(cs);
		if (cm.getActiveNetworkInfo() == null)
			return false;
		 else
			return  true;
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		/**
		 * The fragment argument representing the section number for this fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {

			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

			View rootView = inflater.inflate(R.layout.fragment_main1, container, false);
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {

			super.onAttach(activity);
			((MainActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
		}
	}
}
