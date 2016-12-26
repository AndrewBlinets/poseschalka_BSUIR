package com.urban.basicsample;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.urban.basicsample.util.PassEncrypter;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;


public class LoginActivity extends Activity implements OnClickListener/*,
		NavigationDrawerFragment.NavigationDrawerCallbacks*/ {

	private final int DIALOG = 1;
	private final int ST_DIALOG = 2;
	private Button bAdmin;
	private Button bStudent;
	private Button bP;

	private String nPass;

	private NavigationDrawerFragment mNavigationDrawerFragment;
	private CharSequence mTitle;

	private static final String Tag = "MyLog";
	public MyFileClass fileClass =  new MyFileClass();;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	// стартовое активити
	{


		super.onCreate(savedInstanceState);
		if (getIntent().getBooleanExtra("finish", false))
			finish();
		setContentView(R.layout.fragment_main);

		fileClass.qwe(getApplicationContext());
		fileClass.writeFile_M(this.getLocalClassName() + "   onCreate\n");

		//mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(
		//		R.id.navigation_drawer);
		//mTitle = getTitle();

		// Set up the drawer.
		//mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

		bAdmin = (Button) findViewById(R.id.b_admin);
		bStudent = (Button) findViewById(R.id.b_stud);

		// bP = (Button) findViewById(R.id.parseb);

		// bAdmin.setOnClickListener(this);
		// bStudent.setOnClickListener(this);

		// bP.setOnClickListener(this);

//		InitApi init = new InitApi(getApplicationContext());
//		Constants.mPtGlobal = init.getPtGlobal();
//		Constants.mConn = init.getConn();
	}

	/*public void onSectionAttached(int number) {
		switch (number) {
		case 1:
			mTitle = getString(R.string.title_section1);
			break;
		case 2:
			mTitle = getString(R.string.title_section2);
			break;
		case 3:
			// mTitle = getString(R.string.title_section3);
			finish();
			break;
		}
	}*/

	@Override
	protected Dialog onCreateDialog(int id) {
		Log.i(Tag, "LoginActivity   onCreateDialog");
		fileClass.writeFile(this.getLocalClassName() + "   onCreateDialog\n");
		Dialog dialog = null;
		Builder builder = new Builder(this);
		switch (id) {
		case DIALOG:
			final EditText editText = new EditText(this);
			editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

			DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					nPass = editText.getText().toString();
					// Toast.makeText(LoginActivity.this, "Input was: "+nPass, 1000).show();
					if (PassEncrypter.verifyAdminPass(getApplicationContext(), nPass)) {
						// start activity
						Intent intent = new Intent(LoginActivity.this, MainActivity.class);
						startActivity(intent);
						editText.setText("");
						dialog.dismiss();
					} else {
						editText.setText("");
						Toast.makeText(getApplicationContext(), "Неверный пароль", Toast.LENGTH_SHORT).show();
					}

				}
			};
			
			dialog = builder.setTitle("Авторизация").setMessage("Введите пароль:")
					.setPositiveButton("Ок", onClickListener).setView(editText).create();

			break;
//		case ST_DIALOG:
//			final EditText editText1 = new EditText(this);
//			editText1.setInputType(InputType.TYPE_CLASS_NUMBER);
//			DialogInterface.OnClickListener onClickListener1 = new DialogInterface.OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialog, int which) {
//					String nGroup = editText1.getText().toString();
//					Intent intent = new Intent(LoginActivity.this, ListActivity.class);
//					intent.putExtra("group", nGroup);
//					startActivity(intent);
//				}
//			};
//
//			dialog = builder.setTitle("���� ������������").setMessage("������� ������:")
//					.setPositiveButton("��", onClickListener1).setView(editText1).create();
//			break;
		default:
			break;
		}

		return dialog;/* adb.create(); */
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		// TODO Auto-generated method stub
		super.onPrepareDialog(id, dialog);
		Log.i(Tag, "LoginActivity   onPrepareDialog");
		fileClass.writeFile(this.getLocalClassName() + "   onPrepareDialog\n");
		Builder builder = new Builder(this);

		// Button btnOk = (Button) findViewById(R.id.bPassAdminOk);

		// btnOk.setOnClickListener(onClickListener);
	}

	// @SuppressWarnings("deprecation")
	@Override
	public void onClick(View v) {
		Log.i(Tag, "LoginActivity   onClick");
		fileClass.writeFile(this.getLocalClassName() + "   onClick\n");
		switch (v.getId()) {
		case R.id.b_admin:
			showDialog(DIALOG);
			break;
		case R.id.b_stud:
			//showDialog(ST_DIALOG);
			Intent intent = new Intent(LoginActivity.this, StudentActivity.class);
			startActivity(intent);
			break;
		// case R.id.parseb:
		// Parser parser = new Parser(getApplicationContext());
		// parser.loadScheduleForGroup("450501");
		// break;
		}

	}

	/*@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
				.commit();

	}*/

	/**
	 * A placeholder fragment containing a simple view.
	 */
	/*public static class PlaceholderFragment extends Fragment {
		
		private static final String ARG_SECTION_NUMBER = "section_number";

		
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
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			return rootView;
		}

		@Override
		public void onAttach(Activity activity) {
			super.onAttach(activity);
			((LoginActivity) activity).onSectionAttached(getArguments().getInt(ARG_SECTION_NUMBER));
		}
	}*/
}
