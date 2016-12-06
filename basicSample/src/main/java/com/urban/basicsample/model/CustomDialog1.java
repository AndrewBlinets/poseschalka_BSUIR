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

}
