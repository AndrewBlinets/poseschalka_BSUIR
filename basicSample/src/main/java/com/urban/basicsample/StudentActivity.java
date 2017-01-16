package com.urban.basicsample;

import com.urban.basicsample.model.CustomDialog1;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

public class StudentActivity extends Activity implements OnClickListener {

	private final int SCAN_DIALOG = 1;
	private final int LIST_DIALOG = 2;

	private static final String Tag = "MyLog";
MyFileClass file = new MyFileClass();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		file.writeFile( "StudentActivity   onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_student);
	}

	@Override
	public void onClick(View v) {
		file.writeFile( "StudentActivity   onClick");
		switch (v.getId()) {
		case R.id.scan_button:
			showDialog(SCAN_DIALOG);
			break;
		case R.id.list_button:
			showDialog(LIST_DIALOG);
			break;
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		file.writeFile( "StudentActivity   onCreateDialog");
		Dialog dialog = null;
		Builder builder = new Builder(this);
		switch (id) {
		case SCAN_DIALOG:
			//CustomDialog1 cd = new CustomDialog1(this);
			//cd.show();
			int week = getNumberstudyweek();
			Intent intent = new Intent(this, ScanActivity.class);
			intent.putExtra("week", week);
			this.startActivity(intent);

			break;
		case LIST_DIALOG:
			final EditText editText1 = new EditText(this);
			InputFilter[] filterArray = new InputFilter[1];
		    filterArray[0] = new InputFilter.LengthFilter(10);
		    editText1.setFilters(filterArray);
		    editText1.requestFocus();
//			editText1.setInputType(InputType.TYPE_CLASS_NUMBER);
			DialogInterface.OnClickListener onClickListener1 = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String nGroup = editText1.getText().toString();
					Intent intent = new Intent(StudentActivity.this, ListActivity.class);
					intent.putExtra("group", nGroup);
					intent.putExtra("access", false);
					startActivity(intent);
				}
			};
			dialog = builder.setTitle("Учет посещаемости").setMessage("Введите группу:")
					.setPositiveButton("Ок", onClickListener1).setView(editText1).create();
			dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
			break;
		default:
			break;
		}
		return dialog;
	}


	private int getNumberstudyweek()
	{
		Calendar c = Calendar.getInstance();
		int  now_number_week = c.get(Calendar.WEEK_OF_YEAR);
		c.set(c.get(Calendar.YEAR),8,1);
		int week_1_semtember = c.get(Calendar.WEEK_OF_YEAR);
		if(now_number_week > week_1_semtember)// if the date from September 1 to December 31
		return (now_number_week - week_1_semtember) % 4 + 1;
		else
		{
			c.set(c.get(Calendar.YEAR) - 1,8,1);
			week_1_semtember = c.get(Calendar.WEEK_OF_YEAR);
			int day = 31;
			int col_week_in_year = 1;
			do {
				c.set(c.get(Calendar.YEAR) - 1,11,day);
				col_week_in_year = c.get(Calendar.WEEK_OF_YEAR);
				day--;
			}
			while (col_week_in_year == 1);
			return (now_number_week + col_week_in_year - week_1_semtember) % 4 + 1;
		}
	}
}
