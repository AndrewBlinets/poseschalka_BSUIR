package com.urban.basicsample;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.StreamCorruptedException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.digitalpersona.android.ptapi.PtConnectionAdvancedI;
import com.digitalpersona.android.ptapi.PtException;
import com.digitalpersona.android.ptapi.PtGlobal;
import com.digitalpersona.android.ptapi.struct.PtBir;
import com.digitalpersona.android.ptapi.struct.PtInfo;
import com.digitalpersona.android.ptapi.struct.PtInputBir;
import com.digitalpersona.android.ptapi.usb.PtUsbHost;
import com.urban.basicsample.core.InitApi;
import com.urban.basicsample.core.OpVerifyNew;
import com.urban.basicsample.dao.DBHelper;
import com.urban.basicsample.model.Constants;

public class AddStudentActivity extends Activity implements OnClickListener {

	private PtConnectionAdvancedI mConn = null;
	private PtGlobal mPtGlobal = null;
	private Thread mRunningOp = null;
	private PtInputBir mTemplate = null;

	private EditText fN;
	private EditText lN;
	private EditText etGroup;
	private EditText subGroup;
	private Button bScan;

	private byte[] temp = null;
	private final Object mCond = new Object();
	private PtInfo mSensorInfo = null;
	private boolean isConnectionInitialized = false;

	private static final String ACTION_USB_PERMISSION = "com.digitalpersona.java.ptapi.dpfpddusbhost.USB_PERMISSION";

	private static final Pattern NAME_REGEXP = Pattern.compile("^[\\w, ][\\w,\\- ]*\\w$");
	private AtomicBoolean isNewRun;

	private static final String Tag = "MyLog";
	MyFileClass file = new MyFileClass();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		file.writeFile("AddStudentActivity   onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_student);

		fN = (EditText) findViewById(R.id.firstName);
		lN = (EditText) findViewById(R.id.lastName);
		etGroup = (EditText) findViewById(R.id.group);
		subGroup = (EditText) findViewById(R.id.subGroup);
		bScan = (Button) findViewById(R.id.scan);
		Button bAdd = (Button) findViewById(R.id.btnAdd);
		bScan.setOnClickListener(this);
		bAdd.setOnClickListener(this);

		Intent intent = getIntent();
		temp = intent.getByteArrayExtra("template");
		isNewRun = new AtomicBoolean(true);

		if (temp != null) {
			bScan.setEnabled(false);
		} else {
			initialize();
		}
	}

	public void initialize() {
		file.writeFile( "AddStudentActivity   initialize");
		if (initializePtapi()) {
			Context applContext = getApplicationContext();
			PendingIntent mPermissionIntent;
			mPermissionIntent = PendingIntent.getBroadcast(applContext, 0, new Intent(ACTION_USB_PERMISSION), 0);
			IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
			applContext.registerReceiver(mUsbReceiver, filter);

			try {
				// Open PTAPI session
				if (PtUsbHost.PtUsbCheckAndRequestPermissions(applContext, mPermissionIntent)) {
					openPtapiSession();
				}
			} catch (PtException e) {
				dislayMessage("Error during device opening - " + e.getMessage());
			}

		}
	}

