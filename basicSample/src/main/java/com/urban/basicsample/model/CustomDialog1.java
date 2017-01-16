package com.urban.basicsample.model;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.urban.basicsample.R;
import com.urban.basicsample.ScanActivity;
import com.urban.basicsample.parser.Parser;

import java.util.Calendar;
import java.util.Date;

public class CustomDialog1 implements OnClickListener {

	private Button dialogButton;
	private ProgressDialog pDialog;
	private Dialog dialog;
	private Activity activity;
	private String group;
	private int week = 0;

	String[] data = { "Первая", "Вторая", "Третья", "Четвертая" };
	String[] data1 = { "Общая", "Первая", "Вторая" };

	public CustomDialog1(Activity activity) {
		this.activity = activity;
		getDayOfWeek();
		init();
	}

	private void init() {
		dialog = new Dialog(activity);
		dialog.setTitle("Выберите неделю");
		dialog.setContentView(R.layout.dialog);

		dialogButton = (Button) dialog.findViewById(R.id.db1);
		dialogButton.setOnClickListener(this);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity.getApplicationContext(),
				R.layout.spinner_item, data);
		adapter.setDropDownViewResource(R.layout.spinner_item);
		Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner11);
		spinner.setAdapter(adapter);
		spinner.setPrompt("Неделя:");
		spinner.setSelection(0);

		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				week = ++position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
			}
		});
	}

	public void show() {
		dialog.show();
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(activity, ScanActivity.class);
		intent.putExtra("week", week);
		activity.startActivity(intent);
		dialog.dismiss();
	}

	class MyTask extends AsyncTask<String, String, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			Boolean rez = false;
			Parser parser = new Parser(activity.getApplicationContext());
			rez = parser.loadScheduleForGroupInternal(params[0]);
			return rez;
		}

		@Override
		protected void onPostExecute(Boolean result) {

			if (!result.booleanValue()) {
				// TODO
			} else {
				Intent intent = new Intent(activity, ScanActivity.class);
				// intent.putExtra("group", group);
				intent.putExtra("week", week);
				// intent.putExtra("subgroup", subgroup);
				activity.startActivity(intent);
				// dialog.dismiss();
				pDialog.dismiss();
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			pDialog = new ProgressDialog(activity);
			pDialog.setMessage("Загрузка расписания..."/* getString(R.string.weit) */);
			pDialog.setIndeterminate(true);
			pDialog.setCancelable(true);
			pDialog.show();
			super.onPreExecute();
		}

	}

	public static int getDayOfWeek() {

		int  aq = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);


		Calendar c = Calendar.getInstance();
		Date date = new Date();
		int a = date.getDay();
		int a1 = date.getMonth();
		int a2 = date.getYear();
		//c.set(Calendar.YEAR, date.getYear());
		//c.set(Calendar.MONTH, date.getMonth());
		//c.set(Calendar.DATE, date.getDate());
		//c.set(date.getYear(), date.getMonth(), date.getDay());//year, month, day);
		int dow = c.get(Calendar.WEEK_OF_YEAR);
		 c.get(Calendar.DATE);
		Calendar c1 = Calendar.getInstance();
		c1.set(Calendar.YEAR, date.getMonth());
		c1.set(Calendar.MONTH, 1);
		c1.set(Calendar.DAY_OF_MONTH, 1);
		int dow1 = c1.get(Calendar.WEEK_OF_YEAR);

		Calendar c2 = Calendar.getInstance();
		c2.set(Calendar.YEAR, date.getMonth());
		c2.set(Calendar.MONTH, 0);
		c2.set(Calendar.DAY_OF_MONTH, 1);
		int dow2 = c2.get(Calendar.WEEK_OF_YEAR);

		Calendar c3 = Calendar.getInstance();
		c3.set(Calendar.YEAR, date.getMonth());
		c3.set(Calendar.MONTH, 0);
		c3.set(Calendar.DAY_OF_MONTH, 0);
		int dow3 = c3.get(Calendar.WEEK_OF_YEAR);

		return dow;
	}

}
