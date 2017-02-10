package com.urban.basicsample.parser;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.urban.basicsample.Log_file;

public class Loader extends AsyncTask<String, String, Boolean> {



	private Context context;
	private ProgressDialog pDialog;
	public Log_file fileClass =  new Log_file();

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
			fileClass.writeFile(  " 39 Loader   Ошибка при загрузке расписания ");
		} 
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		pDialog.show();
	}

}