	private boolean initializePtapi() {
		file.writeFile( "AddStudentActivity   initializePtapi");
		// Load PTAPI library
		Context aContext = getApplicationContext();
		mPtGlobal = new PtGlobal(aContext);

		try {
			// Initialize PTAPI interface
			mPtGlobal.initialize();
			return true;
		} catch (java.lang.UnsatisfiedLinkError ule) {
			// Library wasn't loaded properly during PtGlobal object construction
			dislayMessage("libjniPtapi.so not loaded");
			mPtGlobal = null;
			return false;

		} catch (PtException e) {
			dislayMessage(e.getMessage());
			return false;
		}
	}

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			file.writeFile( "AddStudentActivity   BroadcastReceiver");
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (device != null) {
							openPtapiSession();
						}
					} else {
						System.exit(0);
					}
				}
			}
		}
	};

	private void openPtapiSession() {
		file.writeFile( "AddStudentActivity   openPtapiSession");
		try {
			// Try to open session
			openPtapiSessionInternal();

			// Device successfully opened
			return;
		} catch (PtException e) {
			dislayMessage("Error during device opening 1 - " + e.getMessage());
		}
	}

	private void openPtapiSessionInternal() throws PtException {
		file.writeFile( "AddStudentActivity   openPtapiSessionInternal");
		// Try to open device
		try {
			mConn = (PtConnectionAdvancedI) mPtGlobal.open("USB");
			mSensorInfo = mConn.info();
		} catch (PtException e) {
			throw e;
		}

	}

	@Override
	protected void onResume() {
		file.writeFile( "AddStudentActivity   onResume");
		// TODO Auto-generated method stub
		super.onResume();
		// InitApi init = new InitApi(getApplicationContext());
		// Constants.mPtGlobal = init.getPtGlobal();
		// Constants.mConn = init.getConn();
	}

	@Override
	public void onClick(View v) {
		file.writeFile( "AddStudentActivity   onClick");
		switch (v.getId()) {
		case R.id.btnAdd:
			file.writeFile( "AddStudentActivity   onClick    add");
			String firstName = fN.getText().toString().trim();
			String dop_str = firstName.substring(0, 1).toUpperCase();
			dop_str += firstName.substring(1);
			firstName = dop_str;
			do {
			if (firstName.indexOf(" ") == -1)
				break;
				else
			{
				dop_str = firstName.substring(0,firstName.indexOf(" ") - 1) + "_" + firstName.substring(firstName.indexOf(" "));
				firstName = dop_str;
			}
			}
			while (true);
			String lastName = lN.getText().toString().trim();
			dop_str = lastName.substring(0, 1).toUpperCase();
			dop_str += lastName.substring(1);
			lastName = dop_str;
			do {
				if (lastName.indexOf(" ") == -1)
					break;
				else
				{
					dop_str = lastName.substring(0,lastName.indexOf(" ") - 1) + "_" + lastName.substring(lastName.indexOf(" "));
					lastName = dop_str;
				}
			}
			while (true);
			String group = etGroup.getText().toString().trim();
			String subGroupStr = subGroup.getText().toString().trim();
			if (!firstName.isEmpty() && !lastName.isEmpty() && !group.isEmpty() && !subGroupStr.isEmpty()
					&& NAME_REGEXP.matcher(firstName).find() && NAME_REGEXP.matcher(lastName).find()) {
				Integer sGroup = Integer.parseInt(subGroupStr);
				if (mTemplate != null) {
					int id = insertStIntoDb(firstName, lastName, group, sGroup, mTemplate);
					Toast.makeText(getApplicationContext(), "ok", Toast.LENGTH_SHORT).show();
					Intent intent = new Intent();
					intent.putExtra("id", id);
					setResult(RESULT_OK, intent);
//					closeSession();
					finish();

				} else if (temp != null) {
					ObjectInputStream in;
					PtInputBir template = null;
					try {
						in = new ObjectInputStream(new ByteArrayInputStream(temp));
						template = (PtInputBir) in.readObject();
					} catch (StreamCorruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					int id = insertStIntoDb(firstName, lastName, group, sGroup, template);
					Intent intent = new Intent();
					intent.putExtra("id", id);
					intent.putExtra("group", group);
					intent.putExtra("firstName", firstName);
					intent.putExtra("lastName", lastName);
					setResult(RESULT_OK, intent);
					finish();
				} else {
					Toast.makeText(getApplicationContext(), "template = null", Toast.LENGTH_SHORT).show();
					// TODO
				}
			} else {
				Toast.makeText(getApplicationContext(), "Некорректный ввод", Toast.LENGTH_SHORT).show();
			}

			break;
		case R.id.scan:
			if (!isConnectionInitialized) {
				synchronized (mCond) {
					isConnectionInitialized = true;
					bScan.setEnabled(false);
					mRunningOp = new OpVerifyNew(mConn, 3) {
						@Override
						protected void onFinished(PtInputBir template) {
							mTemplate = template;
							synchronized (mCond) {

								mRunningOp = null;
								mCond.notifyAll(); // notify onDestroy that operation has finished
								closeSession();
								terminatePtapi();

							}

						}

						@Override
						protected void onDisplayMessage(String message) {
							dislayMessage(message);

						}

						@Override
						protected void onWrite(PtBir ptBir) {
							try {
								BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(openFileOutput("file",
										MODE_APPEND)));
								bw.write(ptBir.data.toString() + "/");
								bw.write(ptBir.factorsMask + "/");
								bw.write(ptBir.formatID + "/");
								bw.write(ptBir.formatOwner + "/");
								bw.write(ptBir.headerVersion + "/");
								bw.write(ptBir.purpose + "/");
								bw.write(ptBir.quality + "/");
								bw.write(ptBir.type + "/");
								bw.close();
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}

						@Override
						protected void onStop() {
							closeSession();

						}
					};
					mRunningOp.start();
				}
			}
			break;
		}
	}

	public void dislayMessage(String text) {
		file.writeFile( "AddStudentActivity  dislayMessage");
		mHandler.sendMessage(mHandler.obtainMessage(0, 0, 0, text));
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message aMsg) {
			file.writeFile( "AddStudentActivity   mHandler");
			((TextView) findViewById(R.id.EnrollmentTextView)).setText((String) aMsg.obj);
		}
	};

	private int insertStIntoDb(String firstName, String lastName, String group, int sGroup, PtInputBir template) {
		file.writeFile( "AddStudentActivity   insertStIntoDb");
		DBHelper helper = new DBHelper(getApplicationContext());
		SQLiteDatabase database = helper.getWritableDatabase();

		Object o = template;
		ByteArrayOutputStream bos = null;
		try {
			bos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(o);
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}

		byte[] bof = bos.toByteArray();

		int lecturer = 0;
		if (/*isNewRun.get() &&*/ !checkLecturer(group)) {
			lecturer = 1;
		}

		database.beginTransaction();

		ContentValues cv = new ContentValues();
		cv.put("FirstName", firstName);
		cv.put("LastName", lastName);
		cv.put("GroupId", group);
		cv.put("SubGroup", sGroup);
		cv.put("Lecturer", lecturer);
		cv.put("Scan", bof);


		int id = (int) database.insert("Students", null, cv);

		database.setTransactionSuccessful();
		database.endTransaction();

		database.close();
		isNewRun.set(false);
		// Toast.makeText(getApplicationContext(), "��!", 100).show();
		return id;
	}


	private boolean checkLecturer(String group) {
		file.writeFile( "AddStudentActivity   checkLecturer");
		DBHelper helper = new DBHelper(getApplicationContext());
		SQLiteDatabase database = helper.getWritableDatabase();

		String[] selectionArgs = new String[] { String.valueOf(group), String.valueOf(1) };
		Cursor c = database.query("Students", null, "GroupId = ? AND Lecturer = ?", selectionArgs, null, null, null);


		if (c.getCount() <= 0) {
			return false;
		} else {
			return true;
		}
	}

	private void closeSession() {
		file.writeFile( "AddStudentActivity   closeSession");
		if (mConn != null) {
			try {
				mConn.close();
			} catch (PtException e) {
				// Ignore errors
			}
			mConn = null;
		}
	}

	private void terminatePtapi() {
		file.writeFile( "AddStudentActivity   terminatePtapi");
		try {
			if (mPtGlobal != null) {
				mPtGlobal.terminate();
			}
		} catch (PtException e) {
			// ignore errors
		}
		mPtGlobal = null;
	}

}
