package com.urban.basicsample;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
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
import com.urban.basicsample.core.OpVerifyNew;
import com.urban.basicsample.dao.DBHelper;
import com.urban.basicsample.parser.Loader;
import com.urban.basicsample.util.DbUtils;
import com.urban.basicsample.util.PassEncrypter;

public class ScanActivity extends Activity implements android.view.View.OnClickListener {

	private static final int DIALOG = 1;
	private static final int PASS_DIALOG = 100;
	private static final int REPEAT_OR_ADD = 99;
	private static final int dialogShow = 42;
	private static final int tvUpdate = 24;

	private PtConnectionAdvancedI mConn = null;
	private PtGlobal mPtGlobal = null;
	private Thread mRunningOp = null;
	private PtInputBir mTemplate = null;
	private DbUtils dbUtils = null;

	private String firstName;
	private String lastName;
	private String mGroup;
	private int week;
	private String day;
	private String subject;
	private int numSubgroup;
	private int part = 1;
	private int lessonId;

	private int count_repeats = 0;

	private static final String Tag = "MyLog";
	MyFileClass file = new MyFileClass();
	private AtomicBoolean isTreadStarted = new AtomicBoolean(false);


	private static final String ACTION_USB_PERMISSION = "com.digitalpersona.java.ptapi.dpfpddusbhost.USB_PERMISSION";

