package com.urban.basicsample.parser;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class Loader extends AsyncTask<String, String, Boolean> {

	private Context context;
	private ProgressDialog pDialog;
	
	public Loader(Context context) {
		this.context = context;
		pDialog = new ProgressDialog(context);
		pDialog.setMessage("Загрузка расписания...");
		pDialog.setIndeterminate(true);
		pDialog.setCancelable(true);
	}
	
	@Override
	protected Boolean doInBackground(String... params) {
		Boolean rez = false;
		Parser parser = new Parser(context);
		rez = parser.loadScheduleForGroupInternal(params[0]);
		return rez;
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		pDialog.dismiss();
		if (!result.booleanValue()) {
			Toast.makeText(context, "Ошибка при загрузке расписания", Toast.LENGTH_LONG).show();
		} 
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		pDialog.show();
	}

}
