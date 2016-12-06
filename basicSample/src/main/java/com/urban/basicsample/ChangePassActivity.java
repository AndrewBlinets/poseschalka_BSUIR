package com.urban.basicsample;

import java.util.regex.Pattern;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.urban.basicsample.dao.DBHelper;
import com.urban.basicsample.util.PassEncrypter;

public class ChangePassActivity extends Activity implements OnClickListener {

	private EditText oldPass;
	private EditText newPass1;
	private EditText newPass2;
	private Button bOk;

	private static final Pattern PASS_REGEXP = Pattern.compile("(\\w{2})\\w+");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_pass);
		oldPass = (EditText) findViewById(R.id.EditTextOldPass);
		newPass1 = (EditText) findViewById(R.id.EditTextNewPass1);
		newPass2 = (EditText) findViewById(R.id.EditTextNewPass2);
		bOk = (Button) findViewById(R.id.buttonChangePass);
		bOk.setOnClickListener(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getMenuInflater().inflate(R.menu.change_pass, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonChangePass:
			String pass1 = newPass1.getText().toString().trim();
			String pass2 = newPass2.getText().toString().trim();
			if (PASS_REGEXP.matcher(pass1).find() && PASS_REGEXP.matcher(pass2).find()) {
				if (PassEncrypter.verifyAdminPass(this, oldPass.getText().toString().trim())) {
					if (pass1.equals(pass2)) {
						writeNewPassToDb(pass2);
						Toast.makeText(this, "Пароль изменен", Toast.LENGTH_SHORT).show();
						finish();
					} else {
						Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(this, "Неверный пароль", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(this, "Некорректный ввод", Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			// ignore
			break;
		}
	}

	private void writeNewPassToDb(String newPass) {
		DBHelper dbHelper = new DBHelper(this);
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		ContentValues cv = new ContentValues();
		cv.put("pass", PassEncrypter.encrypt(newPass));
		db.update("Users", cv, "user = ?", new String[] { "admin" });
		cv.clear();
		db.close();
	}
}