	private final Object mCond = new Object();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//file.writeFile( "ScanActivity   onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);
		Intent intent = getIntent();
		week = intent.getIntExtra("week", 0);
		dbUtils = new DbUtils(getApplicationContext());
	}

	@Override
	protected void onResume() {
		//file.writeFile( "ScanActivity   onResume");
		initialize();
		if (!isTreadStarted.get()) {
			init();
		}
		super.onResume();
	}

	@Override
	protected void onStop() {
		//file.writeFile( "ScanActivity   onStop");
		if (mRunningOp != null) {
			mRunningOp.interrupt();
		}
		closeSession();
		terminatePtapi();
		super.onStop();
	}

	public void initialize() {
	//	file.writeFile( "ScanActivity   initialize");
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
				displayMessage("Error during device opening ScanActivity   initialize - " + e.getMessage());
			}

		}
	}

	private boolean initializePtapi() {
		// Load PTAPI library
		//file.writeFile( "ScanActivity   initializePtapi");
		Context aContext = getApplicationContext();
		mPtGlobal = new PtGlobal(aContext);

		try {
			// Initialize PTAPI interface
			mPtGlobal.initialize();
			return true;
		} catch (java.lang.UnsatisfiedLinkError ule) {
			// Library wasn't loaded properly during PtGlobal object construction
			displayMessage("libjniPtapi.so not loaded   ScanActivity   initializePtapi");
			mPtGlobal = null;
			return false;

		} catch (PtException e) {
			displayMessage(e.getMessage());
			return false;
		}
	}

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			//file.writeFile( "ScanActivity   mUsbReceiver");
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
		//file.writeFile( "ScanActivity   openPtapiSession");
		try {
			// Try to open session
			openPtapiSessionInternal();

			// Device successfully opened
			return;
		} catch (PtException e) {
			displayMessage("Error during device opening  ScanActivity   openPtapiSession -  " + e.getMessage());
		}
	}

	private void openPtapiSessionInternal() throws PtException {
		//file.writeFile( "ScanActivity   openPtapiSessionInternal");
		// Try to open device
		try {
			mConn = (PtConnectionAdvancedI) mPtGlobal.open("USB");
//			mSensorInfo = mConn.info();
		} catch (PtException e) {
			throw e;
		}
	}

	private void init() {
		//file.writeFile( "ScanActivity   init");
		synchronized (mCond) {
			isTreadStarted.set(true);
			mRunningOp = new OpVerifyNew(mConn, 3) {
				@Override
				protected void onFinished(PtInputBir template) {
					synchronized (mCond) {
						mRunningOp = null;
						displayMessage("OK");
						verifyStudent(template);
						mCond.notifyAll();
					}
				}

				@Override
				protected void onDisplayMessage(String message) {
					displayMessage(message);

				}

				@Override
				protected void onWrite(PtBir ptBir) {
				}

				@Override
				protected void onStop() {
					synchronized (mCond) {

						mRunningOp = null;
						mCond.notifyAll(); // notify onDestroy that operation has finished
						closeSession();
						/*
						 * try {
						 *
						 * Constants.mConn.close(); Constants.mConn = null; Constants.mPtGlobal = null; } catch
						 * (PtException e) { // TODO Auto-generated catch block e.printStackTrace(); }
						 */
						// displayMessage(template.toString());

					}
				}
			};
			mRunningOp.start();
		}

	}



	private void verifyStudent(PtInputBir template) {
		file.writeFile( "ScanActivity   verifyStudent");
		boolean flag = false;
		SQLiteDatabase database = null;
		try {
			DBHelper helper = new DBHelper(getApplicationContext());
			database = helper.getWritableDatabase();
			// String[] selectionArgs = new String[] { mGroup };
			// Cursor c = database.query("Students", null, "GroupId = ?", selectionArgs, null, null, null);
			Cursor c = database.query("Students", null, null, null, null, null, null);
			displayMessage(c.toString());
			if (c != null) {
				if (c.moveToFirst()) {
					do {
						byte[] t = c.getBlob(c.getColumnIndex("Scan"));
						try {
							ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(t));
							PtInputBir storedTemplate = (PtInputBir) in.readObject();

							if (mConn.verifyMatch(null, null, false, template, storedTemplate, null, null, null, null)) {
								// displayMessage(storedTemplate.toString());
								firstName = c.getString(c.getColumnIndex("FirstName"));
								lastName = c.getString(c.getColumnIndex("LastName"));
								mGroup = c.getString(c.getColumnIndex("GroupId"));
								numSubgroup = c.getInt(c.getColumnIndex("SubGroup"));
								int id = c.getInt(c.getColumnIndex("_id"));
								if (!dbUtils.checkSchedule(mGroup)) {
									// new Loader(getApplicationContext()).execute(mGroup).get();
									Loader loader = new Loader(ScanActivity.this);
									loader.execute(mGroup);
									loader.get();
									/*
									 * MyTask mt = new MyTask(); mt.execute(mGroup); mt.get();
									 */
									// identifyClass();
								}
								/*
								 * if (groupNumbers == null || groupNumbers.indexOf(mGroup) == -1) { identifyClass();
								 * groupNumbers += mGroup; }
								 */
								// -
								identifyClass();
								// -
								writeToDb(id);
								flag = true;
								displayMessage("DIALOG");

								break;
								// showDialog(DIALOG);
								/*
								 * } else { Intent intent = new Intent(ScanActivity.this, AddStudentActivity.class);
								 * startActivityForResult(intent, 1);
								 */
							}

							// break;
						} catch (Exception e) {
							displayMessage("ERRORver" + e.toString());
						}

					} while (c.moveToNext());
					if (!flag) {
						// displayMessage("REPEAT_OR_ADD");
						addStudent(template);
					}
					flag = false;
				} else {
					// displayMessage("REPEAT_OR_ADD");
					addStudent(template);
				}
			}

		} catch (Exception e) {
			displayMessage(e.toString());
		} finally {
			if (database != null)
				database.close();
		}

	}

	private void addStudent(PtInputBir template) {
		file.writeFile( "ScanActivity   addStudent");
		if (template != null) {
			Object o = template;
			ByteArrayOutputStream bos = null;
			try {
				bos = new ByteArrayOutputStream();
				ObjectOutput out = new ObjectOutputStream(bos);
				out.writeObject(o);
				out.close();
			} catch (IOException e) {
				displayMessage(e.toString());
			}
			// displayMessage("addStudent");
			Intent intent = new Intent(ScanActivity.this, AddStudentActivity.class);
			intent.putExtra("template", bos.toByteArray());
			startActivityForResult(intent, 1);
		} else {
			displayMessage("Template is null");
		}
	}

	private void writeToDb(int studentId) {
		file.writeFile( "ScanActivity   writeToDb   " + studentId);
		SQLiteDatabase database = null;
		try {
			DBHelper helper = new DBHelper(getApplicationContext());
			database = helper.getWritableDatabase();

			String[] selectionArgs = new String[] { String.valueOf(lessonId), String.valueOf(studentId) };
			Cursor c = database.query("Attendance", null, "LessonId = ? AND StudentId = ?", selectionArgs, null, null,
					null);

			if (c.getCount() == 0) {
				database.beginTransaction();
				ContentValues cv = new ContentValues();
				cv.put("LessonId", lessonId);
				cv.put("StudentId", studentId);
				if (part == 1) {
					{
						cv.put("Attendance2", 1);
						cv.put("Attendance1", 1);
					}
				} else {
					cv.put("Attendance2", 1);
				}

				database.insert("Attendance", null, cv);

				database.setTransactionSuccessful();
				database.endTransaction();
			} else {
				String id;
				if (c.moveToFirst()) {
					ContentValues cv = new ContentValues();
					id = c.getString(c.getColumnIndex("_id"));
					if (part == 1) {
						cv.put("Attendance1", 1);
						database.update("Attendance", cv, "_id = ?", new String[] { id });
					} else {
						cv.put("Attendance2", 1);
						database.update("Attendance", cv, "_id = ?", new String[] { id });
					}
				}

			}
		} finally {
			if (database != null)
				database.close();
		}

	}

	private void identifyClass() {
		file.writeFile( "ScanActivity   identifyClass");
		Date date = new Date();
		int hour = date.getHours();
		int min = date.getMinutes();
		int intDay = date.getDay();

		switch (intDay) {
			case 0:
				day = "Воскресенье";
				break;
			case 1:
				day = "Понедельник";
				break;
			case 2:
				day = "Вторник";
				break;
			case 3:
				day = "Среда";
				break;
			case 4:
				day = "Четверг";
				break;
			case 5:
				day = "Пятница";
				break;
			case 6:
				day = "Суббота";
				break;
		}
		file.writeFile( "ScanActivity   identifyClass 123");
		SQLiteDatabase database = null;
		String namesub = " ";
		try {
			DBHelper helper = new DBHelper(getApplicationContext());
			database = helper.getWritableDatabase();
			String[] selectionArgs = new String[] { mGroup, day };
			Cursor c = database
					.query("Schedule", null, "StudentGroup = ? AND Day = ?", selectionArgs, null, null, null);

			if (c != null) {
				if (c.moveToFirst()) {
					do {
						String weekNumber = c.getString(c.getColumnIndex("WeekNumber"));
						int subgroup = c.getInt(c.getColumnIndex("NumSubgroup"));
						if ((weekNumber.charAt(0) == '0' || weekNumber.indexOf(String.valueOf(week)) != -1)
								&& (subgroup == 0 || subgroup == numSubgroup)) {




							String timeStart = c.getString(c.getColumnIndex("LessonTimeStart"));
							String timeEnd = c.getString(c.getColumnIndex("LessonTimeEnd"));

							StringTokenizer stkS = new StringTokenizer(timeStart, ":");
							StringTokenizer stkE = new StringTokenizer(timeEnd, ":");
							int[] arS = new int[stkS.countTokens()];
							int[] arE = new int[stkS.countTokens()];
							for (int i = 0; i < arS.length; i++) {
								arS[i] = Integer.parseInt(stkS.nextToken());
								arE[i] = Integer.parseInt(stkE.nextToken());
							}
							if ((arS[0] * 60 + arS[1]) <= (hour * 60 + min)
									&& (arE[0] * 60 + arE[1]) >= (hour * 60 + min)) {
								// numSubgroup = c.getInt(c.getColumnIndex("NumSubgroup"));
								if (((hour * 60 + min) - (arS[0] * 60 + arS[1])) <= 45) {
									part = 1;
								} else {
									part = 2;
								}
								subject = c.getString(c.getColumnIndex("Subject"));
								if(namesub.equals(c.getString(c.getColumnIndex("Subject"))))
								{
									file.writeFile( "ScanActivity   identifyClass совпали повторы 2");
									count_repeats++;
								}
								// Toast.makeText(getApplicationContext(), subject, 1000).show();
								/*
								 * ((TextView) findViewById(R.id.stv1)).setText(mGroup); ((TextView)
								 * findViewById(R.id.stv2)).setText(subject); ((TextView)
								 * findViewById(R.id.stv3)).setText("?????: " + part);
								 */
								displayMessage("UPDATE_TV");

								writeLessonToDb();

								break;
							}
							else
							{
								if(namesub.equals(c.getString(c.getColumnIndex("Subject"))))
								{
									count_repeats++;
									file.writeFile( "ScanActivity   identifyClass  совпали повторы 1");
								}
								else {
									count_repeats = 0;
									namesub = c.getString(c.getColumnIndex("Subject"));
									file.writeFile( "ScanActivity   identifyClass не совпали повторы");
								}
							}
						}

					} while (c.moveToNext());
				}

			} else {
				Log.d("DEBUG", "Cursor is null");
			}

		} finally {
			if (database != null)
				database.close();
		}
	}

	private void writeLessonToDb() {
		file.writeFile( "ScanActivity   writeLessonToDb");
		Date d = new Date();
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
		String date = format.format(d);

		SQLiteDatabase database = null;

		try {
			DBHelper helper = new DBHelper(getApplicationContext());
			database = helper.getWritableDatabase();

			String[] selectionArgs = new String[] { date, subject };
			Cursor c = database.query("Lessons", null, "Date = ? AND Subject = ?", selectionArgs, null, null, null);
			if (c.getCount() != 0 && c.getCount() == count_repeats + 1) {
				file.writeFile( "ScanActivity   writeLessonToDb   " + c.getCount() + "  " + count_repeats);
				if (c.moveToFirst()) {
					int Index = c.getColumnIndex("_id");
					if (c.getCount() == 1) {
						lessonId = c.getInt(Index);
						file.writeFile("итоговый 1  id = " + lessonId);
					} else {
						do {
							lessonId = c.getInt(Index);
							file.writeFile("итоговый 2  id = " + lessonId);
						}
						while (c.moveToNext());
					}
				}

			} else {
				file.writeFile( "ScanActivity   writeLessonToDb создаем новый");
				database.beginTransaction();

				ContentValues cv = new ContentValues();
				cv.put("Date", date);
				cv.put("GroupId", mGroup);
				cv.put("Subject", subject);

				lessonId = (int) database.insert("Lessons", null, cv);
				file.writeFile( "ScanActivity   writeLessonToDb new id = " + lessonId);
				database.setTransactionSuccessful();
				database.endTransaction();
			}


		} finally {
			database.close();
		}
	}

	protected Dialog onCreateDialog(int id) {
		//file.writeFile( "ScanActivity   onCreateDialog");
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		switch (id) {
			case REPEAT_OR_ADD:
				adb.setTitle("Студент не найден");
				adb.setMessage("Повторить?");
				adb.setIcon(android.R.drawable.ic_dialog_info);
				adb.setPositiveButton("Повторить", myClickListenerNew);
				adb.setNegativeButton("Добавить", myClickListenerNew);
				break;
			case PASS_DIALOG:
				final EditText editText = new EditText(this);
				editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String nPass = editText.getText().toString();
						// Toast.makeText(LoginActivity.this, "Input was: "+nPass, 1000).show();
						if (PassEncrypter.verifyAdminPass(getApplicationContext(), nPass)) {
							finish();
						} else {
							editText.setText("");
							Toast.makeText(getApplicationContext(), "Неверный пароль", Toast.LENGTH_SHORT).show();
						}
					}
				};

				adb.setTitle("Авторизация").setMessage("Введите пароль:")
						.setPositiveButton("Ок", onClickListener).setView(editText).create();
				break;
			default:
				// ?????????
				if(subject != null)
					adb.setTitle(subject + ", гр: " + mGroup);
				else
					adb.setTitle( "В данный момент занятий нету, гр: " + mGroup);
				// ?????????
				adb.setMessage(firstName + " " + lastName);
				// ??????
				adb.setIcon(android.R.drawable.ic_dialog_info);
				// ?????? ?????????????? ??????
				adb.setPositiveButton("Ok", myClickListener);
				// ?????? ?????????????? ??????
				// adb.setNegativeButton("?????????", myClickListener);
				break;
		}
		adb.setCancelable(false);
		// ??????? ??????
		return adb.create();

		// return super.onCreateDialog(id);
	}

	OnClickListener myClickListener = new OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
				// ????????????? ??????
				case Dialog.BUTTON_POSITIVE:
					init();
					dialog.dismiss();
					break;
				// ?????????? ??????
				case Dialog.BUTTON_NEGATIVE:
					closeSession();
					terminatePtapi();
					finish();
					break;

			}
		}
	};

	OnClickListener myClickListenerNew = new OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
				// ????????????? ??????
				case Dialog.BUTTON_POSITIVE:
					init();
					dialog.dismiss();
					break;
				// ?????????? ??????
				case Dialog.BUTTON_NEGATIVE:
					addStudent(mTemplate);
					break;

			}
		}
	};

	@Override
	public void onBackPressed() {
	//	file.writeFile( "ScanActivity   onBackPressed");
		Toast.makeText(getApplicationContext(), "Таким образом выйти невозможно", Toast.LENGTH_SHORT).show();
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//file.writeFile( "ScanActivity   onCreateOptionsMenu");
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.scan, menu);
		return true;
	}

	public void displayMessage(String text) {
		//file.writeFile( "ScanActivity   displayMessage");
		switch (text) {
			case "REPEAT_OR_ADD":
				mHandler.sendMessage(mHandler.obtainMessage(0, REPEAT_OR_ADD, 0, text));
				break;
			case "DIALOG":
				mHandler.sendMessage(mHandler.obtainMessage(0, dialogShow, 0, text));
				break;
			case "UPDATE_TV":
				mHandler.sendMessage(mHandler.obtainMessage(0, tvUpdate, 0, text));
				break;
			default:
				mHandler.sendMessage(mHandler.obtainMessage(0, 0, 0, text));
				break;
		}
		/*
		 * if (text.equalsIgnoreCase("DIALOG")) { mHandler.sendMessage(mHandler.obtainMessage(0, dialogShow, 0, text));
		 * } else { mHandler.sendMessage(mHandler.obtainMessage(0, 0, 0, text)); }
		 */
	}

	private Handler mHandler = new Handler() {

		int i = 101;

		public void handleMessage(Message aMsg) {
			//file.writeFile( "ScanActivity  mHandler  handleMessage");
			switch (aMsg.arg1) {
				case dialogShow:
					showDialog(i++);
					break;
				case REPEAT_OR_ADD:
					showDialog(REPEAT_OR_ADD);
					break;
				case tvUpdate:
					((TextView) findViewById(R.id.stv1)).setText(mGroup);
					((TextView) findViewById(R.id.stv2)).setText(subject);
					((TextView) findViewById(R.id.stv3)).setText("Часть: " + part);
					break;
				default:
					((TextView) findViewById(R.id.EnrollmentTextView1)).setText((String) aMsg.obj);
					break;
			}
			/*
			 * if (aMsg.arg1 == dialogShow) { showDialog(i++); } else { ((TextView)
			 * findViewById(R.id.EnrollmentTextView1)).setText((String) aMsg.obj); }
			 */
		}
	};

	@Override
	protected void onDestroy() {
		//file.writeFile( "ScanActivity   onDestroy");
		// Cancel running operation
		synchronized (mCond) {
			while (mRunningOp != null) {
				mRunningOp.interrupt();
				isTreadStarted.set(false);
				try {
					mCond.wait();
				} catch (InterruptedException e) {
				}
			}
		}

		// Close PTAPI session
		// closeSession();

		// displayMessage("onDestroy");
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		file.writeFile( "ScanActivity   onActivityResult");
		if (requestCode == 1 && resultCode == RESULT_OK) {
			int id = data.getIntExtra("id", -1);
			mGroup = data.getStringExtra("group");
			firstName = data.getStringExtra("firstName");
			lastName = data.getStringExtra("lastName");
			if (id != -1) {
				if (!dbUtils.checkSchedule(mGroup)) {
					if(!isOnline()){
						Toast.makeText(getApplicationContext(), "Нет доступа к интернету!!!",
								Toast.LENGTH_SHORT).show();
						return;
					}
					Loader loader = new Loader(ScanActivity.this);
					loader.execute(mGroup);
					/*
					 * MyTask mt = new MyTask(); mt.execute(mGroup);
					 */
					try {
						loader.get();
					} catch (InterruptedException e) {
						displayMessage("ERROR" + e.toString());
					} catch (ExecutionException e) {
						displayMessage("ERROR" + e.toString());
					}
					// identifyClass();
				}
				/*
				 * if (groupNumbers == null || groupNumbers.indexOf(mGroup) == -1) { identifyClass(); groupNumbers +=
				 * mGroup; }
				 */
				identifyClass();
				writeToDb(id);
				displayMessage("DIALOG");
				// init();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void closeSession() {
		//file.writeFile( "ScanActivity   closeSession");
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
		//file.writeFile( "ScanActivity   terminatePtapi");
		try {
			if (mPtGlobal != null) {
				mPtGlobal.terminate();
			}
		} catch (PtException e) {
			// ignore errors
		}
		mPtGlobal = null;
	}

	@Override
	public void onClick(View v) {
	//	file.writeFile( "ScanActivity   onClick");
		switch (v.getId()) {
			case R.id.s_exit:
				showDialog(PASS_DIALOG);
				break;
			default:
				break;
		}
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

}