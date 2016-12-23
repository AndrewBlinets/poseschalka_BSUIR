package com.urban.basicsample.parser;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.urban.basicsample.LoginActivity;
import com.urban.basicsample.MyFileClass;

public class Loader extends AsyncTask<String, String, Boolean> {



	private Context context;
	private ProgressDialog pDialog;
	public MyFileClass fileClass =  new MyFileClass();

	public Loader(Context context) {
		fileClass.writeFile(  "Loader       constryctor");
		this.context = context;
		pDialog = new ProgressDialog(context);
		pDialog.setMessage("Загрузка расписания...");
		pDialog.setIndeterminate(true);
		pDialog.setCancelable(true);
	}
	
	@Override
	protected Boolean doInBackground(String... params) {
		fileClass.writeFile(  "Loader   doInBackground   "   + params[0]);
		Boolean rez = false;
		Parser parser = new Parser(context);
		rez = parser.loadScheduleForGroupInternal(params[0]);
		return rez;
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		fileClass.writeFile(  "Loader   onPostExecute");
		pDialog.dismiss();
		if (!result.booleanValue()) {
			Toast.makeText(context, "Ошибка при загрузке расписания", Toast.LENGTH_LONG).show();
		} 
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		fileClass.writeFile( "Loader   onPreExecute");
		super.onPreExecute();
		pDialog.show();
	}

}
